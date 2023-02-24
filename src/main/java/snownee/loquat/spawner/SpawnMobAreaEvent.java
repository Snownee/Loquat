package snownee.loquat.spawner;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.loquat.AreaEventTypes;
import snownee.loquat.LoquatConfig;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.core.area.Area;
import snownee.lychee.core.Job;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.Delay;

public class SpawnMobAreaEvent extends AreaEvent {
	private final String spawnerId;
	private final Spawner spawner;
	private int lastWave;
	private final List<ActiveWave> waves = Lists.newArrayList();

	public SpawnMobAreaEvent(Area area, Spawner spawner, String spawnerId) {
		super(area);
		Preconditions.checkNotNull(spawner, "Spawner %s not found", spawnerId);
		this.spawnerId = spawnerId;
		this.spawner = spawner;
	}

	@Override
	public void tick(ServerLevel world) {
		if (lastWave >= spawner.waves.length && waves.isEmpty()) {
			isFinished = true;
			return;
		}
		if (waves.isEmpty()) {
			newWave(world);
		}
		waves.removeIf(wave -> wave.tick(world, area));
	}

	public void newWave(ServerLevel world) {
		Preconditions.checkArgument(lastWave < spawner.waves.length, "No more wave");
		LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(world);
		builder.withParameter(LootContextParams.ORIGIN, area.getCenter());
		LycheeContext ctx = builder.create(LycheeCompat.LOOT_CONTEXT_PARAM_SET);
		ActiveWave activeWave = new ActiveWave(spawner, spawnerId, lastWave++, ctx);
		if (LoquatConfig.debug) {
			world.getServer().getCommands().performPrefixedCommand(world.getServer().createCommandSourceStack(), "tellraw @a \"Wave %d is coming!\"".formatted(lastWave));
		}
		if (activeWave.getWave().contextual.checkConditions(activeWave, ctx, 1) > 0) {
			waves.add(activeWave);
			activeWave.applyPostActions(ctx, 1);
			if (activeWave.getWave().wait > 0)
				ctx.runtime.jobs.push(new Job(new Delay(activeWave.getWave().wait), 1));
			ctx.runtime.run(activeWave, ctx, 1);
		}
	}

	@Override
	public AreaEvent.Type<?> getType() {
		return AreaEventTypes.SPAWN_MOB;
	}

	public static class Type extends AreaEvent.Type<SpawnMobAreaEvent> {
		@Override
		public SpawnMobAreaEvent deserialize(Area area, CompoundTag data) {
			String spawnerId = data.getString("Spawner");
			SpawnMobAreaEvent event = new SpawnMobAreaEvent(area, SpawnerLoader.INSTANCE.get(spawnerId), spawnerId);
			event.lastWave = data.getInt("LastWave");
			return event;
		}

		@Override
		public CompoundTag serialize(CompoundTag data, SpawnMobAreaEvent event) {
			data.putString("Spawner", event.spawnerId);
			data.putInt("LastWave", event.lastWave);
			return data;
		}
	}
}
