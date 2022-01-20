package connor135246.simplyhotsprings.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.BiomeDictionary;

/**
 * An argument type that gets a {@link BiomeDictionary.Type}
 */
public class BiomeTypeArgument implements ArgumentType<BiomeDictionary.Type>
{
    private static final List<String> EXAMPLES = Arrays.asList(BiomeDictionary.Type.FOREST.getName(), BiomeDictionary.Type.NETHER.getName(),
            BiomeDictionary.Type.MODIFIED.getName());

    private static final DynamicCommandExceptionType TYPE_NOT_FOUND = new DynamicCommandExceptionType((type) -> {
        return new TranslationTextComponent(SimplyHotSpringsCommand.LANG_BIOMETYPES + "type_not_found", type);
    });

    public static BiomeTypeArgument biomeTypeArgument()
    {
        return new BiomeTypeArgument();
    }

    @Override
    public BiomeDictionary.Type parse(final StringReader reader) throws CommandSyntaxException
    {
        String string = reader.readUnquotedString();

        for (BiomeDictionary.Type type : BiomeDictionary.Type.getAll())
            if (type.getName().equals(string))
                return type;

        throw TYPE_NOT_FOUND.create(string);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        return ISuggestionProvider.suggest(BiomeDictionary.Type.getAll().stream().map(type -> type.getName()), builder);
    }

    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }

}
