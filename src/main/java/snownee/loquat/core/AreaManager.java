package snownee.loquat.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import snownee.loquat.Loquat;
import snownee.loquat.LoquatRegistries;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.area.Zone;
import snownee.loquat.network.SOutlinesPacket;

public class AreaManager extends SavedData {

	private final ArrayList<Area> areas = new ArrayList<>();
	private final HashMap<UUID, Area> map = new HashMap<>();
	@Getter
	private final Set<UUID> showOutlinePlayers = Sets.newHashSet();
	@Getter
	private final List<AreaEvent> events = new ArrayList<>();
	private ServerLevel level;
	private Set<Object> boundsCache = new HashSet<>();

	public static AreaManager of(ServerLevel level) {
		AreaManagerContainer container = (AreaManagerContainer) level;
		AreaManager manager = container.loquat$getAreaManager();
		if (manager == null) {
			manager = level.getDataStorage().computeIfAbsent(AreaManager::load, AreaManager::new, Loquat.ID);
			manager.level = level;
			container.loquat$setAreaManager(manager);
		}
		return manager;
	}

	public static AreaManager load(CompoundTag tag) {
		AreaManager manager = new AreaManager();
		loadAreas(tag.getList("Areas", Tag.TAG_COMPOUND)).forEach(manager::add);
		for (Tag t : tag.getList("Events", Tag.TAG_COMPOUND)) {
			try {
				AreaEvent event = AreaEvent.deserialize(manager, (CompoundTag) t);
				if (event != null) {
					manager.events.add(event);
				}
			} catch (Exception e) {
				Loquat.LOGGER.error("Failed to load area event", e);
			}
		}
		manager.setDirty(false);
		return manager;
	}

	@SuppressWarnings("rawtypes")
	public static ListTag saveAreas(Collection<Area> areas, boolean networking) {
		ListTag tag = new ListTag();
		for (Area area : areas) {
			CompoundTag data = new CompoundTag();
			if (area.getUuid() != null)
				data.putUUID("UUID", area.getUuid());
			if (!area.getTags().isEmpty())
				data.put("Tags", area.getTags().stream().map(StringTag::valueOf).collect(ListTag::new, ListTag::add, ListTag::add));
			if (!area.getZones().isEmpty()) {
				CompoundTag zones = new CompoundTag();
				area.getZones().forEach((name, zone) -> zones.put(name, zone.serialize(new CompoundTag())));
				data.put("Zones", zones);
			}
			data.putString("Type", LoquatRegistries.AREA.getKey(area.getType()).toString());
			((Area.Type) area.getType()).serialize(data, area, networking);
			tag.add(data);
		}
		return tag;
	}

	public static List<Area> loadAreas(ListTag tag) {
		List<Area> areas = new ArrayList<>(tag.size());
		for (int i = 0; i < tag.size(); i++) {
			CompoundTag data = tag.getCompound(i);
			Area.Type<?> type = LoquatRegistries.AREA.get(new ResourceLocation(data.getString("Type")));
			Area area = type.deserialize(data);
			if (data.contains("UUID")) {
				area.setUuid(data.getUUID("UUID"));
			}
			if (data.contains("Tags")) {
				ListTag tags = data.getList("Tags", Tag.TAG_STRING);
				for (int j = 0; j < tags.size(); j++) {
					area.getTags().add(tags.getString(j));
				}
			}
			if (data.contains("Zones")) {
				CompoundTag zones = data.getCompound("Zones");
				for (String name : zones.getAllKeys()) {
					area.getZones().put(name, Zone.deserialize(zones.getCompound(name)));
				}
			}
			areas.add(area);
		}
		return areas;
	}

	public void add(Area area) {
		Objects.requireNonNull(area.getUuid(), "Area UUID cannot be null");
		Preconditions.checkState(!map.containsKey(area.getUuid()), "Area UUID already exists: %s", area);
		Object bounds = area.getBounds();
		Preconditions.checkState(!boundsCache.contains(bounds), "Area already exists: same bounds");
		areas.add(area);
		map.put(area.getUuid(), area);
		boundsCache.add(bounds);
		showOutline(Long.MAX_VALUE, List.of(area));
		setDirty();
	}

	public boolean contains(Area area) {
		return areas.contains(area);
	}

	public Area get(UUID uuid) {
		return map.get(uuid);
	}

	public Stream<Area> byTag(String tag) {
		return areas.stream().filter(a -> a.getTags().contains(tag));
	}

	public boolean remove(UUID uuid) {
		Area area = map.remove(uuid);
		if (area == null) {
			return false;
		}
		areas.remove(area);
		boundsCache.remove(area.getBounds());
		showOutline(Long.MIN_VALUE, List.of(area));
		setDirty();
		return true;
	}

	public boolean removeAllInside(AABB aabb) {
		List<UUID> toRemove = new ArrayList<>();
		for (Area area : areas) {
			if (area.inside(aabb)) {
				toRemove.add(area.getUuid());
			}
		}
		toRemove.forEach(this::remove);
		return !toRemove.isEmpty();
	}

	public void showOutline(long duration, List<Area> areas) {
		if (level == null)
			return;
		showOutlinePlayers.stream().map(level::getEntity).filter(Objects::nonNull).forEach(player -> {
			SOutlinesPacket.outlines((ServerPlayer) player, duration, false, areas);
		});
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		tag.put("Areas", saveAreas(areas, false));
		ListTag eventList = new ListTag();
		for (AreaEvent event : events) {
			eventList.add(event.serialize(new CompoundTag()));
		}
		tag.put("Events", eventList);
		return tag;
	}

	public List<Area> areas() {
		return Collections.unmodifiableList(areas);
	}

	public void tick() {
		events.removeIf(event -> {
			try {
				event.tick(level);
				++event.ticksExisted;
				return event.isFinished();
			} catch (Exception e) {
				Loquat.LOGGER.error("Failed to tick area event", e);
				return true;
			}
		});
	}
}
