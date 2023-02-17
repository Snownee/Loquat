package snownee.loquat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.AllArgsConstructor;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

@AllArgsConstructor
public class AABBVolume extends Volume {

	private final AABB aabb;

	public static AABBVolume of(double x1, double y1, double z1, double x2, double y2, double z2) {
		return new AABBVolume(new AABB(x1, y1, z1, x2, y2, z2));
	}

	public static AABBVolume of(Vec3 pos1, Vec3 pos2) {
		return new AABBVolume(new AABB(pos1, pos2));
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
	public void renderDebug(BufferBuilder bufferBuilder, PoseStack pose) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
		edges((x1, y1, z1, x2, y2, z2) -> {
			bufferBuilder.vertex(pose.last().pose(),(float)x1,(float)y1,(float)z1).color(0xFFFFFFFF).endVertex();
			bufferBuilder.vertex(pose.last().pose(),(float)x2,(float)y2,(float)z2).color(0xFFFFFFFF).endVertex();
		});
		BufferUploader.drawWithShader(bufferBuilder.end());
	}

	public void edges(Shapes.DoubleLineConsumer consumer) {
		var minX = aabb.minX;
		var minY = aabb.minY;
		var minZ = aabb.minZ;
		var maxX = aabb.maxX;
		var maxY = aabb.maxY;
		var maxZ = aabb.maxZ;
		consumer.consume(minX, minY, minZ, maxX, minY, minZ);
		consumer.consume(minX, minY, minZ, minX, maxY, minZ);
		consumer.consume(minX, minY, minZ, minX, minY, maxZ);
		consumer.consume(minX, minY, minZ, maxX, minY, minZ);
		consumer.consume(minX, minY, minZ, minX, maxY, minZ);
		consumer.consume(minX, minY, minZ, minX, minY, maxZ);
	}
}
