package connor135246.simplyhotsprings.common.world.gen.placement;

import java.util.Random;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;

import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.SimplePlacement;

/**
 * a simple placement similar to {@link net.minecraft.world.gen.placement.ChancePlacement} but it always uses {@link SimplyHotSpringsConfig.Common#chance}
 */
public class ConfigChancePlacement extends SimplePlacement<NoPlacementConfig>
{

    protected static int chance = 275;

    public ConfigChancePlacement(Codec<NoPlacementConfig> codec)
    {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(Random random, NoPlacementConfig config, BlockPos pos)
    {
        return random.nextFloat() < 1.0F / (float) chance ? Stream.of(pos) : Stream.empty();
    }

    /**
     * instead of using a direct reference to the config option, we only update this when a world loads (from {@link SimplyHotSpringsConfig#finalizeSpringsGeneration})
     */
    public static void updateChance(int newChance)
    {
        chance = newChance;
    }

}
