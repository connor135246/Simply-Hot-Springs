package connor135246.simplyhotsprings.common.world.gen.placement;

import java.util.Random;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * if the dimension doesn't have a ceiling, it's the same as {@link net.minecraft.world.level.levelgen.placement.HeightmapPlacement} except if the y is below the
 * {@link #minHeight}, the placement is discarded. <br>
 * if the dimension does have a ceiling, it ensures the y isn't above the {@link #maxHeightIfBelowRoof}, moves the y down by {@link #roofOffset}, finds the next surface below, and
 * then if the y is below the {@link #minHeight}, the placement is discarded.
 */
public class CeilingAwareHeightmapPlacement extends PlacementModifier
{

    public static final Codec<CeilingAwareHeightmapPlacement> CODEC = RecordCodecBuilder.create((p_191701_) -> {
        return p_191701_.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter((p_191705_) -> {
            return p_191705_.heightmap;
        }), VerticalAnchor.CODEC.optionalFieldOf("min_height", VerticalAnchor.bottom()).forGetter((p_191705_) -> {
            return p_191705_.minHeight;
        }), IntProvider.CODEC.fieldOf("roof_offset").forGetter((p_191705_) -> {
            return p_191705_.roofOffset;
        }), VerticalAnchor.CODEC.optionalFieldOf("max_height_if_below_roof", VerticalAnchor.top()).forGetter((p_191705_) -> {
            return p_191705_.maxHeightIfBelowRoof;
        })).apply(p_191701_, CeilingAwareHeightmapPlacement::new);
    });

    private final Heightmap.Types heightmap;
    private final VerticalAnchor minHeight;
    private final IntProvider roofOffset;
    private final VerticalAnchor maxHeightIfBelowRoof;

    public CeilingAwareHeightmapPlacement(Heightmap.Types heightmap, VerticalAnchor minHeight, IntProvider roofOffset, VerticalAnchor maxHeightIfBelowRoof)
    {
        this.heightmap = heightmap;
        this.minHeight = minHeight;
        this.roofOffset = roofOffset;
        this.maxHeightIfBelowRoof = maxHeightIfBelowRoof;
    }

    private static final CeilingAwareHeightmapPlacement simpleSurface = new CeilingAwareHeightmapPlacement(
            Heightmap.Types.WORLD_SURFACE_WG, VerticalAnchor.bottom(), ConstantInt.of(16), VerticalAnchor.top());

    public static CeilingAwareHeightmapPlacement simpleSurface()
    {
        return simpleSurface;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, Random rand, BlockPos pos)
    {
        int x = pos.getX();
        int z = pos.getZ();
        int y = context.getHeight(heightmap, x, z);
        pos = new BlockPos(x, y, z);

        if (context.getLevel().dimensionType().hasCeiling() && context.getLevel().isStateAtPosition(pos.below(), state -> state.is(Blocks.BEDROCK)))
        {
            int maximum = maxHeightIfBelowRoof.resolveY(context);
            if (pos.getY() > maximum)
                pos = new BlockPos(pos.getX(), maximum, pos.getZ());
            pos = pos.below(roofOffset.sample(rand));
            // find first section of air below the top
            while (pos.getY() >= context.getMinGenY() && !context.getLevel().isEmptyBlock(pos))
                pos = pos.below();
            // find first non-air block below the first section of air (the new "surface")
            while (pos.getY() >= context.getMinGenY() && context.getLevel().isEmptyBlock(pos))
                pos = pos.below();
            pos = pos.above();
        }

        return pos.getY() > minHeight.resolveY(context) ? Stream.of(pos) : Stream.of();
    }

    public static final PlacementModifierType<CeilingAwareHeightmapPlacement> TYPE = new PlacementModifierType<CeilingAwareHeightmapPlacement>() {
        @Override
        public Codec<CeilingAwareHeightmapPlacement> codec()
        {
            return CeilingAwareHeightmapPlacement.CODEC;
        }
    };

    @Override
    public PlacementModifierType<?> type()
    {
        return TYPE;
    }

}
