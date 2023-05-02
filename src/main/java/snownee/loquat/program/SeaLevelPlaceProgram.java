package snownee.loquat.program;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.loquat.core.area.Area;

public class SeaLevelPlaceProgram implements PlaceProgram {

	public static final SeaLevelPlaceProgram INSTANCE = new SeaLevelPlaceProgram();

	public boolean place(Level level, Area area) {
		int seaLevel = level.getSeaLevel();
		BlockState water = Blocks.WATER.defaultBlockState();
		area.allBlockPosIn().filter(pos -> pos.getY() <= seaLevel).forEach(pos -> {
			level.setBlock(pos, water, 2);
		});
		return true;
	}

	public static class Type extends PlaceProgram.Type<SeaLevelPlaceProgram> {

		public SeaLevelPlaceProgram create() {
			return INSTANCE;
		}

	}

}
