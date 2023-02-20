package snownee.loquat.core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.http.util.Asserts;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import snownee.loquat.Loquat;
import snownee.loquat.client.LoquatClient;
import snownee.loquat.core.area.Area;

public class AreaManager extends SavedData {
	public static AreaManager of(ServerLevel level) {
		AreaManagerContainer container = (AreaManagerContainer) level;
		AreaManager manager = container.getAreaManager();
		if (manager == null) {
			manager = level.getDataStorage().computeIfAbsent(AreaManager::load, AreaManager::new, Loquat.ID);
			container.setAreaManager(manager);
		}
		return manager;
	}

	public static AreaManager load(CompoundTag tag) {
		AreaManager manager = new AreaManager();
		return manager;
	}

	private final ArrayList<Area> areas = new ArrayList<>();
	private final HashMap<UUID, Area> map = new HashMap<>();

	public void add(Area area) {
		Asserts.check(!areas.contains(area), "Area already exists");
		areas.add(area);
		map.put(area.getUuid(), area);
		setDirty();
		LoquatClient.renderDebugAreas.add(new LoquatClient.RenderDebugData(area, 0, Long.MAX_VALUE));
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
		setDirty();
		return true;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		return tag;
	}

	public List<Area> areas() {
		return Collections.unmodifiableList(areas);
	}
}
