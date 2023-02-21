package snownee.loquat.core.area;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public abstract class Area {
	@Getter
	@Setter
	private UUID uuid;
	@Getter
	private final List<String> tags = new ArrayList<>();

	public abstract boolean contains(int x, int y, int z);

	public abstract boolean contains(double x, double y, double z);

	public final boolean contains(Vec3 pos) {
		return contains(pos.x, pos.y, pos.z);
	}

	public final boolean contains(Vec3i pos) {
		return contains(pos.getX(),pos.getY(),pos.getZ());
	}

    public abstract Vec3 getCenter();

	public abstract Type<?> getType();

	public static abstract class Type<T extends Area> {
		public abstract T deserialize(CompoundTag data);
		public abstract CompoundTag serialize(CompoundTag data, T area);
	}
}
