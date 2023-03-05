package snownee.loquat.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;

import java.util.List;

public class TagCommand extends LoquatCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("tag")
				.then(Commands.literal("add")
						.then(Commands.argument("tag", StringArgumentType.string())
								.executes(ctx -> {
									var source = ctx.getSource();
									String tag = StringArgumentType.getString(ctx, "tag");
									List<Area> areas = Lists.newArrayList();
									int count = forEachSelected(source, (uuid, manager) -> {
										Area area = manager.get(uuid);
										area.getTags().add(tag);
										areas.add(area);
										return true;
									});
									AreaManager manager = AreaManager.of(source.getLevel());
									manager.setChanged(areas);
									source.sendSuccess(Component.translatable("loquat.command.tag.success", count), true);
									return count;
								})
						))
				.then(Commands.literal("remove")
						.then(Commands.argument("tag", StringArgumentType.string())
								.executes(ctx -> {
									var source = ctx.getSource();
									String tag = StringArgumentType.getString(ctx, "tag");
									List<Area> areas = Lists.newArrayList();
									int count = forEachSelected(source, (uuid, manager) -> {
										Area area = manager.get(uuid);
										if (area.getTags().remove(tag)) {
											areas.add(area);
											return true;
										} else {
											return false;
										}
									});
									AreaManager manager = AreaManager.of(source.getLevel());
									manager.setChanged(areas);
									source.sendSuccess(Component.translatable("loquat.command.tag.success", count), true);
									return count;
								})
						)
				);
	}

}
