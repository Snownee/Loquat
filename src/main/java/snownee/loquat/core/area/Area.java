package snownee.loquat.core.area;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import it.unimi.dsi.fastutil.longs.LongCollection;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class Area {
	@Getter
	private final Set<String> tags = new LinkedHashSet<>(4);
	@Getter
	private final Map<String, Zone> zones = new HashMap<>(2);
	@Getter
	@Setter
	@Nullable
	private UUID uuid;
	@Getter
	@Setter
	@Nullable
	private CompoundTag attachedData;

	public abstract boolean contains(int x, int y, int z);

	public abstract boolean contains(double x, double y, double z);

	public final boolean contains(Vec3 pos) {
		return contains(pos.x, pos.y, pos.z);
	}

	public final boolean contains(Vec3i pos) {
		return contains(pos.getX(), pos.getY(), pos.getZ());
	}

	public abstract boolean intersects(AABB aabb);

	public abstract boolean inside(AABB aabb);

	public abstract boolean contains(AABB aabb);

	public abstract Vec3 getCenter();

	public abstract Vec3 getOrigin();

	public abstract Stream<BlockPos> allBlockPosIn();

	public abstract Type<?> getType();

	public void getRandomPos(RandomSource random, String zoneId, BlockPos.MutableBlockPos pos) {
		Zone zone = zones.get(zoneId);
		Preconditions.checkNotNull(zone, "Zone %s not found", zoneId);
		double sum = zone.aabbs().stream().mapToDouble(AABB::getSize).sum();
		double r = random.nextDouble() * sum;
		for (AABB aabb : zone.aabbs()) {
			if (r < aabb.getSize()) {
				double x = aabb.minX + random.nextDouble() * (aabb.maxX - aabb.minX);
				double y = aabb.minY + random.nextDouble() * (aabb.maxY - aabb.minY);
				double z = aabb.minZ + random.nextDouble() * (aabb.maxZ - aabb.minZ);
				pos.set(x, y, z);
				return;
			}
			r -= aabb.getSize();
		}
		throw new IllegalStateException();
	}

	public abstract Area transform(StructurePlaceSettings settings, BlockPos offset);

	public abstract Object getBounds();

	public abstract LongCollection getChunksIn();

	public abstract AABB getRoughAABB();

	public abstract double distanceToSqr(Vec3 vec);

	public Optional<BlockPos.MutableBlockPos> findSpawnPos(ServerLevel world, String zoneId, Entity entity) {
		int attempts = 10;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, getRoughAABB(), EntitySelector.NO_SPECTATORS);
		int bestAttemptX = 0;
		int bestAttemptY = 0;
		int bestAttemptZ = 0;
		double bestAttemptScore = Double.NEGATIVE_INFINITY;
		SpawnPlacements.Type placementType = SpawnPlacements.getPlacementType(entity.getType());
		for (int i = 0; i < attempts; i++) {
			getRandomPos(world.random, zoneId, pos);
			if (NaturalSpawner.isSpawnPositionOk(placementType, world, pos, entity.getType())) {
				entity.moveTo(pos, entity.getYRot(), entity.getXRot());
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
			return Optional.empty();
		}
		return Optional.of(pos.set(bestAttemptX, bestAttemptY, bestAttemptZ));
	}

	public CompoundTag getOrCreateAttachedData() {
		if (attachedData == null) {
			attachedData = new CompoundTag();
		}
		return attachedData;
	}

	public abstract Optional<VoxelShape> getVoxelShape();

	public static abstract class Type<T extends Area> {
		public abstract T deserialize(CompoundTag data);

		public abstract CompoundTag serialize(CompoundTag data, T area);
	}
}
