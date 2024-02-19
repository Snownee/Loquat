package snownee.loquat.command.argument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import snownee.loquat.core.area.Area;

public class AreaSelectorParser {
	public static final SimpleCommandExceptionType ERROR_INVALID_NAME_OR_UUID = new SimpleCommandExceptionType(Component.translatable(
			"loquat.argument.area.invalid"));
	@Getter
	private final StringReader reader;
	@Setter
	private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = EntitySelectorParser.SUGGEST_NOTHING;
	private Predicate<Area> predicate = area -> true;
	@Nullable
	private Area.Type<?> type;
	@Getter
	private boolean limited;
	@Getter
	private int maxResults;
	@Getter
	private boolean sorted;
	@Getter
	private MinMaxBounds.Doubles distance = MinMaxBounds.Doubles.ANY;
	@Nullable
	private UUID uuid;
	private BiConsumer<Vec3, List<? extends Area>> order = AreaSelector.ORDER_ARBITRARY;
	@Getter
	private boolean selectedAreas;
	@Nullable
	@Getter
	@Setter
	private Double x;
	@Nullable
	@Getter
	@Setter
	private Double y;
	@Nullable
	@Getter
	@Setter
	private Double z;
	@Nullable
	@Getter
	@Setter
	private Double deltaX;
	@Nullable
	@Getter
	@Setter
	private Double deltaY;
	@Nullable
	@Getter
	@Setter
	private Double deltaZ;

	public AreaSelectorParser(StringReader reader) {
		this.reader = reader;
	}

	public AreaSelector getSelector() {
		AABB aabb;
		if (this.deltaX == null && this.deltaY == null && this.deltaZ == null) {
			if (this.distance.getMax() != null) {
				double d0 = this.distance.getMax();
				aabb = new AABB(-d0, -d0, -d0, d0 + 1.0, d0 + 1.0, d0 + 1.0);
			} else {
				aabb = null;
			}
		} else {
			aabb = this.createAabb(
					this.deltaX == null ? 0.0 : this.deltaX,
					this.deltaY == null ? 0.0 : this.deltaY,
					this.deltaZ == null ? 0.0 : this.deltaZ);
		}

		UnaryOperator<Vec3> position;
		if (this.x == null && this.y == null && this.z == null) {
			position = UnaryOperator.identity();
		} else {
			double d0 = this.x == null ? 0.0 : this.x;
			double d1 = this.y == null ? 0.0 : this.y;
			double d2 = this.z == null ? 0.0 : this.z;
			position = vec3 -> new Vec3(this.x == null ? vec3.x : d0, this.y == null ? vec3.y : d1, this.z == null ? vec3.z : d2);
		}

		return new AreaSelector(
				maxResults,
				predicate,
				distance,
				position,
				aabb,
				order,
				selectedAreas,
				uuid
		);
	}

	private AABB createAabb(double dx, double dy, double dz) {
		boolean flag = dx < 0.0;
		boolean flag1 = dy < 0.0;
		boolean flag2 = dz < 0.0;
		double d0 = flag ? dx : 0.0;
		double d1 = flag1 ? dy : 0.0;
		double d2 = flag2 ? dz : 0.0;
		double d3 = (flag ? 0.0 : dx) + 1.0;
		double d4 = (flag1 ? 0.0 : dy) + 1.0;
		double d5 = (flag2 ? 0.0 : dz) + 1.0;
		return new AABB(d0, d1, d2, d3, d4, d5);
	}

	public AreaSelector parse() throws CommandSyntaxException {
		this.suggestions = this::suggestStart;
		if (this.reader.canRead() && this.reader.peek() == '@') {
			this.reader.skip();
			this.parseSelector();
		} else {
			this.parseNameOrUUID();
		}

		this.finalizePredicates();
		return this.getSelector();
	}

	public void finalizePredicates() {
	}

	protected void parseSelector() throws CommandSyntaxException {
		this.suggestions = this::suggestSelector;
		if (!this.reader.canRead()) {
			throw EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE.createWithContext(this.reader);
		} else {
			this.suggestions = this::suggestSelector;
			int i = this.reader.getCursor();
			char ch = this.reader.read();
			if (ch == 'p') {
				this.maxResults = 1;
				this.order = AreaSelector.ORDER_NEAREST;
			} else if (ch == 'a') {
				this.maxResults = Integer.MAX_VALUE;
				this.order = AreaSelector.ORDER_ARBITRARY;
			} else if (ch == 'r') {
				this.maxResults = 1;
				this.order = AreaSelector.ORDER_RANDOM;
			} else if (ch == 's') {
				this.maxResults = Integer.MAX_VALUE;
				this.selectedAreas = true;
			} else {
				this.reader.setCursor(i);
				throw EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(this.reader, "@" + String.valueOf(ch));
			}

			this.suggestions = this::suggestOpenOptions;
			if (this.reader.canRead() && this.reader.peek() == '[') {
				this.reader.skip();
				this.suggestions = this::suggestOptionsKeyOrClose;
				this.parseOptions();
			}
		}
	}

	protected void parseNameOrUUID() throws CommandSyntaxException {
		int i = this.reader.getCursor();
		String s = this.reader.readString();
		try {
			this.uuid = UUID.fromString(s);
		} catch (IllegalArgumentException var4) {
			if (s.isEmpty() || s.length() > 16) {
				this.reader.setCursor(i);
				throw ERROR_INVALID_NAME_OR_UUID.createWithContext(this.reader);
			}
		}
		this.maxResults = 1;
	}

	public void parseOptions() throws CommandSyntaxException {
		this.suggestions = this::suggestOptionsKey;
		this.reader.skipWhitespace();

		while (this.reader.canRead() && this.reader.peek() != ']') {
			this.reader.skipWhitespace();
			int i = this.reader.getCursor();
			String s = this.reader.readString();
			AreaSelectorOptions.Modifier modifier = AreaSelectorOptions.get(this, s, i);
			this.reader.skipWhitespace();
			if (this.reader.canRead() && this.reader.peek() == '=') {
				this.reader.skip();
				this.reader.skipWhitespace();
				this.suggestions = EntitySelectorParser.SUGGEST_NOTHING;
				modifier.handle(this);
				this.reader.skipWhitespace();
				this.suggestions = this::suggestOptionsNextOrClose;
				if (!this.reader.canRead()) {
					continue;
				}

				if (this.reader.peek() == ',') {
					this.reader.skip();
					this.suggestions = this::suggestOptionsKey;
					continue;
				}

				if (this.reader.peek() != ']') {
					throw EntitySelectorParser.ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
				}
				break;
			}

			this.reader.setCursor(i);
			throw EntitySelectorParser.ERROR_EXPECTED_OPTION_VALUE.createWithContext(this.reader, s);
		}

		if (this.reader.canRead()) {
			this.reader.skip();
			this.suggestions = EntitySelectorParser.SUGGEST_NOTHING;
		} else {
			throw EntitySelectorParser.ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
		}
	}

	public boolean shouldInvertValue() {
		this.reader.skipWhitespace();
		if (this.reader.canRead() && this.reader.peek() == '!') {
			this.reader.skip();
			this.reader.skipWhitespace();
			return true;
		} else {
			return false;
		}
	}

	public void thenTag() {
		boolean invert = shouldInvertValue();
		String s = reader.readUnquotedString();
		addPredicate(area -> {
			if (s.isEmpty()) {
				return area.getTags().isEmpty() != invert;
			} else {
				return area.getTags().contains(s) != invert;
			}
		});
	}

	public void addPredicate(Predicate<Area> predicate) {
		this.predicate = this.predicate.and(predicate);
	}

	public void thenType() {
		//TODO
	}

	public void thenLimit() throws CommandSyntaxException {
		int i = reader.getCursor();
		int j = reader.readInt();
		if (j < 1) {
			reader.setCursor(i);
			throw EntitySelectorOptions.ERROR_LIMIT_TOO_SMALL.createWithContext(getReader());
		} else {
			maxResults = j;
			limited = true;
		}
	}

	public boolean isTypeLimited() {
		return type != null;
	}

	public void thenDistance() throws CommandSyntaxException {
		int i = reader.getCursor();
		MinMaxBounds.Doubles range = MinMaxBounds.Doubles.fromReader(reader);
		if (range.getMin() != null && range.getMin() < 0.0 || range.getMax() != null && range.getMax() < 0.0) {
			reader.setCursor(i);
			throw EntitySelectorOptions.ERROR_RANGE_NEGATIVE.createWithContext(reader);
		} else {
			distance = range;
		}
	}

	public void thenSort() throws CommandSyntaxException {
		int i = reader.getCursor();
		String s = reader.readUnquotedString();
		setSuggestions((builder, builderConsumer) -> {
			return SharedSuggestionProvider.suggest(List.of("nearest", "furthest", "random", "arbitrary"), builder);
		});
		switch (s) {
			case "nearest":
				order = AreaSelector.ORDER_NEAREST;
				break;
			case "furthest":
				order = AreaSelector.ORDER_FURTHEST;
				break;
			case "random":
				order = AreaSelector.ORDER_RANDOM;
				break;
			case "arbitrary":
				order = AreaSelector.ORDER_ARBITRARY;
				break;
			default:
				reader.setCursor(i);
				throw EntitySelectorOptions.ERROR_SORT_UNKNOWN.createWithContext(reader, s);
		}
		sorted = true;
	}

	public void thenNBT() throws CommandSyntaxException {
		boolean invert = shouldInvertValue();
		CompoundTag nbt = new TagParser(reader).readStruct();
		addPredicate(area -> NbtUtils.compareNbt(nbt, area.getAttachedData(), true) != invert);
	}

	private CompletableFuture<Suggestions> suggestStart(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> builderConsumer) {
		fillSelectorSuggestions(builder);
		builder.add(builder);
		return builder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestSelector(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> builderConsumer) {
		SuggestionsBuilder suggestionsbuilder = builder.createOffset(builder.getStart() - 1);
		return suggestStart(suggestionsbuilder, builderConsumer);
	}

	private static void fillSelectorSuggestions(SuggestionsBuilder builder) {
		builder.suggest("@p", Component.translatable("loquat.argument.area.selector.p"));
		builder.suggest("@a", Component.translatable("loquat.argument.area.selector.a"));
		builder.suggest("@r", Component.translatable("loquat.argument.area.selector.r"));
		builder.suggest("@s", Component.translatable("loquat.argument.area.selector.s"));
	}

	private CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> builderConsumer) {
		builder.suggest(String.valueOf('['));
		return builder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestOptionsKeyOrClose(
			SuggestionsBuilder builder,
			Consumer<SuggestionsBuilder> builderConsumer) {
		builder.suggest(String.valueOf(']'));
		AreaSelectorOptions.suggestNames(this, builder);
		return builder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestOptionsKey(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> builderConsumer) {
		AreaSelectorOptions.suggestNames(this, builder);
		return builder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestOptionsNextOrClose(
			SuggestionsBuilder builder,
			Consumer<SuggestionsBuilder> builderConsumer) {
		builder.suggest(String.valueOf(','));
		builder.suggest(String.valueOf(']'));
		return builder.buildFuture();
	}

	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> builderConsumer) {
		return this.suggestions.apply(builder.createOffset(this.reader.getCursor()), builderConsumer);
	}
}
