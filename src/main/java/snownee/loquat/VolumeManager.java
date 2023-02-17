package snownee.loquat;


import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.apache.http.util.Asserts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@AllArgsConstructor
@EqualsAndHashCode
public class VolumeManager {
	public final ResourceKey<Level> level;
	private final ArrayList<Volume> volumes = new ArrayList<>();
	private final HashMap<UUID, Volume> map = new HashMap<>();

	public void appendVolume(Volume volume) {
		Asserts.check(!volumes.contains(volume), "can't add an already exist volume");
		volumes.add(volume);
		map.put(volume.getUuid(), volume);
	}

	public boolean contains(Volume volume) {
		return volumes.contains(volume);
	}

	public void renderDebug(PoseStack poseStack) {
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		for (var volume : volumes){
			volume.renderDebug(bufferBuilder,poseStack);
		}
	}

	public Volume getVolumeByUUID(UUID uuid) {
		return map.get(uuid);
	}


}
