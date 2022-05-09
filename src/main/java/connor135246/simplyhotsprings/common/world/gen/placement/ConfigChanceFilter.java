package connor135246.simplyhotsprings.common.world.gen.placement;

import java.util.Random;

import com.mojang.serialization.Codec;

import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * a simple placement similar to {@link net.minecraft.world.level.levelgen.placement.RarityFilter} but it always uses {@link SimplyHotSpringsConfig.Common#chance}
 */
public class ConfigChanceFilter extends PlacementFilter
{

    private static final ConfigChanceFilter INSTANCE = new ConfigChanceFilter();

    public static Codec<ConfigChanceFilter> CODEC = Codec.unit(() -> {
        return INSTANCE;
    });

    public static ConfigChanceFilter configChance()
    {
        return INSTANCE;
    }

    protected static int chance = 275;

    @Override
    protected boolean shouldPlace(PlacementContext context, Random rand, BlockPos pos)
    {
        return rand.nextFloat() < 1.0F / (float) chance;
    }

    /**
     * instead of using a direct reference to the config option, we only update this when a world loads (from {@link SimplyHotSpringsConfig#finalizeSpringsGeneration})
     */
    public static void updateChance(int newChance)
    {
        chance = newChance;
    }

    public static final PlacementModifierType<ConfigChanceFilter> TYPE = new PlacementModifierType<ConfigChanceFilter>() {
        @Override
        public Codec<ConfigChanceFilter> codec()
        {
            return ConfigChanceFilter.CODEC;
        }
    };

    @Override
    public PlacementModifierType<?> type()
    {
        return TYPE;
    }

}
