package snownee.loquat.spawner;

import java.lang.reflect.Type;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

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
import snownee.loquat.spawner.difficulty.Difficulty;
import snownee.loquat.spawner.difficulty.DifficultyLoader;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.fragment.Fragments;

public class SpawnerLoader extends SimpleJsonResourceReloadListener {
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.setLenient()
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(PostAction.class, new PostActionSerializer())
			.registerTypeAdapter(ContextualHolder.class, new ContextualHolderSerializer())
			.registerTypeAdapter(Difficulty.Provider.class, new Difficulty.DifficultyProviderSerializer())
			.create();
	public static final SpawnerLoader INSTANCE = new SpawnerLoader("loquat_spawners");
	private final Map<ResourceLocation, Spawner> spawners = Maps.newHashMap();

	public SpawnerLoader(String dir) {
		super(GSON, dir);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		spawners.clear();
		map.forEach((id, json) -> {
			Fragments.INSTANCE.process(json);
			spawners.put(id, GSON.fromJson(json, Spawner.class));
		});
	}

	public void spawn(ResourceLocation spawnerId, @Nullable String difficultyId, ServerLevel world, Area area) {
		Spawner spawner = get(spawnerId);
		if (difficultyId == null) {
			difficultyId = spawner.difficulty;
		}
		Difficulty difficulty = DifficultyLoader.INSTANCE.get(difficultyId);
		AreaManager.of(world).addEvent(new SpawnMobAreaEvent(area, spawner, spawnerId, difficulty, difficultyId));
	}

	public Spawner get(ResourceLocation spawnerId) {
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
