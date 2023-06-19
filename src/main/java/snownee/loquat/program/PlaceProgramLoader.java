package snownee.loquat.program;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import snownee.loquat.LoquatRegistries;
import snownee.loquat.spawner.SpawnerLoader;

public class PlaceProgramLoader extends SimpleJsonResourceReloadListener {
	public static final PlaceProgramLoader INSTANCE = new PlaceProgramLoader("loquat_place_programs");
	private final Map<ResourceLocation, PlaceProgram> programs = Maps.newHashMap();

	public PlaceProgramLoader(String dir) {
		super(SpawnerLoader.GSON, dir);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		programs.clear();
		map.forEach((id, json) -> {
			ResourceLocation type = ResourceLocation.tryParse(json.getAsJsonObject().get("type").getAsString());
			PlaceProgram program = LoquatRegistries.PLACE_PROGRAM.getOptional(type).orElseThrow().create(json.getAsJsonObject());
			programs.put(id, program);
		});
	}

	public PlaceProgram get(ResourceLocation programId) {
		return programs.get(programId);
	}

}
