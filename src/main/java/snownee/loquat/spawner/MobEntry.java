package snownee.loquat.spawner;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NaturalSpawner;
import snownee.loquat.core.area.Area;
import snownee.lychee.util.LUtil;

public record MobEntry(EntityType<?> type, @NotNull CompoundTag nbt, boolean randomize,
					   @Nullable Object2DoubleMap<String> attrs) {

	public static final Map<String, Attribute> SIMPLE_ATTRIBUTES = Maps.newHashMap();

	static {
		SIMPLE_ATTRIBUTES.put("hp", Attributes.MAX_HEALTH);
		SIMPLE_ATTRIBUTES.put("movement_speed", Attributes.MOVEMENT_SPEED);
		SIMPLE_ATTRIBUTES.put("damage", Attributes.ATTACK_DAMAGE);
		SIMPLE_ATTRIBUTES.put("attack_speed", Attributes.ATTACK_SPEED);
		SIMPLE_ATTRIBUTES.put("armor", Attributes.ARMOR);
		SIMPLE_ATTRIBUTES.put("armor_toughness", Attributes.ARMOR_TOUGHNESS);
		SIMPLE_ATTRIBUTES.put("knockback_resistance", Attributes.KNOCKBACK_RESISTANCE);
		SIMPLE_ATTRIBUTES.put("knockback", Attributes.ATTACK_KNOCKBACK);
	}

	public static MobEntry load(JsonElement json) {
		if (json.isJsonPrimitive()) {
			String entityTypeId = json.getAsString();
			CompoundTag nbt = new CompoundTag();
			nbt.putString("id", entityTypeId);
			return new MobEntry(EntityType.byString(entityTypeId).orElseThrow(), nbt, false, null);
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
			Object2DoubleMap<String> attrs = jsonObject.has("attrs") ? new Object2DoubleArrayMap<>() : null;
			if (attrs != null) {
				jsonObject.getAsJsonObject("attrs").entrySet().forEach(entry -> {
					Attribute attr = SIMPLE_ATTRIBUTES.computeIfAbsent(entry.getKey(), s -> {
						return Registry.ATTRIBUTE.get(new ResourceLocation(s));
					});
					Preconditions.checkNotNull(attr, "Invalid attribute: " + entry.getKey());
					attrs.put(entry.getKey(), entry.getValue().getAsDouble());
				});
			}
			return new MobEntry(entityType, nbt, randomize, attrs);
		}
		throw new IllegalArgumentException("Invalid mob entry: " + json);
	}

	public JsonElement save() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", EntityType.getKey(type).toString());
		if (nbt.getAllKeys().size() > 1) {
			jsonObject.add("nbt", LUtil.tagToJson(nbt));
		}
		if (!randomize) {
			jsonObject.addProperty("randomize", false);
		}
		if (attrs != null) {
			JsonObject attrsObject = new JsonObject();
			attrs.forEach(attrsObject::addProperty);
			jsonObject.add("attrs", attrsObject);
		}
		return jsonObject;
	}

	public Entity createMob(ServerLevel world, Area area, String zoneId, Consumer<Entity> consumer) {
		int attempts = 10;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, area.getRoughAABB(), EntitySelector.NO_SPECTATORS);
		Entity entity = null;
		int bestAttemptX = 0;
		int bestAttemptY = 0;
		int bestAttemptZ = 0;
		double bestAttemptScore = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < attempts; i++) {
			area.getRandomPos(world.random, zoneId, pos);
			if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, world, pos, type)) {
				if (entity == null) {
					entity = EntityType.loadEntityRecursive(nbt.copy(), world, e -> {
						e.moveTo(pos, e.getYRot(), e.getXRot());
						if (randomize && e instanceof Mob mob) {
							mob.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.TRIGGERED, null, null);
						}
						return e;
					});
					if (entity == null) {
						return null;
					}
					Preconditions.checkState(entity instanceof LivingEntity, "Entity %s is not a LivingEntity", entity);
				} else {
					entity.moveTo(pos, entity.getYRot(), entity.getXRot());
				}
				//				AABBArea aabbArea = new AABBArea(entity.getBoundingBox());
				//				aabbArea.setUuid(UUID.randomUUID());
				//				if (LoquatConfig.debug) {
				//					for (ServerPlayer player : world.players()) {
				//						SOutlinesPacket.outlines(player, world.getGameTime() + 40, true, List.of(aabbArea));
				//					}
				//				}
				if (world.noCollision(entity)) {
					double score = 0;
					for (LivingEntity nearbyEntity : nearbyEntities) {
						double distance = entity.distanceToSqr(nearbyEntity);
						if (nearbyEntity instanceof Player) {
							if (distance < 100) {
								score -= 100 - distance;
							}
						} else {
							if (distance < 9) {
								score -= 9 - distance;
							}
						}
					}
					if (score > bestAttemptScore) {
						bestAttemptScore = score;
						bestAttemptX = pos.getX();
						bestAttemptY = pos.getY();
						bestAttemptZ = pos.getZ();
						if (score == 0) {
							break;
						}
					}
				}
			}
		}
		if (bestAttemptScore == Double.NEGATIVE_INFINITY) {
			return null;
		}
		if (attrs != null) {
			for (Object2DoubleMap.Entry<String> entry : attrs.object2DoubleEntrySet()) {
				Attribute attr = SIMPLE_ATTRIBUTES.get(entry.getKey());
				Preconditions.checkNotNull(attr, "Invalid attribute: " + entry.getKey());
				((LivingEntity) entity).getAttribute(attr).setBaseValue(entry.getDoubleValue());
				if ("hp".equals(entry.getKey())) {
					((LivingEntity) entity).setHealth((float) entry.getDoubleValue());
				}
			}
		}
		pos.set(bestAttemptX, bestAttemptY, bestAttemptZ);
		entity.moveTo(pos, entity.getYRot(), entity.getXRot());
		consumer.accept(entity);
		entity.getIndirectPassengers().forEach(e -> {
			e.moveTo(pos, e.getYRot(), e.getXRot());
			consumer.accept(e);
		});
		world.addFreshEntityWithPassengers(entity);
		return entity;
	}
}
