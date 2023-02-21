package snownee.loquat.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import snownee.loquat.AreaTypes;
import snownee.loquat.core.area.AABBArea;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.PosSelection;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.util.Color;

public class LoquatClient {

	// let's pray it's thread-safe
	public static final Map<UUID, RenderDebugData> normalOutlines = Collections.synchronizedMap(Maps.newLinkedHashMap());
	public static final Map<UUID, RenderDebugData> highlightOutlines = Collections.synchronizedMap(Maps.newLinkedHashMap());
	public static final Map<Area.Type<?>, BiConsumer<RenderDebugContext, RenderDebugData>> renderers = Maps.newHashMap();
	private static ResourceKey<Level> oDimension;

	static {
		renderers.put(AreaTypes.BOX, (ctx, data) -> {
			var buffer = ctx.bufferSource().getBuffer(RenderType.lines());
			var aabb = ((AABBArea) data.area).getAabb().inflate(0.01);
			var color = Color.rgb(data.color());
			float alpha = (float) color.getOpacity();
			if (ctx.time() + 10 > data.expire) {
				alpha *= (data.expire() - ctx.time()) / 10F;
			}
			LevelRenderer.renderLineBox(ctx.poseStack(), buffer, aabb, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, alpha);
		});
	}

	public static void render(PoseStack matrixStack, MultiBufferSource.BufferSource bufferSource) {
		ClientLevel level = Minecraft.getInstance().level;
		LocalPlayer player = Minecraft.getInstance().player;
		if (level == null || player == null) {
			return;
		}
		List<PosSelection> selections = SelectionManager.of(player).getSelections();
		if (oDimension != level.dimension()) {
			clearDebugAreas();
			oDimension = level.dimension();
		}
		var outlines = highlightOutlines.isEmpty() ? normalOutlines : highlightOutlines;
		if (outlines.isEmpty() && selections.isEmpty()) {
			return;
		}
		long time = level.getGameTime();
		var context = new RenderDebugContext(matrixStack, bufferSource, time);
		if (!outlines.isEmpty()) {
			renderAreas(context, level, outlines);
		}
		if (!selections.isEmpty()) {
			renderSelections(context, level, selections);
		}
		bufferSource.endBatch();
	}

	private static void renderAreas(RenderDebugContext context, ClientLevel level, Map<UUID, RenderDebugData> outlines) {
		outlines.values().removeIf(data -> context.time >= data.expire);
		for (var data : outlines.values()) {
			Objects.requireNonNull(renderers.get(data.area.getType())).accept(context, data);
		}
	}

	private static void renderSelections(RenderDebugContext context, ClientLevel level, List<PosSelection> selections) {
		var buffer = context.bufferSource().getBuffer(RenderType.lines());
		for (PosSelection selection : selections) {
			AABB aabb = selection.toAABB().inflate(0.01);
			LevelRenderer.renderLineBox(context.poseStack(), buffer, aabb, 0, 0, 1, 1);
		}
	}

	public static void clearDebugAreas() {
		normalOutlines.clear();
	}

	public record RenderDebugData(Area area, int color, long expire) {
	}

	public record RenderDebugContext(PoseStack poseStack, MultiBufferSource bufferSource, long time) {
	}

}
