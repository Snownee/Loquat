package snownee.loquat.command;

import java.util.UUID;

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

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(Loquat.ID).requires(cs -> cs.hasPermission(2))
				.then(CreateCommand.register())
				.then(DeleteCommand.register())
				.then(NearbyCommand.register())
				.then(OutlineCommand.register())
				.then(UnselectCommand.register())
				.then(ZoneCommand.register())
				.then(ReplaceCommand.register());
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

	public static final SimpleCommandExceptionType EMPTY_SELECTION = new SimpleCommandExceptionType(Component.translatable("loquat.command.emptySelection"));
	public static final SimpleCommandExceptionType TOO_MANY_SELECTIONS = new SimpleCommandExceptionType(Component.translatable("loquat.command.tooManySelections"));

	public static Area getOnlySelectedArea(CommandSourceStack source) throws CommandSyntaxException {
		var selection = SelectionManager.of(source.getPlayerOrException());
		if (selection.getSelectedAreas().isEmpty()) {
			throw EMPTY_SELECTION.create();
		}
		SelectionManager.removeInvalidAreas(source.getPlayerOrException());
		if (selection.getSelectedAreas().size() > 1) {
			throw TOO_MANY_SELECTIONS.create();
		}
		UUID uuid = selection.getSelectedAreas().get(0);
		return AreaManager.of(source.getLevel()).get(uuid);
	}
}
