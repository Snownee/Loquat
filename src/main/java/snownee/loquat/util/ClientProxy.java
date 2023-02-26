package snownee.loquat.util;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.StructureBlockRenderer;
import snownee.loquat.client.LoquatClient;
import snownee.loquat.modular.ModularModule;

public class ClientProxy implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
			var matrixStack = context.matrixStack();
			matrixStack.pushPose();
			var pos = context.camera().getPosition().reverse();
			matrixStack.translate(pos.x, pos.y, pos.z);
			LoquatClient.render(context.matrixStack(), Minecraft.getInstance().renderBuffers().bufferSource(), pos);
			matrixStack.popPose();
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> LoquatClient.clearDebugAreas());
		BlockEntityRenderers.register(ModularModule.SUPER_STRUCTURE_BLOCK_ENTITY.get(), StructureBlockRenderer::new);
	}
}
