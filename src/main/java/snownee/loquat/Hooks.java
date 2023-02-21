package snownee.loquat;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import snownee.loquat.network.CRequestHighlightPacket;

public final class Hooks {
	public static boolean handleComponentClicked(String value) {
		String[] s = StringUtils.split(value, ' ');
		if (s.length == 3 && s[0].equals("@loquat")) {
			if (s[1].equals("highlight")) {
				CRequestHighlightPacket.request(60, List.of(UUID.fromString(s[2])));
			} else if (s[1].equals("info")) {
				// TODO
			}
			return true;
		}
		return false;
	}
}
