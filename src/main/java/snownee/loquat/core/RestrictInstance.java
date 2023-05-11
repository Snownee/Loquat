package snownee.loquat.core;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import snownee.loquat.command.RestrictCommand;
import snownee.loquat.core.area.Area;
import snownee.loquat.duck.LoquatServerPlayer;

public class RestrictInstance {

	@Nullable
	@Setter
	private RestrictInstance fallback;
	@Nullable
	@Getter
	private Object2IntMap<Area> rules;

	public static RestrictInstance of(ServerPlayer player) {
		return ((LoquatServerPlayer) player).loquat$getRestrictionManager();
	}

	public static RestrictInstance of(ServerLevel level, String player) {
		var manager = AreaManager.of(level);
		if (player.equals("*")) {
			return manager.getFallbackRestriction();
		}
		return manager.getOrCreateRestrictInstance(player);
	}

	public void restrict(Area area, RestrictCommand.RestrictBehavior behavior, boolean restricted) {
		if (rules == null) {
			rules = new Object2IntOpenHashMap<>();
		}
		rules.computeInt(area, (k, v) -> {
			int flags = v == null ? 0 : v;
			if (restricted) {
				flags |= 1 << behavior.ordinal();
			} else {
				flags &= ~(1 << behavior.ordinal());
			}
			return flags == 0 ? null : flags;
		});
	}

	private int getFlags(Area area) {
		if (rules == null || !rules.containsKey(area)) {
			return fallback == null ? 0 : fallback.getFlags(area);
		}
		return rules.getInt(area);
	}

	public boolean isRestricted(Area area, RestrictCommand.RestrictBehavior behavior) {
		return (getFlags(area) & (1 << behavior.ordinal())) != 0;
	}

	public Optional<ListTag> serializeNBT() {
		if (rules == null || rules.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(AreaManager.saveAreas(rules.keySet(), true, (area, areaTag) -> {
			areaTag.putUUID("UUID", Objects.requireNonNull(area.getUuid()));
			areaTag.putInt("Flags", rules.getInt(area));
		}));
	}

	public void deserializeNBT(AreaManager manager, ListTag listTag) {
		if (rules == null) {
			rules = new Object2IntOpenHashMap<>(listTag.size());
		} else {
			rules.clear();
		}
		listTag.forEach(rawtag -> {
			CompoundTag tag = (CompoundTag) rawtag;
			Area area = manager.get(tag.getUUID("UUID"));
			if (area != null) {
				rules.put(area, tag.getInt("Flags"));
			}
		});
	}

	public void getAllRules(BiConsumer<Area, Integer> consumer) {
		Stream.concat(rules == null ? Stream.empty() : rules.keySet().stream(), fallback == null || fallback.rules == null ? Stream.empty() : fallback.rules.keySet().stream()).distinct().forEach(area -> {
			consumer.accept(area, getFlags(area));
		});
	}

	public void resetForClient() {
		if (rules == null) {
			rules = new Object2IntLinkedOpenHashMap<>();
		} else {
			rules.clear();
		}
	}
}
