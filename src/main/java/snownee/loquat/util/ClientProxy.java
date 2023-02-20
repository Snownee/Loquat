package snownee.loquat.util;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import snownee.loquat.client.LoquatClient;

public class ClientProxy implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WorldRenderEvents.END.register(context -> {
			context.matrixStack().pushPose();
			context.matrixStack().translate(-context.camera().getPosition().x, -context.camera().getPosition().y, -context.camera().getPosition().z);
			LoquatClient.render(context.matrixStack(), context.consumers());
			context.matrixStack().popPose();
		});
	}
}
