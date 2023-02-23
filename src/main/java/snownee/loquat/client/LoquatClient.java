package snownee.loquat.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
			var aabb = ((AABBArea) data.area).getAabb().inflate(0.01);
			var color = data.type.color;
			float alpha = 1;
			if (ctx.time() + 10 > data.expire) {
				alpha *= (data.expire - ctx.time()) / 10F;
			}
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			DebugRenderer.renderFilledBox(aabb.move(ctx.pos), color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, alpha * 0.2F);
			var buffer = ctx.bufferSource().getBuffer(RenderType.lines());
			LevelRenderer.renderLineBox(ctx.poseStack(), buffer, aabb, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, alpha);
		});
	}

	public static void render(PoseStack matrixStack, MultiBufferSource.BufferSource bufferSource, Vec3 pos) {
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
		var context = new RenderDebugContext(matrixStack, bufferSource, time, pos);
		if (!outlines.isEmpty()) {
			renderAreas(context, level, player, outlines);
		}
		if (!selections.isEmpty()) {
			renderSelections(context, level, selections);
		}
	}

	private static void renderAreas(RenderDebugContext context, ClientLevel level, LocalPlayer player, Map<UUID, RenderDebugData> outlines) {
		outlines.values().removeIf(data -> context.time >= data.expire);
		SelectionManager selectionManager = SelectionManager.of(player);
		for (var data : outlines.values()) {
			if (data.type != DebugAreaType.HIGHLIGHT)
				data.type = selectionManager.isSelected(data.area) ? DebugAreaType.SELECTED : DebugAreaType.NORMAL;
			Objects.requireNonNull(renderers.get(data.area.getType())).accept(context, data);
			if (data.type == DebugAreaType.SELECTED) {
				var buffer = context.bufferSource().getBuffer(RenderType.lines());
				data.area.getZones().forEach((name, zone) -> {
					float alpha = 1;
					if (context.time() + 10 > data.expire) {
						alpha *= (data.expire - context.time()) / 10F;
					}
					RenderSystem.disableTexture();
					RenderSystem.disableDepthTest();
					for (AABB aabb : zone.aabbs()) {
						var center = aabb.getCenter();
						if (aabb.getYsize() > 1) {
							aabb = aabb.inflate(0.02);
						} else {
							aabb = aabb.deflate(0.2, 0.5, 0.2).move(0, -0.499, 0);
						}
						LevelRenderer.renderLineBox(context.poseStack(), buffer, aabb, 1, 0.6F, 0, alpha);
						DebugRenderer.renderFloatingText(name, center.x, center.y, center.z, 0, 0.045F);
					}
				});
			}
		}
	}

	private static void renderSelections(RenderDebugContext context, ClientLevel level, List<PosSelection> selections) {
		var buffer = context.bufferSource().getBuffer(RenderType.lines());
		for (PosSelection selection : selections) {
			AABB aabb = selection.toAABB().inflate(0.01);
			LevelRenderer.renderLineBox(context.poseStack(), buffer, aabb, 0.4F, 0.4F, 1, 1);
		}
	}

	public static void clearDebugAreas() {
		normalOutlines.clear();
	}

	public enum DebugAreaType {
		NORMAL(Color.rgb(255, 255, 255)), HIGHLIGHT(Color.rgb(255, 255, 255)), SELECTED(Color.rgb(180, 255, 180));

		private final Color color;

		DebugAreaType(Color color) {
			this.color = color;
		}
	}

	public static class RenderDebugData {
		public final Area area;
		public DebugAreaType type;
		public long expire;

		public RenderDebugData(Area area, DebugAreaType type, long expire) {
			this.area = area;
			this.type = type;
			this.expire = expire;
		}
	}

	public record RenderDebugContext(PoseStack poseStack, MultiBufferSource bufferSource, long time, Vec3 pos) {
	}

}