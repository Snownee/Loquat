package snownee.loquat.util;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import snownee.loquat.client.LoquatClient;

public class ClientProxy {
	public static void initClient() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderLevelStageEvent.class, event -> {
			if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
				return;
			}
			PoseStack matrixStack = event.getPoseStack();
			Camera camera = event.getCamera();
			Vec3 pos = camera.getPosition().reverse();
			matrixStack.pushPose();
			LoquatClient.get().render(matrixStack, Minecraft.getInstance().renderBuffers().bufferSource(), pos);
			matrixStack.popPose();
		});
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ClientPlayerNetworkEvent.LoggingOut.class, event -> {
			LoquatClient.get().clearDebugAreas();
		});
	}
}
