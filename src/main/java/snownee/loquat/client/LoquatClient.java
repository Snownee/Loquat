package snownee.loquat.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import snownee.loquat.WorldVolume;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class LoquatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WorldRenderEvents.END.register(context -> WorldVolume.getVolumeManager(
				Objects.requireNonNull(Minecraft.getInstance().player).clientLevel.dimension()).renderDebug(context.matrixStack()));
	}
}
