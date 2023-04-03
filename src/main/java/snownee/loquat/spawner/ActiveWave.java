package snownee.loquat.spawner;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import snownee.loquat.Loquat;
import snownee.loquat.core.area.Area;
import snownee.loquat.spawner.difficulty.Difficulty;
import snownee.loquat.util.CommonProxy;
import snownee.lychee.core.ActionRuntime;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.json.JsonPointer;

public class ActiveWave implements ILycheeRecipe<LycheeContext> {

	private final String spawnerId;
	@Getter
	private final Spawner spawner;
	private final int waveIndex;
	@Getter
	private final Spawner.Wave wave;
	@Getter
	private final LycheeContext context;

	private final ObjectArrayList<SpawnMobAction> pendingMobs = ObjectArrayList.of();
	private final Set<UUID> mobs = Sets.newHashSet();
	private final Set<UUID> successiveSpawnableMobs = Sets.newHashSet();
	private boolean pendingMobsNeedShuffle;
	private int spawnCooldown;
	private Consumer<Entity> deathListener;
	private BiConsumer<Entity, Entity> successiveSpawnListener;
	private boolean isFinished;
	private int successiveSpawnCooldown;
	private int proactiveCheckCooldown;
	private final Difficulty.DifficultyLevel difficulty;

	public ActiveWave(Spawner spawner, String spawnerId, int waveIndex, LycheeContext context, Difficulty.DifficultyLevel difficulty) {
		this.spawner = spawner;
		this.spawnerId = spawnerId;
		this.waveIndex = waveIndex;
		this.wave = spawner.waves[waveIndex];
		this.context = context;
		this.difficulty = difficulty;
	}

	public static boolean canSuccessiveSpawn(LivingEntity entity) {
		return entity instanceof Slime;
	}

	public boolean tick(ServerLevel world, Area area) {
		if (successiveSpawnCooldown > 0 && --successiveSpawnCooldown == 0) {
			checkIfFinished();
			if (isFinished) {
				return true;
			}
		}
		if (context.runtime.state == ActionRuntime.State.STOPPED && --proactiveCheckCooldown <= 0) {
			mobs.removeIf(uuid -> world.getEntity(uuid) == null);
			checkIfFinished();
			proactiveCheckCooldown = 60;
		}
		if (spawnCooldown > 0) {
			spawnCooldown--;
			return false;
		} else {
			if (pendingMobsNeedShuffle) {
				pendingMobsNeedShuffle = false;
				Util.shuffle(pendingMobs, world.random);
			}
			pendingMobs.removeIf(action -> {
				List<ServerPlayer> players = world.players();
				int offset = world.random.nextInt(players.size());
				MutableObject<ServerPlayer> target = new MutableObject<>();
				for (int i = 0; i < players.size(); i++) {
					ServerPlayer player = players.get((i + offset) % players.size());
					if (!player.isSpectator() && area.contains(player.getBoundingBox())) {
						target.setValue(player);
						break;
					}
				}
				Entity entity = action.getMob().createMob(world, area, action.getZone(), e -> {
					if (target.getValue() == null) {
						return;
					}
					e.lookAt(EntityAnchorArgument.Anchor.EYES, target.getValue().position());
					if (e instanceof Monster monster) {
						monster.setTarget(target.getValue());
					}
				});
				if (entity != null) {
					addMob(entity);
					if (entity.isVehicle()) {
						entity.getIndirectPassengers().forEach(this::addMob);
					}
				}
				return entity != null;
			});
			if (!pendingMobs.isEmpty()) {
				spawnCooldown = 20;
			}
		}
		if (isFinished) {
			CommonProxy.unregisterDeathListener(deathListener);
			deathListener = null;
			if (successiveSpawnListener != null) {
				CommonProxy.unregisterSuccessiveSpawnListener(successiveSpawnListener);
				successiveSpawnListener = null;
			}
		}
		return isFinished;
	}

	public float getMobAmountMultiplier() {
		return difficulty.amount;
	}

	public void addPendingMob(SpawnMobAction action) {
		pendingMobsNeedShuffle = true;
		pendingMobs.add(action);
	}

	public void addMob(Entity entity) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		living = difficulty.apply(living);
		mobs.add(living.getUUID());
		if (canSuccessiveSpawn(living)) {
			successiveSpawnableMobs.add(living.getUUID());
			if (successiveSpawnListener == null) {
				successiveSpawnListener = (e, newEntity) -> {
					if (successiveSpawnableMobs.contains(e.getUUID())) {
						addMob(newEntity);
					}
				};
				CommonProxy.registerSuccessiveSpawnListener(successiveSpawnListener);
			}
		}
	}

	public void onStart() {
		deathListener = entity -> {
			if (mobs.remove(entity.getUUID())) {
				if (successiveSpawnableMobs.contains(entity.getUUID())) {
					successiveSpawnCooldown = 30;
				}
				onKilled(entity);
			}
		};
		CommonProxy.registerDeathListener(deathListener);
	}

	public void onKilled(Entity entity) {
		checkIfFinished();
	}

	public void checkIfFinished() {
		if (isFinished)
			return;
		isFinished = mobs.isEmpty() && pendingMobs.isEmpty() && context.runtime.state == ActionRuntime.State.STOPPED && successiveSpawnCooldown <= 0;
	}

	public int getRemainMobs() {
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
