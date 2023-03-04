package snownee.loquat.spawner;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.loquat.AreaEventTypes;
import snownee.loquat.LoquatConfig;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.core.area.Area;
import snownee.loquat.util.LoquatUtil;
import snownee.lychee.core.Job;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.Delay;

public class SpawnMobAreaEvent extends AreaEvent {
	private final String spawnerId;
	private final Spawner spawner;
	private int lastWave;
	private final List<ActiveWave> waves = Lists.newArrayList();

	private boolean hasTimeout;
	private long timeoutInTicks;

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
			if (LoquatConfig.debug) {
				LoquatUtil.runCommandSilently(world, "scoreboard objectives remove LoquatSpawner");
			}
			return;
		}
		if (waves.isEmpty() || (hasTimeout && world.getGameTime() >= timeoutInTicks)) {
			newWave(world);
		}
		waves.removeIf(wave -> wave.tick(world, area));
		if (LoquatConfig.debug) {
			LoquatUtil.runCommandSilently(world, "scoreboard players reset * LoquatSpawner");
			int mobs = waves.stream().mapToInt(ActiveWave::getRemainMobs).sum();
			LoquatUtil.runCommandSilently(world, "scoreboard players set mobs:%d LoquatSpawner 1".formatted(mobs));
			int timeout = hasTimeout ? (int) (timeoutInTicks - world.getGameTime()) / 20 : -1;
			LoquatUtil.runCommandSilently(world, "scoreboard players set timeout:%ds LoquatSpawner 2".formatted(timeout));
		}
	}

	public void newWave(ServerLevel world) {
		Preconditions.checkArgument(lastWave < spawner.waves.length, "No more wave");
		LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(world);
		builder.withParameter(LootContextParams.ORIGIN, area.getOrigin());
		LycheeContext ctx = builder.create(LycheeCompat.LOOT_CONTEXT_PARAM_SET);
		ActiveWave activeWave = new ActiveWave(spawner, spawnerId, lastWave++, ctx);
		//		if (LoquatConfig.debug) {
		//			world.getServer().getCommands().performPrefixedCommand(world.getServer().createCommandSourceStack(), "tellraw @a \"Wave %d is coming!\"".formatted(lastWave));
		//		}
		Spawner.Wave wave = activeWave.getWave();
		if (wave.contextual.checkConditions(activeWave, ctx, 1) > 0) {
			if (LoquatConfig.debug) {
				if (lastWave == 1) {
					LoquatUtil.runCommandSilently(world, "scoreboard objectives add LoquatSpawner dummy");
					LoquatUtil.runCommandSilently(world, "scoreboard objectives setdisplay sidebar LoquatSpawner");
				}
				LoquatUtil.runCommandSilently(world, "scoreboard objectives modify LoquatSpawner displayname \"Wave: %d/%d\"".formatted(lastWave, spawner.waves.length));
			}
			waves.add(activeWave);
			hasTimeout = wave.timeout > 0;
			if (hasTimeout)
				timeoutInTicks = LongMath.checkedAdd(world.getGameTime(), wave.timeout * 20);
			activeWave.onStart();
			activeWave.applyPostActions(ctx, 1);
			if (wave.wait > 0)
				ctx.runtime.jobs.push(new Job(new Delay(wave.wait), 1));
			ctx.runtime.run(activeWave, ctx);
		}
	}

	@Override
	public AreaEvent.Type<?> getType() {
		return AreaEventTypes.SPAWN_MOBS;
	}

	public static class Type extends AreaEvent.Type<SpawnMobAreaEvent> {
		@Override
		public SpawnMobAreaEvent deserialize(Area area, CompoundTag data) {
			String spawnerId = data.getString("Spawner");
			SpawnMobAreaEvent event = new SpawnMobAreaEvent(area, SpawnerLoader.INSTANCE.get(spawnerId), spawnerId);
			event.lastWave = data.getInt("LastWave");
			event.hasTimeout = data.getBoolean("HasTimeout");
			if (event.hasTimeout)
				event.timeoutInTicks = data.getLong("TimeoutInTicks");
			return event;
		}

		@Override
		public CompoundTag serialize(CompoundTag data, SpawnMobAreaEvent event) {
			data.putString("Spawner", event.spawnerId);
			data.putInt("LastWave", event.lastWave);
			data.putBoolean("HasTimeout", event.hasTimeout);
			if (event.hasTimeout)
				data.putLong("TimeoutInTicks", event.timeoutInTicks);
			return data;
		}
	}
}