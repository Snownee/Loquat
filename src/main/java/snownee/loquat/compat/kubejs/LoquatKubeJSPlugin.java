package snownee.loquat.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import snownee.loquat.Loquat;
import snownee.loquat.placement.LoquatPlacements;
import snownee.loquat.placement.tree.TreeNodePlacer;

public class LoquatKubeJSPlugin extends KubeJSPlugin {

	@Override
	public void init() {
		Loquat.LOGGER.info("KubeJS detected, loading Loquat KubeJS plugin");
	}

	@Override
	public void registerBindings(BindingsEvent event) {
		event.add("LoquatPlacements", LoquatPlacements.class);
		event.add("LoquatTreeNodePlacer", TreeNodePlacer.class);
	}

}
