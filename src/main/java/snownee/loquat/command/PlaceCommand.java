package snownee.loquat.command;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;
import snownee.loquat.program.PlaceProgram;
import snownee.loquat.program.PlaceProgramLoader;

public class PlaceCommand extends LoquatCommand {

	public static final SimpleCommandExceptionType PROGRAM_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("loquat.command.programNotFound"));

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("place")
				.then(Commands.argument("uuid", UuidArgument.uuid())
						.then(Commands.argument("program", StringArgumentType.string())
								.executes(ctx -> {
									var uuid = UuidArgument.getUuid(ctx, "uuid");
									var source = ctx.getSource();
									var manager = AreaManager.of(source.getLevel());
									var area = manager.get(uuid);
									if (area == null) {
										source.sendFailure(Component.translatable("loquat.command.areaNotFound"));
										return 0;
									}
									var program = getProgram(ctx);
									program.place(source.getLevel(), area);
									source.sendSuccess(Component.translatable("loquat.command.place.success"), true);
									return 1;
								})
						)
				)
				.then(Commands.literal("selection")
						.then(Commands.argument("program", ResourceLocationArgument.id())
								.executes(ctx -> {
									var source = ctx.getSource();
									var program = getProgram(ctx);
									int count = forEachSelected(source, (area, manager) -> {
										return program.place(source.getLevel(), area);
									});
									source.sendSuccess(Component.translatable("loquat.command.place.success"), true);
									return count;
								})
						)
				);
	}

	public static PlaceProgram getProgram(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		try {
			PlaceProgram program = PlaceProgramLoader.INSTANCE.get(ResourceLocationArgument.getId(ctx, "program"));
			Preconditions.checkNotNull(program);
			return program;
		} catch (Exception e) {
			throw PROGRAM_NOT_FOUND.create();
		}
	}

}
