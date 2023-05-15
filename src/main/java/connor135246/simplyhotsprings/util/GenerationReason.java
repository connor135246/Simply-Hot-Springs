package connor135246.simplyhotsprings.util;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.ChatFormatting;

/**
 * An enum for encapsulating the reason a hot spring might or might not generate in a biome.
 */
public enum GenerationReason
{

    NO_WORLD_GEN_CONFIG(false, "no_world_gen_config"),
    WHITELISTED(true, "whitelisted"),
    BLACKLISTED(false, "blacklisted"),
    NOT_WHITELISTED(false, "not_whitelisted"),
    NOT_BLACKLISTED(true, "not_blacklisted"),
    NO_HOT_SPRINGS(false, "no_hot_springs"),
    UNKNOWN_BIOME(false, "unknown_biome"),
    UNKNOWN_MODIFIER(false, "unknown_modifier");

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

    public ChatFormatting getTextFormatting()
    {
        return allowsGeneration ? ChatFormatting.GREEN : ChatFormatting.DARK_RED;
    }

    public static final String YES = SimplyHotSpringsCommand.LANG_LOCATIONINFO + "yes", NO = SimplyHotSpringsCommand.LANG_LOCATIONINFO + "no";

    public String getYN()
    {
        return allowsGeneration ? YES : NO;
    }

}
