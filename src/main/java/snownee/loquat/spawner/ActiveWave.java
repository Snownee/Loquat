package snownee.loquat.spawner;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import snownee.loquat.Loquat;
import snownee.loquat.core.area.Area;
import snownee.lychee.core.ActionRuntime;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.json.JsonPointer;

public class ActiveWave implements ILycheeRecipe<LycheeContext> {

	private final String spawnerId;
	private final int waveIndex;
	@Getter
	private final Spawner spawner;
	@Getter
	private final Spawner.Wave wave;
	@Getter
	private final LycheeContext context;

	private final Queue<SpawnMobAction> pendingMobs = new ArrayDeque<>();

	public ActiveWave(Spawner spawner, String spawnerId, int waveIndex, LycheeContext context) {
		this.spawner = spawner;
		this.spawnerId = spawnerId;
		this.waveIndex = waveIndex;
		this.wave = spawner.waves[waveIndex];
		this.context = context;
	}

	@Override
	public ResourceLocation lychee$getId() {
		return new ResourceLocation(Loquat.ID, "spawner/%s/%d".formatted(spawnerId, waveIndex));
	}

	@Override
	public IntList getItemIndexes(JsonPointer jsonPointer) {
		return IntList.of();
	}

	@Override
	public Stream<PostAction> getPostActions() {
		return Stream.of(wave.post);
	}

	@Override
	public ContextualHolder getContextualHolder() {
		return wave.contextual;
	}

	@Override
	public @Nullable String getComment() {
		return null;
	}

	@Override
	public boolean showInRecipeViewer() {
		return false;
	}

	public boolean tick(ServerLevel world, Area area) {
		while (!pendingMobs.isEmpty()) {
			SpawnMobAction action = pendingMobs.peek();
			if (action.getMob().createMob(world, area, action.getZone())) {
				pendingMobs.poll();
			} else {
				pendingMobs.poll();
			}
		}
		return context.runtime.state == ActionRuntime.State.STOPPED;
	}

	public void addMob(SpawnMobAction action) {
		pendingMobs.add(action);
	}

	@Override
	public void applyPostActions(LycheeContext ctx, int times) {
		ctx.enqueueActions(getPostActions(), times, true);
	}
}
