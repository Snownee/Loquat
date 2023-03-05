package snownee.loquat.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import org.apache.commons.compress.utils.Lists;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.area.Zone;
import snownee.loquat.core.select.PosSelection;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.network.SSyncSelectionPacket;

import java.util.List;
import java.util.Optional;

public class ZoneCommand extends LoquatCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("zone")
				.then(Commands.literal("add")
						.executes(ctx -> {
							return addZone(ctx.getSource(), "0");
						})
						.then(Commands.argument("name", StringArgumentType.string())
								.executes(ctx -> {
									return addZone(ctx.getSource(), StringArgumentType.getString(ctx, "name"));
								})
						)
				)
				.then(Commands.literal("remove")
						.then(Commands.argument("name", StringArgumentType.string())
								.executes(ctx -> {
									return removeZone(ctx.getSource(), StringArgumentType.getString(ctx, "name"));
								})
						)
				);
	}

	private static int removeZone(CommandSourceStack source, String name) throws CommandSyntaxException {
		Area area = getOnlySelectedArea(source);
		Zone removed = area.getZones().remove(name);
		if (removed == null) {
			source.sendFailure(Component.translatable("loquat.command.zoneNotFound"));
			return 0;
		}
		AreaManager manager = AreaManager.of(source.getLevel());
		manager.setChanged(List.of(area));
		source.sendSuccess(Component.translatable("loquat.command.zone.remove.success"), true);
		return 1;
	}

	private static int addZone(CommandSourceStack source, String name) throws CommandSyntaxException {
		Area area = getOnlySelectedArea(source);
		var selections = SelectionManager.of(source.getPlayerOrException()).getSelections();
		if (selections.isEmpty()) {
			source.sendFailure(Component.translatable("loquat.command.emptySelection"));
			return 0;
		}
		List<AABB> aabbs = Lists.newArrayList();
		Optional.ofNullable(area.getZones().get(name)).ifPresent($ -> aabbs.addAll($.aabbs()));
		aabbs.addAll(selections.stream().map(PosSelection::toAABB).map($ -> $.getYsize() > 1 ? $ : $.move(0, 1, 0)).toList());
		area.getZones().put(name, new Zone(aabbs));
		AreaManager manager = AreaManager.of(source.getLevel());
		manager.setChanged(List.of(area));
		selections.clear();
		SSyncSelectionPacket.sync(source.getPlayerOrException());
		source.sendSuccess(Component.translatable("loquat.command.zone.add.success"), true);
		return 1;
	}

}
