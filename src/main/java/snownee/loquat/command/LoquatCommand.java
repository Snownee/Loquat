package snownee.loquat.command;

import java.util.Objects;
import java.util.function.BiPredicate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import snownee.loquat.Loquat;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;

public class LoquatCommand {

	public static final SimpleCommandExceptionType EMPTY_SELECTION = new SimpleCommandExceptionType(Component.translatable("loquat.command.emptySelection"));
	public static final SimpleCommandExceptionType TOO_MANY_SELECTIONS = new SimpleCommandExceptionType(Component.translatable("loquat.command.tooManySelections"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(Loquat.ID).requires(cs -> cs.hasPermission(2))
				.then(CreateCommand.register())
				.then(DeleteCommand.register())
				.then(NearbyCommand.register())
				.then(OutlineCommand.register())
				.then(UnselectCommand.register())
				.then(ZoneCommand.register())
				.then(ReplaceCommand.register())
				.then(TagCommand.register())
				.then(EmptyCommand.register())
				.then(SelectTagCommand.register());
		if (Loquat.hasLychee) {
			builder.then(SpawnCommand.register());
		} else {
			builder.then(Commands.literal("spawn").requires(cs -> cs.hasPermission(2)).executes(ctx -> {
				ctx.getSource().sendFailure(Component.translatable("loquat.command.lycheeNotInstalled"));
				return 0;
			}));
		}
		dispatcher.register(builder);
	}

	public static Area getOnlySelectedArea(CommandSourceStack source) throws CommandSyntaxException {
		var selection = SelectionManager.of(source.getPlayerOrException());
		var manager = AreaManager.of(source.getLevel());
		var areas = selection.getSelectedAreas().stream().map(manager::get).filter(Objects::nonNull).toList();
		if (areas.isEmpty()) {
			throw EMPTY_SELECTION.create();
		}
		if (areas.size() > 1) {
			throw TOO_MANY_SELECTIONS.create();
		}
		return areas.get(0);
	}

	public static int forEachSelected(CommandSourceStack source, BiPredicate<Area, AreaManager> action) throws CommandSyntaxException {
		var selection = SelectionManager.of(source.getPlayerOrException());
		var manager = AreaManager.of(source.getLevel());
		var areas = selection.getSelectedAreas().stream().map(manager::get).filter(Objects::nonNull).toList();
		int count = 0;
		for (Area area : areas) {
			if (action.test(area, manager)) {
				count++;
			}
		}
		if (count == 0) {
			throw EMPTY_SELECTION.create();
		}
		return count;
	}
}
