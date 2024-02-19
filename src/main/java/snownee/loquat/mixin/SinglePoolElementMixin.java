package snownee.loquat.mixin;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import snownee.loquat.Hooks;
import snownee.loquat.duck.LoquatStructurePiece;

@Mixin(SinglePoolElement.class)
public class SinglePoolElementMixin {

	@Inject(method = "getSettings", at = @At("TAIL"))
	private void loquat$lowPriorityAddProcessors(
			Rotation rotation,
			BoundingBox boundingBox,
			boolean bl,
			CallbackInfoReturnable<StructurePlaceSettings> cir) {
		Pair<LoquatStructurePiece, RegistryAccess> pair = LoquatStructurePiece.CURRENT.get();
		if (pair != null && pair.getLeft().loquat$getAttachedData() != null) {
			StructurePlaceSettings settings = cir.getReturnValue();
			Hooks.addDynamicProcessors(settings, pair.getRight(), pair.getLeft().loquat$getAttachedData(), "LowPriorityProcessors");
		}
	}

	@Inject(
			method = "getSettings",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/world/level/levelgen/structure/pools/SinglePoolElement;processors:Lnet/minecraft/core/Holder;"),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void loquat$highPriorityAddProcessors(
			Rotation rotation,
			BoundingBox boundingBox,
			boolean bl,
			CallbackInfoReturnable<StructurePlaceSettings> cir,
			StructurePlaceSettings settings) {
		Pair<LoquatStructurePiece, RegistryAccess> pair = LoquatStructurePiece.CURRENT.get();
		if (pair != null && pair.getLeft().loquat$getAttachedData() != null) {
			Hooks.addDynamicProcessors(settings, pair.getRight(), pair.getLeft().loquat$getAttachedData(), "HighPriorityProcessors");
		}
	}

}
