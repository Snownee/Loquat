package snownee.loquat.client;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import snownee.loquat.AreaTypes;
import snownee.loquat.core.area.AABBArea;
import snownee.loquat.core.area.Area;
import snownee.loquat.util.Color;

public class LoquatClient {

	public static final Map<UUID, RenderDebugData> renderDebugAreas = Collections.synchronizedMap(Maps.newLinkedHashMap()); // let's pray it's thread-safe
	public static final Map<Area.Type<?>, BiConsumer<RenderDebugContext, RenderDebugData>> renderers = Maps.newHashMap();
	private static ResourceKey<Level> oDimension;

	static {
		renderers.put(AreaTypes.BOX, (ctx, data) -> {
			var buffer = ctx.bufferSource().getBuffer(RenderType.lines());
			var aabb = ((AABBArea) data.area).getAabb().inflate(0.01);
			var color = Color.rgb(data.color());
			float alpha = (float) color.getOpacity();
			if (ctx.time() + 20 > data.expire) {
				alpha *= (data.expire() - ctx.time()) / 20F;
			}
			LevelRenderer.renderLineBox(ctx.poseStack(), buffer, aabb, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, alpha);
		});
	}

	public static void render(PoseStack matrixStack, MultiBufferSource.BufferSource bufferSource) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}
		if (oDimension != level.dimension()) {
			clearDebugAreas();
			oDimension = level.dimension();
		}
		if (renderDebugAreas.isEmpty()) {
			return;
		}
		long time = level.getGameTime();
		var context = new RenderDebugContext(matrixStack, bufferSource, time);
		renderDebugAreas.values().removeIf(data -> time >= data.expire);
		for (var data : renderDebugAreas.values()) {
			Objects.requireNonNull(renderers.get(data.area.getType())).accept(context, data);
		}
		bufferSource.endBatch();
	}

	public static void clearDebugAreas() {
		renderDebugAreas.clear();
	}

	public record RenderDebugData(Area area, int color, long expire) {
	}

	public record RenderDebugContext(PoseStack poseStack, MultiBufferSource bufferSource, long time) {
	}

}
