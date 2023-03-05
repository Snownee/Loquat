package snownee.loquat;

import net.minecraft.server.level.ServerPlayer;
import snownee.loquat.core.area.Area;

public interface LoquatEvents {

	@FunctionalInterface
	interface PlayerEnterArea {
		void enterArea(ServerPlayer player, Area area);
	}

	@FunctionalInterface
	interface PlayerLeaveArea {
		void leaveArea(ServerPlayer player, Area area);
	}

}
