package snownee.loquat.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraftforge.fml.loading.LoadingModList;

public class MixinPlugin implements IMixinConfigPlugin {

	private boolean hasKubeJS;
	private boolean hasCanary;

	private static boolean hasMod(String modid) {
		return LoadingModList.get().getModFileById(modid) != null;
	}

	@Override
	public void onLoad(String mixinPackage) {
		hasKubeJS = hasMod("kubejs");
		hasCanary = hasMod("canary");
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.startsWith("snownee.loquat.mixin.kubejs.")) {
			return hasKubeJS;
		}
		if (mixinClassName.startsWith("snownee.loquat.mixin.canary.")) {
			return hasCanary;
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

}
