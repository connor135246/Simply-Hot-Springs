package connor135246.simplyhotsprings.common.world.gen.placement;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.HeightmapBasedPlacement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;

/**
 * if the dimension doesn't have a ceiling, it's the same as {@link net.minecraft.world.gen.placement.HeightmapPlacement} except if the y is below the
 * {@link TopSolidRangeConfig#bottomOffset}, the placement is discarded. <br>
 * if the dimension does have a ceiling, it ensures the y isn't above {@link TopSolidRangeConfig#maximum}, moves the y down by {@link TopSolidRangeConfig#topOffset}, finds the next
 * surface below, and then if the y is below the {@link TopSolidRangeConfig#bottomOffset}, the placement is discarded.
 */
public class CeilingAwareHeightmapPlacement extends HeightmapBasedPlacement<TopSolidRangeConfig>
{

    /** reflecting the {@link WorldDecoratingHelper}'s ISeedReader to be able to check if the dimension has a ceiling */
    protected static Field helperReaderField = null;
    static
    {
        try
        {
            helperReaderField = WorldDecoratingHelper.class.getDeclaredField("field_242889_a");
            helperReaderField.setAccessible(true);
        }
        catch (NoSuchFieldException | SecurityException excep)
        {
            helperReaderField = null;
        }
    }

    public CeilingAwareHeightmapPlacement(Codec<TopSolidRangeConfig> codec)
    {
        super(codec);
    }

    @Override
    protected Heightmap.Type func_241858_a(TopSolidRangeConfig config)
    {
        return Heightmap.Type.MOTION_BLOCKING;
    }

    @Override
    public Stream<BlockPos> getPositions(WorldDecoratingHelper helper, Random rand, TopSolidRangeConfig config, BlockPos pos)
    {
        int x = pos.getX();
        int z = pos.getZ();
        int y = helper.func_242893_a(func_241858_a(config), x, z);
        pos = new BlockPos(x, y, z);

        ISeedReader helperReader = getHelperReader(helper);
        if (helperReader != null && helperReader.getDimensionType().getHasCeiling()
                && helperReader.hasBlockState(pos.down(), state -> state.matchesBlock(Blocks.BEDROCK)))
        {
            if (config.maximum > -1 && pos.getY() > config.maximum)
                pos = new BlockPos(pos.getX(), config.maximum, pos.getZ());
            pos = pos.down(config.topOffset);
            // find first section of air below the top
            while (pos.getY() >= 0 && !helperReader.isAirBlock(pos))
                pos = pos.down();
            // find first non-air block below the first section of air (the new "surface")
            while (pos.getY() >= 0 && helperReader.isAirBlock(pos))
                pos = pos.down();
            pos = pos.up();
        }

        return pos.getY() > config.bottomOffset ? Stream.of(pos) : Stream.of();
    }

    @Nullable
    protected static ISeedReader getHelperReader(WorldDecoratingHelper helper)
    {
        if (helperReaderField != null)
        {
            try
            {
                return (ISeedReader) helperReaderField.get(helper);
            }
            catch (ClassCastException | IllegalArgumentException | IllegalAccessException excep)
            {
                ;
            }
        }
        return null;
    }

}
