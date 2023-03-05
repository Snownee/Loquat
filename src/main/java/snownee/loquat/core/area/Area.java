package snownee.loquat.core.area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class Area {
	@Getter
	private final List<String> tags = new ArrayList<>();
	@Getter
	private final Map<String, Zone> zones = new HashMap<>();
	@Getter
	@Setter
	@Nullable
	private UUID uuid;

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

	public static abstract class Type<T extends Area> {
		public abstract T deserialize(CompoundTag data);

		public abstract CompoundTag serialize(CompoundTag data, T area, boolean networking);
	}
}
