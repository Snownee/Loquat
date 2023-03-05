package snownee.loquat.mixin;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import snownee.loquat.core.AreaManager;
import snownee.loquat.duck.AreaManagerContainer;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements AreaManagerContainer {
	private AreaManager loquat$areaManager;

	@Override
	public AreaManager loquat$getAreaManager() {
		return loquat$areaManager;
	}

	@Override
	public void loquat$setAreaManager(AreaManager areaManager) {
		loquat$areaManager = areaManager;
	}

	@Inject(at = @At("HEAD"), method = "close")
	private void loquat$onClose(CallbackInfo ci) {
		loquat$areaManager = null;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raids;tick()V"), method = "tick")
	private void loquat$tick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		if (loquat$areaManager != null)
			loquat$areaManager.tick();
	}
}
