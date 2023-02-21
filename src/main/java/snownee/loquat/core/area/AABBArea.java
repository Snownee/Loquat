package snownee.loquat.core.area;

import java.util.Objects;
import java.util.stream.DoubleStream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import snownee.loquat.AreaTypes;

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
		return aabb.contains(x, y, z);
	}

	@Override
	public boolean contains(double x, double y, double z) {
		return aabb.contains(x, y, z);
	}

	@Override
	public Vec3 getCenter() {
		return aabb.getCenter();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AABBArea aabbArea = (AABBArea) o;
		return Objects.equals(aabb, aabbArea.aabb);
	}

	@Override
	public int hashCode() {
		return Objects.hash(aabb);
	}

	@Override
	public Area.Type<?> getType() {
		return AreaTypes.BOX;
	}

	public static class Type extends Area.Type<AABBArea> {

		@Override
		public AABBArea deserialize(CompoundTag data) {
			ListTag doubleList = data.getList("aabb", Tag.TAG_DOUBLE);
			return new AABBArea(new AABB(doubleList.getDouble(0), doubleList.getDouble(1), doubleList.getDouble(2), doubleList.getDouble(3), doubleList.getDouble(4), doubleList.getDouble(5)));
		}

		@Override
		public CompoundTag serialize(CompoundTag data, AABBArea area, boolean networking) {
			ListTag doubleList = new ListTag();
			AABB aabb = area.getAabb();
			DoubleStream.of(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ)
					.mapToObj(DoubleTag::valueOf)
					.forEach(doubleList::add);
			data.put("aabb", doubleList);
			return data;
		}

	}
}
