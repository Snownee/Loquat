package snownee.loquat.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;
import snownee.loquat.util.LoquatUtil;

public class EmptyCommand extends LoquatCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("empty")
				.then(Commands.argument("uuid", UuidArgument.uuid())
						.executes(ctx -> {
							var uuid = UuidArgument.getUuid(ctx, "uuid");
							var source = ctx.getSource();
							var manager = AreaManager.of(source.getLevel());
							var area = manager.get(uuid);
							if (area == null) {
								source.sendFailure(Component.translatable("loquat.command.areaNotFound"));
								return 0;
							}
							LoquatUtil.emptyBlocks(source.getLevel(), area::allBlockPosIn);
							source.sendSuccess(() -> Component.translatable("loquat.command.empty.success"), true);
							return 1;
						})
				)
				.then(Commands.literal("selection")
						.executes(ctx -> {
							var source = ctx.getSource();
							int count = forEachSelected(source, (area, manager) -> {
								LoquatUtil.emptyBlocks(source.getLevel(), area::allBlockPosIn);
								return true;
							});
							source.sendSuccess(() -> Component.translatable("loquat.command.empty.success"), true);
							return count;
						})
				);
	}

}
