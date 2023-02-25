package snownee.loquat.spawner;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import snownee.loquat.Loquat;
import snownee.loquat.core.area.Area;
import snownee.loquat.util.CommonProxy;
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

	private final List<SpawnMobAction> pendingMobs = Lists.newArrayList();

	private int spawnCooldown;

	private final Set<UUID> mobs = Sets.newHashSet();

	private Consumer<Entity> deathListener;

	public ActiveWave(Spawner spawner, String spawnerId, int waveIndex, LycheeContext context) {
		this.spawner = spawner;
		this.spawnerId = spawnerId;
		this.waveIndex = waveIndex;
		this.wave = spawner.waves[waveIndex];
		this.context = context;
	}

	public boolean tick(ServerLevel world, Area area) {
		if (spawnCooldown > 0) {
			spawnCooldown--;
			return false;
		} else {
			pendingMobs.removeIf(action -> {
				Entity entity = action.getMob().createMob(world, area, action.getZone());
				if (entity != null) {
					mobs.add(entity.getUUID());
				}
				return entity != null;
			});
			if (!pendingMobs.isEmpty()) {
				spawnCooldown = 20;
			}
		}
		return isFinished();
	}

	public void addMob(SpawnMobAction action) {
		pendingMobs.add(action);
	}

	public void onStart() {
		deathListener = entity -> {
			if (mobs.remove(entity.getUUID())) {
				onKilled(entity);
			}
		};
		CommonProxy.registerDeathListener(deathListener);
	}

	public void onKilled(Entity entity) {
		if (isFinished()) {
			CommonProxy.unregisterDeathListener(deathListener);
			deathListener = null;
		}
	}

	public boolean isFinished() {
		return mobs.isEmpty() && pendingMobs.isEmpty() && context.runtime.state == ActionRuntime.State.STOPPED;
	}

	public int getRamainMobs() {
		return mobs.size() + pendingMobs.size();
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

	@Override
	public void applyPostActions(LycheeContext ctx, int times) {
		ctx.enqueueActions(getPostActions(), times, true);
	}

}
