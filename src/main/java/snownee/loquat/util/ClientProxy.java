package snownee.loquat.util;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import snownee.loquat.client.LoquatClient;

public class ClientProxy implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
			var matrixStack = context.matrixStack();
			matrixStack.pushPose();
			var pos = context.camera().getPosition();
			matrixStack.translate(-pos.x, -pos.y, -pos.z);
			LoquatClient.render(context.matrixStack(), Minecraft.getInstance().renderBuffers().bufferSource(), pos);
			matrixStack.popPose();
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> LoquatClient.clearDebugAreas());
	}
}
