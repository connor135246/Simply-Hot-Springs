package connor135246.simplyhotsprings.common.world.gen.feature;

import static connor135246.simplyhotsprings.common.SimplyHotSpringsCommon.HOT_SPRING_WATER_BLOCK;
import static connor135246.simplyhotsprings.common.SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER;

import java.util.Map.Entry;
import java.util.Random;

import com.mojang.serialization.Codec;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ForgeRegistries;

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

        while (pos.getY() > glevel.getMinBuildHeight() + 5 && glevel.isEmptyBlock(pos))
            pos = pos.below();

        pos = pos.below(rand.nextInt(3));

        return doPlace(glevel, rand, pos, true);
    }

    public static boolean doPlace(WorldGenLevel glevel, Random rand, BlockPos pos, boolean postProcess)
    {
        if (!HOT_SPRING_WATER_BLOCK.isPresent())
            return false;

        if (pos.getY() <= glevel.getMinBuildHeight() + 4)
            return false;

        pos = pos.below(4);

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
                            if (postProcess && air)
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
                        if (canReplaceBlock(belowState))
                        {
                            if (y < 4)
                            {
                                if (belowState.getMaterial().isSolid())
                                {
                                    if (isDirt(belowState) || belowState.is(BlockTags.SNOW))
                                        glevel.setBlock(belowPos, Blocks.STONE.defaultBlockState(), 2);
                                    else if (belowState.is(Blocks.SAND))
                                        glevel.setBlock(belowPos, Blocks.SANDSTONE.defaultBlockState(), 2);
                                    else if (belowState.is(Blocks.RED_SAND))
                                        glevel.setBlock(belowPos, Blocks.RED_SANDSTONE.defaultBlockState(), 2);
                                }
                            }
                            else if (y >= 4)
                            {
                                ResourceKey<Biome> biome = ResourceKey.create(ForgeRegistries.Keys.BIOMES,
                                        glevel.getBiome(belowPos).value().getRegistryName());
                                if (biome == Biomes.CRIMSON_FOREST || biome == Biomes.WARPED_FOREST)
                                {
                                    if (belowState.is(Blocks.NETHERRACK) && glevel.isEmptyBlock(belowPos.above()))
                                        glevel.setBlock(belowPos, biome == Biomes.CRIMSON_FOREST ? Blocks.CRIMSON_NYLIUM.defaultBlockState()
                                                : Blocks.WARPED_NYLIUM.defaultBlockState(), 2);
                                }
                                else if (belowState.is(Blocks.DIRT) && glevel.isEmptyBlock(belowPos.above())) // can't do glevel.getBrightness - lighting manager sometimes isn't ready in adjacent chunks
                                    glevel.setBlock(belowPos, biomeGrasses.get(biome), 2);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private static final Object2ObjectOpenHashMap<ResourceKey<Biome>, BlockState> biomeGrasses = Util.make(
            new Object2ObjectOpenHashMap<ResourceKey<Biome>, BlockState>(10, 0.95F), map -> map.defaultReturnValue(Blocks.GRASS_BLOCK.defaultBlockState()));

    /**
     * instead of using a direct reference to the config option, we only update this when a world loads (from {@link SimplyHotSpringsConfig#finalizeSpringsGeneration})
     */
    public static void updateBiomeGrasses(Object2ObjectOpenHashMap<ResourceKey<Biome>, BlockState> newBiomeGrasses)
    {
        biomeGrasses.clear();
        for (Entry<ResourceKey<Biome>, BlockState> entry : newBiomeGrasses.entrySet())
            biomeGrasses.put(entry.getKey(), entry.getValue());
        //biomeGrasses.trim();
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
