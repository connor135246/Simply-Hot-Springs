package connor135246.simplyhotsprings.util;

import net.minecraft.util.text.TextFormatting;

/**
 * An enum for encapsulating the reason a hot spring might or might not generate in a dimension and biome.
 */
public enum GenerationReason
{

    NO_WORLD_GEN(false, "no_world_gen"),
    BOP_SPRINGS(false, "bop_springs"),
    SUPERFLAT(false, "superflat"),
    IN_DIM_BLACKLIST(false, "dim_blacklist"),
    NOT_DIM_WHITELISTED(false, "dim_whitelist"),
    /** an internal reason used just to pass on from world checks to biome checks */
    ALLOW_WORLD(true, "allow_world"),

    IN_BIOME_NAME_WHITELIST(true, "biome_name_whitelist"),
    IN_BIOME_NAME_BLACKLIST(false, "biome_name_blacklist"),
    IN_BIOME_TYPE_BLACKLIST(false, "biome_type_blacklist"),
    IN_BIOME_TYPE_WHITELIST(true, "biome_type_whitelist"),
    NOT_BIOME_BLACKLISTED(true, "not_blacklisted"),
    NOT_BIOME_WHITELISTED(false, "not_whitelisted"),
    UNKNOWN_BIOME(false, "unknown_biome");

    private final boolean allowsGeneration;
    private final String key;

    GenerationReason(boolean allowsGeneration, String reasonKey)
    {
        this.allowsGeneration = allowsGeneration;
        this.key = Reference.MODID + ".gen.reason." + reasonKey;
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

    public static final String YES = Reference.MODID + ".command.yes", NO = Reference.MODID + ".command.no";

    public String getYN()
    {
        return allowsGeneration ? YES : NO;
    }

}
