package connor135246.simplyhotsprings.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.Tags;

/**
 * {@link ResourceKeyArgument}, but tags instead. Or if you prefer, {@link ResourceOrTagLocationArgument}, but without the resources. Either way, there's a '#' in front of it.
 */
public class TagKeyArgument<T> implements ArgumentType<TagKey<T>>
{

    private static final List<String> EXAMPLES = Arrays.asList("#" + BlockTags.MINEABLE_WITH_PICKAXE.location().toString(),
            "#" + Tags.Items.DYES.location().toString(),
            "#" + BiomeTags.WATER_ON_MAP_OUTLINES.location().toString());

    private static final DynamicCommandExceptionType NOT_A_TAG = new DynamicCommandExceptionType((tag) -> {
        return Component.translatable(SimplyHotSpringsCommand.LANG_BIOMETAGS + "not_a_tag", tag);
    });

    public final ResourceKey<? extends Registry<T>> registryKey;

    private TagKeyArgument(ResourceKey<? extends Registry<T>> registryKey)
    {
        this.registryKey = registryKey;
    }

    public static <T> TagKeyArgument<T> tagKeyArgument(ResourceKey<? extends Registry<T>> registryKey)
    {
        return new TagKeyArgument<T>(registryKey);
    }

    public static <T> TagKey<T> get(CommandContext<CommandSourceStack> context, String name, ResourceKey<Registry<T>> registry,
            DynamicCommandExceptionType exception) throws CommandSyntaxException
    {
        TagKey<?> result = context.getArgument(name, TagKey.class);
        Optional<TagKey<T>> optional = result.cast(registry);
        return optional.orElseThrow(() -> {
            return exception.create("#" + result);
        });
    }

    @Override
    public TagKey<T> parse(final StringReader reader) throws CommandSyntaxException
    {
        if (reader.canRead() && reader.peek() == '#')
        {
            int i = reader.getCursor();

            try
            {
                reader.skip();
                ResourceLocation id = ResourceLocation.read(reader);
                return TagKey.create(this.registryKey, id);
            }
            catch (CommandSyntaxException excep)
            {
                reader.setCursor(i);
                throw excep;
            }
        }
        else
            throw NOT_A_TAG.create(reader.readUnquotedString());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        Object object = context.getSource();
        if (object instanceof SharedSuggestionProvider sharedsuggestionprovider)
            return sharedsuggestionprovider.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.TAGS, builder, context);
        else
            return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }

    public static class Info<T> implements ArgumentTypeInfo<TagKeyArgument<T>, TagKeyArgument.Info<T>.Template>
    {
        public void serializeToNetwork(TagKeyArgument.Info<T>.Template p_233278_, FriendlyByteBuf p_233279_)
        {
            p_233279_.writeResourceLocation(p_233278_.registryKey.location());
        }

        public TagKeyArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf p_233289_)
        {
            ResourceLocation resourcelocation = p_233289_.readResourceLocation();
            return new TagKeyArgument.Info<T>.Template(ResourceKey.createRegistryKey(resourcelocation));
        }

        public void serializeToJson(TagKeyArgument.Info<T>.Template p_233275_, JsonObject p_233276_)
        {
            p_233276_.addProperty("registry", p_233275_.registryKey.location().toString());
        }

        public TagKeyArgument.Info<T>.Template unpack(TagKeyArgument<T> p_233281_)
        {
            return new TagKeyArgument.Info<T>.Template(p_233281_.registryKey);
        }

        public final class Template implements ArgumentTypeInfo.Template<TagKeyArgument<T>>
        {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> p_233296_)
            {
                this.registryKey = p_233296_;
            }

            public TagKeyArgument<T> instantiate(CommandBuildContext p_233299_)
            {
                return new TagKeyArgument<>(this.registryKey);
            }

            public ArgumentTypeInfo<TagKeyArgument<T>, ?> type()
            {
                return Info.this;
            }
        }
    }

}
