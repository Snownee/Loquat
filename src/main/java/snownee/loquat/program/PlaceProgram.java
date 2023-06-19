package snownee.loquat.program;

import com.google.gson.JsonObject;

import net.minecraft.world.level.Level;
import snownee.loquat.core.area.Area;

public interface PlaceProgram {

	boolean place(Level level, Area area);

	abstract class Type<T extends PlaceProgram> {

		public abstract T create(JsonObject asJsonObject);

	}

}
