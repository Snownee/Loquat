package snownee.loquat.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import snownee.loquat.spawner.difficulty.DifficultyLoader;
import snownee.loquat.spawner.LycheeCompat;

@Mixin(value = DifficultyLoader.class, remap = false)
public abstract class DifficultyLoaderMixin implements IdentifiableResourceReloadListener {
	@Override
	public ResourceLocation getFabricId() {
		return LycheeCompat.DIFFICULTY_ID;
	}
}
