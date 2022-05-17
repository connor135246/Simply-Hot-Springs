package connor135246.simplyhotsprings.common.world.gen.feature;

import static connor135246.simplyhotsprings.common.SimplyHotSpringsCommon.HOT_SPRING_WATER_BLOCK;
import static connor135246.simplyhotsprings.common.SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER;

import java.util.Random;

import com.mojang.serialization.Codec;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.common.Tags;

/**
 * pretty much copy-pasted from {@link net.minecraft.world.gen.feature.LakesFeature}
 */
public class HotSpringsFeature extends Feature<NoFeatureConfig>
{

    public HotSpringsFeature(Codec<NoFeatureConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean generate(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config)
    {
        if (!HOT_SPRING_WATER_BLOCK.isPresent())
            return false;

        while (pos.getY() > 5 && reader.isAirBlock(pos))
            pos = pos.down();

        pos = pos.down(rand.nextInt(3));

        return doGenerate(reader, rand, pos, true);
    }

    public static boolean doGenerate(ISeedReader reader, Random rand, BlockPos pos, boolean checkForVillage)
    {
        if (!HOT_SPRING_WATER_BLOCK.isPresent())
            return false;

        if (pos.getY() <= 4)
            return false;
        else
        {
            pos = pos.down(4);
            if (checkForVillage && reader.func_241827_a(SectionPos.from(pos), Structure.VILLAGE).findAny().isPresent())
                return false;
            else
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
                                Material material = reader.getBlockState(pos.add(x, y, z)).getMaterial();
                                if (y >= 4 && material.isLiquid())
                                    return false;

                                if (y < 4 && !material.isSolid() && !reader.getFluidState(pos.add(x, y, z)).isTagged(TAG_HOT_SPRING_WATER))
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
                                BlockPos setPos = pos.add(x, y, z);
                                BlockState replaceState = reader.getBlockState(setPos);

                                if (replaceState.getBlockHardness(reader, setPos) >= 0.0F && !replaceState.hasTileEntity())
                                    reader.setBlockState(setPos, y >= 4 || reader.getDimensionType().isUltrawarm() ? Blocks.AIR.getDefaultState()
                                            : HOT_SPRING_WATER_BLOCK.get().getDefaultState(), 2);
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
                                BlockPos belowPos = pos.add(x, y - 1, z);
                                BlockState belowState = reader.getBlockState(belowPos);
                                boolean isDirt = isDirt(belowState.getBlock());

                                if (y < 4)
                                {
                                    if (belowState.getMaterial().isSolid())
                                    {
                                        if (isDirt || belowState.matchesBlock(Blocks.SNOW_BLOCK))
                                            reader.setBlockState(belowPos, Blocks.STONE.getDefaultState(), 2);
                                        else if (belowState.matchesBlock(Blocks.SAND))
                                            reader.setBlockState(belowPos, Blocks.SANDSTONE.getDefaultState(), 2);
                                        else if (belowState.matchesBlock(Blocks.RED_SAND))
                                            reader.setBlockState(belowPos, Blocks.RED_SANDSTONE.getDefaultState(), 2);
                                    }
                                }
                                else if (y >= 4 && reader.isAirBlock(belowPos.up()))
                                {
                                    BlockState topBlock = reader.getBiome(belowPos).getGenerationSettings().getSurfaceBuilderConfig().getTop();

                                    if (isDirt)
                                    {
                                        if (isDirt(topBlock.getBlock())) // can't do reader.getLightFor - lighting manager sometimes isn't ready in adjacent chunks
                                            reader.setBlockState(belowPos, topBlock, 2);
                                    }
                                    else if (Tags.Blocks.NETHERRACK.contains(belowState.getBlock()))
                                    {
                                        if (BlockTags.NYLIUM.contains(topBlock.getBlock()))
                                            reader.setBlockState(belowPos, topBlock, 2);
                                    }
                                }
                            }
                        }
                    }
                }

                return true;
            }
        }
    }

}
