package snownee.loquat.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import snownee.loquat.Hooks;

@Mixin(Screen.class)
public class ScreenMixin {

	@Inject(method = "handleComponentClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;setClipboard(Ljava/lang/String;)V"), cancellable = true)
	private void loquat$handleComponentClicked(Style style, CallbackInfoReturnable<Boolean> cir) {
		if (Hooks.handleComponentClicked(style.getClickEvent().getValue())) {
			cir.setReturnValue(true);
		}
	}

}
