package snownee.loquat.spawner;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.NaturalSpawner;
import snownee.loquat.core.area.Area;
import snownee.lychee.util.LUtil;

public record MobEntry(EntityType<?> type, @NotNull CompoundTag nbt, boolean randomize) {

	public static MobEntry load(JsonElement json) {
		if (json.isJsonPrimitive()) {
			String entityTypeId = json.getAsString();
			CompoundTag nbt = new CompoundTag();
			nbt.putString("id", entityTypeId);
			return new MobEntry(EntityType.byString(entityTypeId).orElseThrow(), nbt, false);
		} else if (json.isJsonObject()) {
			JsonObject jsonObject = json.getAsJsonObject();
			String entityTypeId = jsonObject.get("type").getAsString();
			EntityType<?> entityType = EntityType.byString(entityTypeId).orElseThrow();
			CompoundTag nbt;
			if (jsonObject.has("nbt")) {
				nbt = LUtil.jsonToTag(jsonObject.getAsJsonObject("nbt"));
			} else {
				nbt = new CompoundTag();
			}
			nbt.putString("id", entityTypeId);
			boolean randomize = GsonHelper.getAsBoolean(jsonObject, "randomize", true);
			return new MobEntry(entityType, nbt, randomize);
		}
		throw new IllegalArgumentException("Invalid mob entry: " + json);
	}

	public JsonElement save() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", EntityType.getKey(type).toString());
		if (nbt.getAllKeys().size() > 1) {
			jsonObject.add("nbt", LUtil.tagToJson(nbt));
		}
		return jsonObject;
	}

	public Entity createMob(ServerLevel world, Area area, String zoneId) {
		int attempts = 10;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Entity entity = null;
		for (int i = 0; i < attempts; i++) {
			area.getRandomPos(world.random, zoneId, pos);
			if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, world, pos, type)) {
				if (entity == null) {
					entity = EntityType.loadEntityRecursive(nbt.copy(), world, e -> {
						if (randomize && e instanceof Mob mob) {
							mob.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.TRIGGERED, null, null);
						}
						return e;
					});
					if (entity == null) {
						return null;
					}
					Preconditions.checkState(entity instanceof LivingEntity, "Entity %s is not a LivingEntity", entity);
				}
				entity.moveTo(pos, entity.getYRot(), entity.getXRot());
				//				AABBArea aabbArea = new AABBArea(entity.getBoundingBox());
				//				aabbArea.setUuid(UUID.randomUUID());
				//				if (LoquatConfig.debug) {
				//					for (ServerPlayer player : world.players()) {
				//						SOutlinesPacket.outlines(player, world.getGameTime() + 40, true, List.of(aabbArea));
				//					}
				//				}
				if (world.noCollision(entity)) {
					entity.getIndirectPassengers().forEach(e -> e.moveTo(pos, e.getYRot(), e.getXRot()));
					world.addFreshEntityWithPassengers(entity);
					return entity;
				}
			}
		}
		return null;
	}
}
