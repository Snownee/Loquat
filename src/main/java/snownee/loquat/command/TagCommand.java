package snownee.loquat.command;

import java.util.List;
import java.util.function.BiPredicate;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import snownee.loquat.command.argument.AreaArgument;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;

public class TagCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("tag")
				.then(Commands.argument("areas", AreaArgument.areas())
						.then(Commands.literal("add")
								.then(Commands.argument("tag", StringArgumentType.string())
										.executes(ctx -> forEachArea(ctx, (area, tag) -> area.getTags().add(tag)))
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("tag", StringArgumentType.string())
										.executes(ctx -> forEachArea(ctx, (area, tag) -> area.getTags().remove(tag)))
								)
						)
				);
	}

	private static int forEachArea(CommandContext<CommandSourceStack> ctx, BiPredicate<Area, String> func) throws CommandSyntaxException {
		var source = ctx.getSource();
		String tag = StringArgumentType.getString(ctx, "tag");
		List<Area> areas = Lists.newArrayList();
		for (Area area : AreaArgument.getAreas(ctx, "areas")) {
			if (func.test(area, tag)) {
				areas.add(area);
			}
		}
		AreaManager manager = AreaManager.of(source.getLevel());
		manager.setChanged(areas);
		return LoquatCommand.countedSuccess(ctx, "tag", areas.size());
	}

}
