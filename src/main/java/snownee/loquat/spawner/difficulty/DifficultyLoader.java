package snownee.loquat.spawner.difficulty;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import snownee.loquat.spawner.SpawnerLoader;
import snownee.lychee.fragment.Fragments;

public class DifficultyLoader extends SimpleJsonResourceReloadListener {
	public static final DifficultyLoader INSTANCE = new DifficultyLoader("loquat_difficulties");
	private final Map<String, Difficulty> difficulties = Maps.newHashMap();

	public DifficultyLoader(String dir) {
		super(SpawnerLoader.GSON, dir);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		difficulties.clear();
		map.forEach((id, json) -> {
			Fragments.INSTANCE.process(json);
			difficulties.put(id.getPath(), SpawnerLoader.GSON.fromJson(json, Difficulty.class));
		});
	}

	public Difficulty get(String difficultyId) {
		return difficulties.get(difficultyId);
	}

}
