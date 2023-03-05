package snownee.loquat.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import snownee.loquat.duck.AreaManagerContainer;
import snownee.loquat.network.SOutlinesPacket;

public class AreaManager extends SavedData {

	private final List<Area> areas = ObjectArrayList.of();
	private final Map<UUID, Area> map = Maps.newHashMap();
	@Getter
	private final Set<UUID> showOutlinePlayers = Sets.newHashSet();
	private final List<AreaEvent> events = ObjectArrayList.of();
	private final List<AreaEvent> pendingEvents = ObjectArrayList.of();
	private final Set<Object> boundsCache = Sets.newHashSet();
	private final Long2ObjectOpenHashMap<Set<Area>> chunkLookup = new Long2ObjectOpenHashMap<>();
	private ServerLevel level;
	private boolean ticking;

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
		area.getChunksIn().forEach(chunk -> {
			chunkLookup.computeIfAbsent(chunk, c -> Sets.newHashSet()).add(area);
		});
		setChanged(List.of(area));
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

	public Stream<Area> byChunk(long chunkPos) {
		return chunkLookup.getOrDefault(chunkPos, Set.of()).stream();
	}

	public boolean remove(UUID uuid) {
		Area area = map.remove(uuid);
		if (area == null) {
			return false;
		}
		areas.remove(area);
		boundsCache.remove(area.getBounds());
		area.getChunksIn().forEach(chunk -> {
			Set<Area> areas = chunkLookup.get(chunk);
			if (areas != null) {
				areas.remove(area);
//				if (areas.isEmpty()) {
//					chunkLookup.remove(chunk);
//				}
			}
		});
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

	public void showOutline(long duration, Collection<Area> areas) {
		if (level == null)
			return;
		showOutlinePlayers.stream().map(level::getEntity).filter(Objects::nonNull).forEach(player -> {
			SOutlinesPacket.outlines((ServerPlayer) player, duration, false, areas);
		});
	}

	public void setChanged(Collection<Area> areas) {
		setDirty();
		showOutline(Long.MAX_VALUE, areas);
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

	public List<AreaEvent> events() {
		return Collections.unmodifiableList(events);
	}

	public void addEvent(AreaEvent event) {
		if (ticking) {
			pendingEvents.add(event);
			return;
		}
		events.add(event);
		setDirty();
	}

	public void tick() {
		ticking = true;
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
		ticking = false;
		if (!pendingEvents.isEmpty()) {
			events.addAll(pendingEvents);
			pendingEvents.clear();
			setDirty();
		}
	}
}
