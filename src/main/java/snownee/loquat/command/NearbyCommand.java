package snownee.loquat.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;

public class NearbyCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("nearby")
				.executes(ctx -> {
					var source = ctx.getSource();
					var manager = AreaManager.of(source.getLevel());
					var areas = manager.areas().stream().toList(); //TODO filter?
					source.sendSystemMessage(Component.translatable("loquat.command.nearby.total", areas.size()));
					for (Area area : areas) {
						var center = area.getCenter();
						var component = Component.translatable("loquat.command.nearby.pos", (int) center.x, (int) center.y, (int) center.z)
								.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s " + center.x + " " + center.y + " " + center.z)));
						component = component.append(" ");
						component = component.append(Component.translatable("loquat.command.nearby.highlight")
								.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "@loquat highlight " + area.getUuid())))
						);
						component = component.append(" ");
						component = component.append(Component.translatable("loquat.command.nearby.more")
								.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "@loquat info " + area.getUuid())))
						);
						component = component.append(" ");
						//TODO
						source.sendSystemMessage(component);
					}
					return areas.size();
				});
	}

}
