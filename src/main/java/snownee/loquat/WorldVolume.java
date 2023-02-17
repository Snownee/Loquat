package snownee.loquat;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.WeakHashMap;

public class WorldVolume {
	public static final HashMap<ResourceKey<Level>,VolumeManager> VOLUME_MANAGERS = new HashMap(3);

	public static VolumeManager getVolumeManager(ResourceKey<Level> level){
		return VOLUME_MANAGERS.computeIfAbsent(level,VolumeManager::new);
	}

}
