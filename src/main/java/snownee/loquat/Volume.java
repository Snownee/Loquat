package snownee.loquat;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Volume {
	@Getter
	private final UUID uuid = UUID.randomUUID();
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

	public abstract void renderDebug(BufferBuilder bufferBuilder, PoseStack poseStack);

}
