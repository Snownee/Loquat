package snownee.loquat.core.area;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import snownee.loquat.AreaTypes;
import snownee.loquat.util.AABBSerializer;

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
	public Vec3 getCenter() {
		return aabb.getCenter();
	}

	@Override
	public Vec3 getOrigin() {
		return new Vec3(aabb.minX, aabb.minY, aabb.minZ);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
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
			return new AABBArea(AABBSerializer.read(data.getList("AABB", Tag.TAG_DOUBLE)));
		}

		@Override
		public CompoundTag serialize(CompoundTag data, AABBArea area, boolean networking) {
			data.put("AABB", AABBSerializer.write(area.aabb));
			return data;
		}

	}
}
