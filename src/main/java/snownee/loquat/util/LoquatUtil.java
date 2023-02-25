package snownee.loquat.util;

import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TaskChainer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface LoquatUtil {

	static void runCommandSilently(ServerLevel world, String command) {
		world.getServer().getCommands().performPrefixedCommand(new SilentCommandSourceStack(world), command);
	}

	class SilentCommandSourceStack extends CommandSourceStack {

		public SilentCommandSourceStack(ServerLevel world) {
			super(world.getServer(), Vec3.atLowerCornerOf(world.getSharedSpawnPos()), Vec2.ZERO, world, 4, "Server", Component.literal("Server"), world.getServer(), null, true, (commandContext, bl, i) -> {
			}, EntityAnchorArgument.Anchor.FEET, CommandSigningContext.ANONYMOUS, TaskChainer.IMMEDIATE);
		}
	}

}
