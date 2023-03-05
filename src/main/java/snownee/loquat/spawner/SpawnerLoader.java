package snownee.loquat.spawner;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;

public class SpawnerLoader extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.setLenient()
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(PostAction.class, new PostActionSerializer())
			.registerTypeAdapter(ContextualHolder.class, new ContextualHolderSerializer())
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

	public void spawn(String spawnerId, ServerLevel world, Area area) {
		Spawner spawner = get(spawnerId);
		AreaManager.of(world).addEvent(new SpawnMobAreaEvent(area, spawner, spawnerId));
	}

	public Spawner get(String spawnerId) {
		return spawners.get(spawnerId);
	}

	private static class PostActionSerializer implements JsonDeserializer<PostAction> {
		@Override
		public PostAction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			return PostAction.parse(jsonElement.getAsJsonObject());
		}
	}

	private static class ContextualHolderSerializer implements JsonDeserializer<ContextualHolder> {
		@Override
		public ContextualHolder deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			ContextualHolder holder = new ContextualHolder();
			holder.parseConditions(jsonElement);
			return holder;
		}
	}
}
