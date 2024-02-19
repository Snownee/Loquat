package snownee.loquat.core;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import snownee.loquat.client.LoquatClient;
import snownee.loquat.core.area.Area;
import snownee.loquat.duck.LoquatServerPlayer;

public class RestrictInstance {

	@Nullable
	@Setter
	private RestrictInstance fallback;
	@Nullable
	@Getter
	private Object2IntMap<Area> rules;

	public static RestrictInstance of(Player player) {
		if (player instanceof LoquatServerPlayer) {
			return ((LoquatServerPlayer) player).loquat$getRestrictionInstance();
		} else if (player.level().isClientSide && Minecraft.getInstance().player == player) {
			return LoquatClient.get().restrictInstance;
		}
		throw new IllegalArgumentException("Unknown player type: " + player);
	}

	public static RestrictInstance of(ServerLevel level, String player) {
		var manager = AreaManager.of(level);
		if (player.equals("*")) {
			return manager.getFallbackRestriction();
		}
		return manager.getOrCreateRestrictInstance(player);
	}

	public void restrict(Area area, RestrictBehavior behavior, boolean restricted) {
		if (rules == null) {
			rules = new Object2IntLinkedOpenHashMap<>();
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

	public boolean isRestricted(Area area, RestrictBehavior behavior) {
		return (getFlags(area) & (1 << behavior.ordinal())) != 0;
	}

	public boolean isRestricted(BlockPos pos, RestrictBehavior behavior) {
		return areaStream().anyMatch(area -> {
			return isRestricted(area, RestrictInstance.RestrictBehavior.PLACE) && area.contains(pos);
		});
	}

	public Optional<ListTag> serializeNBT(AreaManager manager) {
		if (rules == null || rules.isEmpty()) {
			return Optional.empty();
		}
		ListTag listTag = new ListTag();
		rules.forEach((area, flags) -> {
			CompoundTag tag = new CompoundTag();
			tag.putUUID("UUID", area.getUuid());
			tag.putInt("Flags", flags);
			listTag.add(tag);
		});
		return Optional.of(listTag);
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

	public Stream<Area> areaStream() {
		return Stream.concat(rules == null ? Stream.empty() : rules.keySet().stream(), fallback == null || fallback.rules == null ? Stream.empty() : fallback.rules.keySet().stream()).distinct();
	}

	public void forEachRules(BiConsumer<Area, Integer> consumer) {
		areaStream().forEach(area -> {
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

	public boolean removeArea(Area area) {
		if (rules == null) {
			return false;
		}
		return rules.removeInt(area) != 0;
	}

	public boolean isEmpty() {
		return (rules == null || rules.isEmpty()) && (fallback == null || fallback.isEmpty());
	}

	public enum RestrictBehavior {
		ENTER("enter"),
		EXIT("exit"),
		DESTROY("destroy"),
		PLACE("place"),
		;

		public static final RestrictBehavior[] VALUES = values();
		public final String name;

		RestrictBehavior(String name) {
			this.name = name;
		}

		public MutableComponent getDisplayName() {
			return Component.translatable("loquat.restrict.behavior." + name);
		}

		public MutableComponent getNotificationMessage() {
			return Component.translatable("loquat.restrict.behavior." + name + ".notification");
		}
	}
}
