package connor135246.simplyhotsprings.util;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.util.text.TextFormatting;

/**
 * An enum for encapsulating the reason a hot spring might or might not generate in a biome.
 */
public enum GenerationReason
{

    NO_WORLD_GEN(false, "no_world_gen"),
    IN_NAME_WHITELIST(true, "in_name_whitelist"),
    IN_NAME_BLACKLIST(false, "in_name_blacklist"),
    IN_TYPE_BLACKLIST(false, "in_type_blacklist"),
    IN_TYPE_WHITELIST(true, "in_type_whitelist"),
    NOT_BLACKLISTED(true, "not_blacklisted"),
    NOT_WHITELISTED(false, "not_whitelisted"),
    UNKNOWN_BIOME(false, "unknown_biome"),
    CONFIGURED_ERROR(false, "configured_error");

    private final boolean allowsGeneration;
    private final String key;

    GenerationReason(boolean allowsGeneration, String reasonKey)
    {
        this.allowsGeneration = allowsGeneration;
        this.key = SimplyHotSprings.MODID + ".gen.reason." + reasonKey;
    }

    public boolean allowsGeneration()
    {
        return allowsGeneration;
    }

    public String getKey()
    {
        return key;
    }

    public TextFormatting getTextFormatting()
    {
        return allowsGeneration ? TextFormatting.GREEN : TextFormatting.DARK_RED;
    }

}
