package snownee.loquat.core.area;

import java.util.stream.Stream;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import snownee.loquat.AreaTypes;
import snownee.loquat.util.AABBSerializer;
import snownee.loquat.util.LoquatUtil;
import snownee.loquat.util.TransformUtil;

@AllArgsConstructor
public class AABBArea extends Area {

	@Getter
	private final AABB aabb;

	public static AABBArea of(double x1, double y1, double z1, double x2, double y2, double z2) {
		return new AABBArea(new AABB(x1, y1, z1, x2, y2, z2));
	}

	public static AABBArea of(Vec3 pos1, Vec3 pos2) {
		return new AABBArea(new AABB(pos1, pos2));
	}

	@Override
	public boolean contains(int x, int y, int z) {
		return aabb.intersects(x, y, z, x + 1, y + 1, z + 1);
	}

	@Override
	public boolean contains(double x, double y, double z) {
		return aabb.contains(x, y, z);
	}

	@Override
	public boolean intersects(AABB aabb) {
		return this.aabb.intersects(aabb);
	}

	@Override
	public boolean inside(AABB aabb2) {
		return LoquatUtil.isAABBFullyInsideAABB(aabb, aabb2);
	}

	@Override
	public boolean contains(AABB aabb2) {
		return LoquatUtil.isAABBFullyInsideAABB(aabb2, aabb);
	}

	@Override
	public Vec3 getCenter() {
		return aabb.getCenter();
	}

	@Override
	public Vec3 getOrigin() {
		return new Vec3(aabb.minX, aabb.minY, aabb.minZ);
	}

	@Override
	public AABBArea transform(StructurePlaceSettings settings, BlockPos offset) {
		return new AABBArea(TransformUtil.transform(settings, offset, aabb));
	}

	@Override
	public Stream<BlockPos> allBlockPosIn() {
		return BlockPos.betweenClosedStream(aabb.deflate(0.25));
	}

	@Override
	public Object getBounds() {
		return aabb;
	}

	@Override
	public AABB getRoughAABB() {
		return getAabb();
	}

	@Override
	public double distanceToSqr(Vec3 vec) {
		if (aabb.contains(vec)) {
			return 0;
		}
		return aabb.clip(vec, getCenter()).map(vec::distanceToSqr).orElse(Double.MAX_VALUE);
	}

	@Override
	public LongCollection getChunksIn() {
		int minX = SectionPos.blockToSectionCoord(aabb.minX);
		int minZ = SectionPos.blockToSectionCoord(aabb.minZ);
		int maxX = SectionPos.blockToSectionCoord(aabb.maxX);
		int maxZ = SectionPos.blockToSectionCoord(aabb.maxZ);
		if (minX == maxX && minZ == maxZ) {
			return LongSet.of(ChunkPos.asLong(minX, minZ));
		}
		LongList list = new LongArrayList((maxX - minX + 1) * (maxZ - minZ + 1));
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				list.add(ChunkPos.asLong(x, z));
			}
		}
		return list;
	}

	@Override
	public Area.Type<?> getType() {
		return AreaTypes.BOX;
	}

	public static class Type extends Area.Type<AABBArea> {

		@Override
		public AABBArea deserialize(CompoundTag data) {
			return new AABBArea(AABBSerializer.read(data.getList("AABB", Tag.TAG_DOUBLE)));
		}

		@Override
		public CompoundTag serialize(CompoundTag data, AABBArea area) {
			data.put("AABB", AABBSerializer.write(area.aabb));
			return data;
		}

	}
}
