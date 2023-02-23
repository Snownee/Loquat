package snownee.loquat.spawner;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import snownee.loquat.core.area.Area;
import snownee.lychee.core.post.PostAction;

public class SpawnerLoader extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.setLenient()
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(PostAction.class, new PostActionSerializer())
			.create();
	private final Map<String, Spawner> spawners = Maps.newHashMap();
	public static final SpawnerLoader INSTANCE = new SpawnerLoader("loquat_spawners");

	public SpawnerLoader(String dir) {
		super(GSON, dir);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		spawners.clear();
		map.forEach((id, json) -> spawners.put(id.getPath(), GSON.fromJson(json, Spawner.class)));
	}

	public void spawn(String spawnerId, ServerLevel world, Area area, RandomSource random) {
		Spawner spawner = spawners.get(spawnerId);
		Preconditions.checkNotNull(spawner, "Spawner %s not found", spawnerId);
	}

	private static class PostActionSerializer implements JsonDeserializer<PostAction>, JsonSerializer<PostAction> {

		@Override
		public PostAction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			return PostAction.parse(jsonElement.getAsJsonObject());
		}

		@Override
		public JsonElement serialize(PostAction postAction, Type type, JsonSerializationContext jsonSerializationContext) {
			return postAction.toJson();
		}
	}
}
