package snownee.loquat.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import snownee.loquat.placement.GenerationContextExtension;

@Mixin(JigsawStructure.class)
public class JigsawStructureMixin {

	@Inject(method = "findGenerationPoint", at = @At("HEAD"))
	private void loquat$findGenerationPoint(
			Structure.GenerationContext context,
			CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
		ResourceLocation key = context.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey((JigsawStructure) (Object) this);
		if (key == null) {
			return;
		}
		GenerationContextExtension.CACHE.put(context, new GenerationContextExtension(key));
	}

}
