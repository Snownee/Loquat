package snownee.loquat;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.network.CRequestOutlinesPacket;
import snownee.loquat.network.CSelectAreaPacket;
import snownee.loquat.util.TransformUtil;

public final class Hooks {
	public static boolean handleComponentClicked(String value) {
		String[] s = StringUtils.split(value, ' ');
		if (s.length == 3 && s[0].equals("@loquat")) {
			if (s[1].equals("highlight")) {
				CRequestOutlinesPacket.request(60, List.of(UUID.fromString(s[2])));
			} else if (s[1].equals("info")) {
				// TODO
			} else if (s[1].equals("select")) {
				Player player = Minecraft.getInstance().player;
				if (player != null) {
					SelectionManager manager = SelectionManager.of(player);
					UUID uuid = UUID.fromString(s[2]);
					CSelectAreaPacket.send(!manager.getSelectedAreas().contains(uuid), uuid);
				}
			}
			return true;
		}
		return false;
	}

	public static void fillFromWorld(AreaManager manager, BlockPos pos, Vec3i size, List<Area> areas) {
		AABB aabb = new AABB(pos, pos.offset(size));
		var settings = new StructurePlaceSettings();
		for (Area area : manager.areas()) {
			if (area.inside(aabb)) {
				area = TransformUtil.transform(settings, pos.multiply(-1), area);
				area.setUuid(null);
				areas.add(area);
			}
		}
	}

	public static void placeInWorld(AreaManager manager, BlockPos pos, BlockPos blockPos, List<Area> areas, StructurePlaceSettings settings, Vec3i size) {
		manager.removeAllInside(TransformUtil.transform(settings, pos, new AABB(pos, pos.offset(size))));
		for (Area area : areas) {
			area = TransformUtil.transform(settings, pos, area);
			if (settings.getBoundingBox() != null && !AABB.of(settings.getBoundingBox()).contains(area.getOrigin())) {
				continue;
			}
			// still not sure the difference between pos and blockPos...
			manager.add(area);
		}
	}
}
