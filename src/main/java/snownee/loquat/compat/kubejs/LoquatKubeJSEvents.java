package snownee.loquat.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface LoquatKubeJSEvents {
	EventGroup GROUP = EventGroup.of("LoquatEvents");

	EventHandler AREA_ENTERED = GROUP.server("playerEnteredArea", () -> PlayerAreaEventJS.class);
	EventHandler AREA_LEFT = GROUP.server("playerLeftArea", () -> PlayerAreaEventJS.class);
}
