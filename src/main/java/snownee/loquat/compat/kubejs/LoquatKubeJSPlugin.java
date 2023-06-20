package snownee.loquat.compat.kubejs;

import java.util.Objects;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import snownee.loquat.Loquat;
import snownee.loquat.core.AreaManager;
import snownee.loquat.placement.LoquatPlacements;
import snownee.loquat.placement.tree.TreeNode;
import snownee.loquat.placement.tree.TreeNodePlacer;
import snownee.loquat.util.CommonProxy;

public class LoquatKubeJSPlugin extends KubeJSPlugin {

	@Override
	public void init() {
		Loquat.LOGGER.info("KubeJS detected, loading Loquat KubeJS plugin");
		CommonProxy.registerPlayerEnterAreaListener((player, area) -> {
			LoquatKubeJSEvents.AREA_ENTERED.post(Objects.requireNonNull(area.getUuid()).toString(), new PlayerAreaEventJS(player, area));
		});
		CommonProxy.registerPlayerLeaveAreaListener((player, area) -> {
			LoquatKubeJSEvents.AREA_LEFT.post(Objects.requireNonNull(area.getUuid()).toString(), new PlayerAreaEventJS(player, area));
		});
	}

	@Override
	public void registerBindings(BindingsEvent event) {
		event.add("LoquatPlacements", LoquatPlacements.class);
		event.add("LoquatTreeNodePlacer", TreeNodePlacer.class);
		event.add("LoquatAreaManager", AreaManager.class);
		event.add("LoquatTreeNode", TreeNode.class);
	}

	@Override
	public void registerEvents() {
		LoquatKubeJSEvents.GROUP.register();
	}
}
