package snownee.loquat.util;

import java.util.Optional;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class RegistryBridge<T> {
	private final IForgeRegistry<T> registry;

	public RegistryBridge(IForgeRegistry<T> registry) {
		this.registry = registry;
	}

	public T get(ResourceLocation id) {
		return registry.getValue(id);
	}

	public ResourceLocation getKey(T value) {
		return registry.getKey(value);
	}

	public Optional<T> getOptional(ResourceLocation id) {
		return registry.containsKey(id) ? Optional.of(registry.getValue(id)) : Optional.empty();
	}

	public void register(ResourceLocation id, T value) {
		registry.register(id, value);
	}

	public ResourceKey<Registry<T>> getRegistryKey() {
		return registry.getRegistryKey();
	}

}
