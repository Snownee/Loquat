package snownee.loquat.command.argument;

import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;

public class AreaSelectorOptions {
	private static final Map<String, Option> OPTIONS = Maps.newHashMap();

	public interface Modifier {
		void handle(AreaSelectorParser parser) throws CommandSyntaxException;
	}

	record Option(Modifier modifier, Predicate<AreaSelectorParser> canUse, Component description) {
	}

	private static void register(String name, Modifier modifier, Predicate<AreaSelectorParser> canUse) {
		register(name, modifier, canUse, Component.translatable("loquat.argument.area.options.%s.description".formatted(name)));
	}

	public static void register(String name, Modifier modifier, Predicate<AreaSelectorParser> canUse, Component description) {
		OPTIONS.put(name, new Option(modifier, canUse, description));
	}

	public static Modifier get(AreaSelectorParser parser, String name, int cursor) throws CommandSyntaxException {
		Option option = OPTIONS.get(name);
		if (option != null) {
			if (option.canUse.test(parser)) {
				return option.modifier;
			} else {
				throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.createWithContext(parser.getReader(), name);
			}
		}
		parser.getReader().setCursor(cursor);
		throw EntitySelectorOptions.ERROR_UNKNOWN_OPTION.createWithContext(parser.getReader(), name);
	}

	public static void suggestNames(AreaSelectorParser parser, SuggestionsBuilder builder) {
		String s = builder.getRemaining().toLowerCase(Locale.ROOT);
		for (Map.Entry<String, Option> entry : OPTIONS.entrySet()) {
			if (entry.getValue().canUse.test(parser) && entry.getKey().toLowerCase(Locale.ROOT).startsWith(s)) {
				builder.suggest(entry.getKey() + "=", entry.getValue().description);
			}
		}
	}

	public static void bootstrap() {
		if (!OPTIONS.isEmpty()) {
			return;
		}
		register("tag", AreaSelectorParser::thenTag, p -> true);
//		register("type", AreaSelectorParser::thenType, p -> !p.isTypeLimited());
		register("limit", AreaSelectorParser::thenLimit, p -> !p.isLimited());
		register("distance", AreaSelectorParser::thenDistance, p -> p.getDistance().isAny());
		register("sort", AreaSelectorParser::thenSort, p -> !p.isSelectedAreas() && !p.isSorted());
		register("nbt", AreaSelectorParser::thenNBT, p -> true);
		register("x", p -> p.setX(p.getReader().readDouble()), p -> p.getX() == null);
		register("y", p -> p.setY(p.getReader().readDouble()), p -> p.getY() == null);
		register("z", p -> p.setZ(p.getReader().readDouble()), p -> p.getZ() == null);
		register("dx", p -> p.setDeltaX(p.getReader().readDouble()), p -> p.getDeltaX() == null);
		register("dy", p -> p.setDeltaY(p.getReader().readDouble()), p -> p.getDeltaY() == null);
		register("dz", p -> p.setDeltaZ(p.getReader().readDouble()), p -> p.getDeltaZ() == null);
	}
}
