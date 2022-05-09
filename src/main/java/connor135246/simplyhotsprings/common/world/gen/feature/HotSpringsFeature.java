package connor135246.simplyhotsprings.common.world.gen.feature;

import static connor135246.simplyhotsprings.common.SimplyHotSpringsCommon.HOT_SPRING_WATER_BLOCK;
import static connor135246.simplyhotsprings.common.SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER;

import java.util.Random;

import com.mojang.serialization.Codec;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.material.Material;

/**
 * pretty much copy-pasted from {@link net.minecraft.world.level.levelgen.feature.LakeFeature}
 */
public class HotSpringsFeature extends Feature<NoneFeatureConfiguration>
{

    public HotSpringsFeature(Codec<NoneFeatureConfiguration> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
    {
        if (!HOT_SPRING_WATER_BLOCK.isPresent())
            return false;

        WorldGenLevel glevel = context.level();
        Random rand = context.random();
        BlockPos pos = context.origin();

        if (context.chunkGenerator().hasFeatureChunkInRange(BuiltinStructureSets.VILLAGES, context.level().getSeed(),
                SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), 1))
        {
            SimplyHotSprings.log.info("Village found!"); // TODO remove
            return false;
        }

        while (pos.getY() > glevel.getMinBuildHeight() + 5 && glevel.isEmptyBlock(pos))
            pos = pos.below();

        pos = pos.below(rand.nextInt(3));

        return doPlace(glevel, rand, pos);
    }

    public static boolean doPlace(WorldGenLevel glevel, Random rand, BlockPos pos)
    {
        if (!HOT_SPRING_WATER_BLOCK.isPresent())
            return false;

        if (pos.getY() <= glevel.getMinBuildHeight() + 4)
            return false;
        else
        {
            pos = pos.below(4);

            {
                // TODO find out what exactly each of these values does and make them configurable via worldgen .jsons
                // lakes are made by creating multiple overlapping spheres
                // the dimensions of the spheres are random, but the y is always smaller making that ellipse shape
                // i is the number of spheres
                // d,e,f are the diameters in each dimension
                // g,h,k is the center of the sphere

                boolean[] bls = new boolean[2048];
                int i = rand.nextInt(4) + 4;

                for (int a = 0; a < i; ++a)
                {
                    double d = rand.nextDouble() * 6.0D + 3.0D;
                    double e = rand.nextDouble() * 4.0D + 2.0D;
                    double f = rand.nextDouble() * 6.0D + 3.0D;
                    double g = rand.nextDouble() * (16.0D - d - 2.0D) + 1.0D + d / 2.0D;
                    double h = rand.nextDouble() * (8.0D - e - 4.0D) + 2.0D + e / 2.0D;
                    double k = rand.nextDouble() * (16.0D - f - 2.0D) + 1.0D + f / 2.0D;

                    for (int x = 1; x < 15; ++x)
                    {
                        for (int z = 1; z < 15; ++z)
                        {
                            for (int y = 1; y < 7; ++y)
                            {
                                double o = ((double) x - g) / (d / 2.0D);
                                double p = ((double) y - h) / (e / 2.0D);
                                double q = ((double) z - k) / (f / 2.0D);
                                double r = o * o + p * p + q * q;
                                if (r < 1.0D)
                                {
                                    bls[(x * 16 + z) * 8 + y] = true;
                                }
                            }
                        }
                    }
                }

                for (int x = 0; x < 16; ++x)
                {
                    for (int z = 0; z < 16; ++z)
                    {
                        for (int y = 0; y < 8; ++y)
                        {
                            boolean flag = !bls[(x * 16 + z) * 8 + y]
                                    && (x < 15 && bls[((x + 1) * 16 + z) * 8 + y] || x > 0 && bls[((x - 1) * 16 + z) * 8 + y]
                                            || z < 15 && bls[(x * 16 + z + 1) * 8 + y] || z > 0 && bls[(x * 16 + (z - 1)) * 8 + y]
                                            || y < 7 && bls[(x * 16 + z) * 8 + y + 1] || y > 0 && bls[(x * 16 + z) * 8 + (y - 1)]);
                            if (flag)
                            {
                                Material material = glevel.getBlockState(pos.offset(x, y, z)).getMaterial();
                                if (y >= 4 && material.isLiquid())
                                    return false;

                                if (y < 4 && !material.isSolid() && !glevel.getFluidState(pos.offset(x, y, z)).is(TAG_HOT_SPRING_WATER))
                                    return false;
                            }
                        }
                    }
                }

                if (SimplyHotSpringsConfig.COMMON.debug.get())
                    SimplyHotSprings.log.info("Generated a hot spring around {} {} {}", pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8);

                for (int x = 0; x < 16; ++x)
                {
                    for (int z = 0; z < 16; ++z)
                    {
                        for (int y = 0; y < 8; ++y)
                        {
                            if (bls[(x * 16 + z) * 8 + y])
                            {
                                BlockPos setPos = pos.offset(x, y, z);
                                if (canReplaceBlock(glevel.getBlockState(setPos)))
                                {
                                    boolean air = y >= 4 || glevel.dimensionType().ultraWarm();
                                    glevel.setBlock(setPos, air ? Blocks.AIR.defaultBlockState() : HOT_SPRING_WATER_BLOCK.get().defaultBlockState(), 2);
                                    if (air)
                                    {
                                        glevel.scheduleTick(setPos, Blocks.AIR, 0);
                                        markAboveForPostProcessingStatic(glevel, setPos);
                                    }
                                }
                            }
                        }
                    }
                }

                for (int x = 0; x < 16; ++x)
                {
                    for (int z = 0; z < 16; ++z)
                    {
                        for (int y = 0; y < 8; ++y)
                        {
                            if (bls[(x * 16 + z) * 8 + y] && (y > 0 ? !bls[(x * 16 + z) * 8 + (y - 1)] : true))
                            {
                                BlockPos belowPos = pos.offset(x, y - 1, z);
                                BlockState belowState = glevel.getBlockState(belowPos);
                                boolean isDirt = isDirt(belowState);

                                if (y < 4)
                                {
                                    if (belowState.getMaterial().isSolid())
                                    {
                                        if (isDirt || belowState.is(Blocks.SNOW_BLOCK))
                                            glevel.setBlock(belowPos, Blocks.STONE.defaultBlockState(), 2);
                                        else if (belowState.is(Blocks.SAND))
                                            glevel.setBlock(belowPos, Blocks.SANDSTONE.defaultBlockState(), 2);
                                        else if (belowState.is(Blocks.RED_SAND))
                                            glevel.setBlock(belowPos, Blocks.RED_SANDSTONE.defaultBlockState(), 2);
                                    }
                                }
                                else if (y >= 4)
                                {
                                    // TODO
                                    /*
                                    BlockState topBlock = glevel.getBiome(belowPos).value().getGenerationSettings().getSurfaceBuilderConfig().getTop();

                                    if (isDirt)
                                    {
                                        if (glevel.getBrightness(LightLayer.SKY, belowPos.above()) > 0 && isDirt(topBlock))
                                            glevel.setBlock(belowPos, topBlock, 2);
                                    }
                                    else if (Tags.Blocks.NETHERRACK.contains(belowState.getBlock()))
                                    {
                                        if (glevel.isEmptyBlock(belowPos.above()) && BlockTags.NYLIUM.contains(topBlock.getBlock()))
                                            glevel.setBlock(belowPos, topBlock, 2);
                                    }
                                    */
                                }
                            }
                        }
                    }
                }

                return true;
            }
        }
    }

    public static boolean canReplaceBlock(BlockState state)
    {
        return !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    /**
     * {@link #markAboveForPostProcessing(WorldGenLevel, BlockPos)} but static
     */
    public static void markAboveForPostProcessingStatic(WorldGenLevel glevel, BlockPos pos)
    {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        for (int i = 0; i < 2; ++i)
        {
            mutablePos.move(Direction.UP);
            if (glevel.getBlockState(mutablePos).isAir())
                return;

            glevel.getChunk(mutablePos).markPosForPostprocessing(mutablePos);
        }
    }

}
