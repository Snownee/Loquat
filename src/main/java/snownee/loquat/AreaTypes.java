package snownee.loquat;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoadingContext;
import snownee.loquat.core.area.AABBArea;
import snownee.loquat.core.area.Area;

public class AreaTypes {

	public static void init() {
	}

	public static final AABBArea.Type BOX = register("box", new AABBArea.Type());

	public static <T extends Area.Type<?>> T register(String name, T t) {
		ModLoadingContext.get().setActiveContainer(null); // bypass Forge warning
		LoquatRegistries.AREA.register(new ResourceLocation(name), t);
		return t;
	}
}
