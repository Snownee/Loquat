package snownee.loquat.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import snownee.loquat.util.LoquatDataLoader;

@Mixin(value = LoquatDataLoader.class, remap = false)
public abstract class LoquatDataLoaderMixin implements IdentifiableResourceReloadListener {
	@Shadow
	private ResourceLocation id;

	@Override
	public ResourceLocation getFabricId() {
		return id;
	}

//	@Override
//	public Collection<ResourceLocation> getFabricDependencies() {
//		return List.of(LycheeCompat.DIFFICULTY_ID);
//	}
}
