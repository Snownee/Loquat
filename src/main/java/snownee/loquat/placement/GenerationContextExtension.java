package snownee.loquat.placement;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;

public record GenerationContextExtension(ResourceLocation structureId) {

	public static final Cache<Structure.GenerationContext, GenerationContextExtension> CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).build();

}
