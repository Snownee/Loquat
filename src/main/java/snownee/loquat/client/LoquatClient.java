package snownee.loquat.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import snownee.loquat.core.area.AABBArea;
import snownee.loquat.core.area.Area;
import snownee.loquat.util.Color;

public class LoquatClient {

	public static final List<RenderDebugData> renderDebugAreas = Collections.synchronizedList(Lists.newArrayList()); // let's pray it's thread-safe
	public static final Map<Class<?>, BiConsumer<RenderDebugContext, RenderDebugData>> renderers = Maps.newHashMap();

	static {
		renderers.put(AABBArea.class, (ctx, data) -> {
			var buffer = ctx.bufferSource().getBuffer(RenderType.lines());
			var aabb = ((AABBArea) data.area).getAabb();
			var color = Color.rgb(0xFFFFFFFF);
			LevelRenderer.renderLineBox(ctx.poseStack(), buffer, aabb, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
		});
	}

	public static void render(PoseStack matrixStack, MultiBufferSource bufferSource) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null || renderDebugAreas.isEmpty()) {
			return;
		}
		var context = new RenderDebugContext(matrixStack, Minecraft.getInstance().renderBuffers().bufferSource());
		long time = level.getGameTime();
		renderDebugAreas.removeIf(data -> time >= data.expire);
		for (var data : renderDebugAreas) {
			Objects.requireNonNull(renderers.get(data.area.getClass())).accept(context, data);
		}
		LevelRenderer.renderLineBox(context.poseStack(), context.bufferSource().getBuffer(RenderType.lines()), new AABB(BlockPos.ZERO), 1, 1, 1, 1);
		Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
	}

	public record RenderDebugData(Area area, int color, long expire) {
	}

	public record RenderDebugContext(PoseStack poseStack, MultiBufferSource bufferSource) {
	}

}
