package snownee.loquat.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;

public class DeleteCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("delete")
				.then(Commands.argument("uuid", UuidArgument.uuid())
						.executes(ctx -> {
							var uuid = UuidArgument.getUuid(ctx, "uuid");
							var source = ctx.getSource();
							var manager = AreaManager.of(source.getLevel());
							if (manager.remove(uuid)) {
								source.sendSuccess(Component.translatable("loquat.command.remove.success"), true);
								return 1;
							} else {
								source.sendFailure(Component.translatable("loquat.command.areaNotFound"));
								return 0;
							}
						})
				);
	}

}
