package snownee.loquat.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.level.ServerLevel;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.AreaManagerContainer;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements AreaManagerContainer {
	private AreaManager loquat$areaManager;

	@Override
	public AreaManager getAreaManager() {
		return loquat$areaManager;
	}

	@Override
	public void setAreaManager(AreaManager areaManager) {
		loquat$areaManager = areaManager;
	}
}
