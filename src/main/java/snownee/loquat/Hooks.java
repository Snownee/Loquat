package snownee.loquat;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.network.CRequestOutlinesPacket;
import snownee.loquat.network.CSelectAreaPacket;

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
}
