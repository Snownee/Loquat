package snownee.loquat.spawner;

import java.util.List;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.NaturalSpawner;
import snownee.loquat.LoquatConfig;
import snownee.loquat.core.area.AABBArea;
import snownee.loquat.core.area.Area;
import snownee.loquat.network.SOutlinesPacket;

public record MobEntry(EntityType<?> type) {

	public static MobEntry load(JsonElement json) {
		if (json.isJsonPrimitive()) {
			return new MobEntry(EntityType.byString(json.getAsString()).orElseThrow());
		} else if (json.isJsonObject()) {
			JsonObject jsonObject = json.getAsJsonObject();
			EntityType<?> entityType = EntityType.byString(jsonObject.get("type").getAsString()).orElseThrow();
			return new MobEntry(entityType);
		}
		throw new IllegalArgumentException("Invalid mob entry: " + json);
	}

	public JsonElement save() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", EntityType.getKey(type).toString());
		return jsonObject;
	}

	public boolean createMob(ServerLevel world, Area area, String zoneId) {
		int attempts = 10;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Entity entity = null;
		for (int i = 0; i < attempts; i++) {
			area.getRandomPos(world.random, zoneId, pos);
			if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, world, pos, type)) {
				if (entity == null) {
					entity = type.create(world, null, null, null, pos, MobSpawnType.TRIGGERED, false, false);
					if (entity == null) {
						return true;
					}
				} else {
					entity.moveTo(pos, entity.getYRot(), entity.getXRot());
				}
				AABBArea aabbArea = new AABBArea(entity.getBoundingBox());
				aabbArea.setUuid(UUID.randomUUID());
				if (LoquatConfig.debug) {
					for (ServerPlayer player : world.players()) {
						SOutlinesPacket.outlines(player, world.getGameTime() + 40, true, List.of(aabbArea));
					}
				}
				if (world.noCollision(entity)) {
					return world.addFreshEntity(entity);
				}
			}
		}
		return false;
	}
}
