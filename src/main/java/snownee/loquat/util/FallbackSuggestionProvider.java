package snownee.loquat.util;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;

public class FallbackSuggestionProvider implements SuggestionProvider<SharedSuggestionProvider> {
	private final SuggestionProvider<SharedSuggestionProvider> delegate;

	public static <S extends SharedSuggestionProvider> SuggestionProvider<S> register(ResourceLocation id, SuggestionProvider<SharedSuggestionProvider> delegate) {
		return SuggestionProviders.register(id, new FallbackSuggestionProvider(delegate));
	}

	public FallbackSuggestionProvider(SuggestionProvider<SharedSuggestionProvider> delegate) {
		this.delegate = delegate;
	}

	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<SharedSuggestionProvider> context, SuggestionsBuilder builder) throws CommandSyntaxException {
		CompletableFuture<Suggestions> suggestions = delegate.getSuggestions(context, builder);
		if (suggestions.isDone() && !suggestions.join().isEmpty()) {
			return suggestions;
		}
		return SuggestionProviders.ASK_SERVER.getSuggestions(context, builder);
	}
}
