package snownee.loquat.command;

import java.util.List;
import java.util.function.UnaryOperator;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.RestrictInstance;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;

public class NearbyCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("nearby")
				.executes(ctx -> {
					var source = ctx.getSource();
					var manager = AreaManager.of(source.getLevel());
					var selections = SelectionManager.of(source.getPlayerOrException());
					var areas = manager.areas().stream().toList(); //TODO filter?
					source.sendSystemMessage(Component.translatable("loquat.command.nearby.total", areas.size()));
					for (Area area : areas) {
						var center = area.getCenter();
						var component = Component.translatable("loquat.command.nearby.pos", (int) center.x, (int) center.y, (int) center.z)
								.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in %s run tp @s %.2f %.2f %.2f".formatted(source.getLevel().dimension().location(), center.x, center.y, center.z))));
						if (selections.getSelectedAreas().contains(area.getUuid())) {
							component = component.withStyle(ChatFormatting.GREEN);
						}
						component = component.append(" ").withStyle(ChatFormatting.RESET);
						component = component.append(Component.translatable("loquat.command.nearby.highlight")
								.withStyle(clickEvent("highlight", source, area))
						);
						component = component.append(" ");
						component = component.append(Component.translatable("loquat.command.nearby.more")
								.withStyle(clickEvent("info", source, area))
								.withStyle(style -> {
									List<MutableComponent> lines = Lists.newArrayList();
									if (!area.getTags().isEmpty()) {
										String tags = Joiner.on(", ").join(area.getTags());
										lines.add(Component.translatable("loquat.command.nearby.more.tags", tags));
									}
									if (area.getAttachedData() != null) {
										lines.add(Component.translatable("loquat.command.nearby.more.data", NbtUtils.toPrettyComponent(area.getAttachedData())));
									}
									printRestrictions(lines, manager.getFallbackRestriction(), area, "global");
									if (source.getPlayer() != null) {
										RestrictInstance restrictInstance = manager.getOrCreateRestrictInstance(source.getPlayer().getScoreboardName());
										printRestrictions(lines, restrictInstance, area, "player");
									}
									if (lines.isEmpty()) {
										return style;
									}
									return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, lines.stream().reduce((a, b) -> a.append("\n").append(b)).orElseGet(Component::empty)));
								})
						);
						component = component.append(" ");
						component = component.append(Component.translatable("loquat.command.nearby.select")
								.withStyle(clickEvent("select", source, area))
						);
						source.sendSystemMessage(component);
					}
					return areas.size();
				});
	}

	private static void printRestrictions(List<MutableComponent> lines, RestrictInstance restrictInstance, Area area, String key) {
		List<MutableComponent> behaviors = Lists.newArrayList();
		for (RestrictInstance.RestrictBehavior behavior : RestrictInstance.RestrictBehavior.VALUES) {
			if (restrictInstance.isRestricted(area, behavior)) {
				behaviors.add(behavior.getDisplayName().withStyle(ChatFormatting.RED));
			}
		}
		if (!behaviors.isEmpty()) {
			behaviors.stream().reduce((a, b) -> a.append(", ").append(b)).ifPresent(component -> {
				lines.add(Component.translatable("loquat.command.nearby.more.restrict." + key, component));
			});
		}
	}

	private static UnaryOperator<Style> clickEvent(String type, CommandSourceStack source, Area area) {
		return style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "@loquat %s %s %s".formatted(type, source.getLevel().dimension().location(), area.getUuid())));
	}

}
