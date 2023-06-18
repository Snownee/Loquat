package snownee.loquat.mixin.kubejs;

import org.spongepowered.asm.mixin.Mixin;

import dev.latvian.mods.kubejs.core.WithPersistentData;
import net.minecraft.nbt.CompoundTag;
import snownee.loquat.core.area.Area;

@Mixin(Area.class)
public class AreaKubeJSMixin implements WithPersistentData {
	@Override
	public CompoundTag kjs$getPersistentData() {
		return ((Area) (Object) this).getOrCreateAttachedData();
	}
}
