package snownee.loquat.spawner;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;

public class Spawner {
	public Wave[] waves;
	public ResourceLocation difficulty = Difficulty.DEFAULT_ID;

	public static class Wave {
		private static final ContextualHolder EMPTY_CONDITIONS = new ContextualHolder();

		public float wait;
		public int timeout;
		public ContextualHolder contextual = EMPTY_CONDITIONS;
		public PostAction[] post;
	}

	public static void spawn(ResourceLocation spawnerId, @Nullable ResourceLocation difficultyId, ServerLevel world, Area area) {
		Spawner spawner = LycheeCompat.SPAWNERS.get(spawnerId);
		if (difficultyId == null) {
			difficultyId = spawner.difficulty;
		}
		Difficulty difficulty = LycheeCompat.DIFFICULTIES.get(difficultyId);
		AreaManager.of(world).addEvent(new SpawnMobAreaEvent(area, spawner, spawnerId, difficulty, difficultyId));
	}
}
