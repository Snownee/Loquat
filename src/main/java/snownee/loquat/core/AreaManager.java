package snownee.loquat.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.http.util.Asserts;

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
import snownee.loquat.Loquat;
import snownee.loquat.LoquatRegistries;
import snownee.loquat.core.area.Area;
import snownee.loquat.network.SOutlinesPacket;

public class AreaManager extends SavedData {

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
		manager.setDirty(false);
		return manager;
	}

	private ServerLevel level;
	private final ArrayList<Area> areas = new ArrayList<>();
	private final HashMap<UUID, Area> map = new HashMap<>();
	@Getter
	private final Set<UUID> showOutlinePlayers = Sets.newHashSet();

	@SuppressWarnings("rawtypes")
	public static ListTag saveAreas(Collection<Area> areas, boolean networking) {
		ListTag tag = new ListTag();
		for (Area area : areas) {
			CompoundTag data = new CompoundTag();
			data.putUUID("UUID", area.getUuid());
			if (!area.getTags().isEmpty())
				data.put("Tags", area.getTags().stream().map(StringTag::valueOf).collect(ListTag::new, ListTag::add, ListTag::add));
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
			area.setUuid(data.getUUID("UUID"));
			if (data.contains("Tags")) {
				ListTag tags = data.getList("Tags", Tag.TAG_STRING);
				for (int j = 0; j < tags.size(); j++) {
					area.getTags().add(tags.getString(j));
				}
			}
			areas.add(area);
		}
		return areas;
	}

	public void add(Area area) {
		Objects.requireNonNull(area.getUuid(), "Area UUID cannot be null");
		Asserts.check(!areas.contains(area), "Area already exists");
		areas.add(area);
		map.put(area.getUuid(), area);
		if (level != null) {
			showOutlinePlayers.stream().map(level::getEntity).filter(Objects::nonNull).forEach(player -> {
				SOutlinesPacket.outlines((ServerPlayer) player, Long.MAX_VALUE, false, List.of(area));
			});
		}
		setDirty();
	}

	public boolean contains(Area area) {
		return areas.contains(area);
	}

	public Area get(UUID uuid) {
		return map.get(uuid);
	}

	public boolean remove(UUID uuid) {
		Area area = map.remove(uuid);
		if (area == null) {
			return false;
		}
		areas.remove(area);
		showOutlinePlayers.stream().map(level::getEntity).filter(Objects::nonNull).forEach(player -> {
			SOutlinesPacket.outlines((ServerPlayer) player, Long.MIN_VALUE, false, List.of(area));
		});
		setDirty();
		return true;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		tag.put("Areas", saveAreas(areas, false));
		return tag;
	}

	public List<Area> areas() {
		return Collections.unmodifiableList(areas);
	}
}
