package snownee.loquat.util;

import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TaskChainer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;

public interface LoquatUtil {

	static int runCommandSilently(ServerLevel world, String command) {
		return world.getServer().getCommands().performPrefixedCommand(new SilentCommandSourceStack(world), command);
	}

	static void emptyArea(ServerLevel world, Area area) {
		area.allBlockPosIn().forEach(pos -> world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2));
		area.allBlockPosIn().forEach(pos -> world.blockUpdated(pos, Blocks.AIR));
		AreaManager.of(world).remove(area.getUuid());
	}

	class SilentCommandSourceStack extends CommandSourceStack {

		public SilentCommandSourceStack(ServerLevel world) {
			super(world.getServer(), Vec3.atLowerCornerOf(world.getSharedSpawnPos()), Vec2.ZERO, world, 4, "Server", Component.literal("Server"), world.getServer(), null, true, (commandContext, bl, i) -> {
			}, EntityAnchorArgument.Anchor.FEET, CommandSigningContext.ANONYMOUS, TaskChainer.IMMEDIATE);
		}
	}

}
