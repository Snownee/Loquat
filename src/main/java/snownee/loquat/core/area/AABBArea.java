package snownee.loquat.core.area;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

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
}
