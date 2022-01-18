package connor135246.simplyhotsprings.common.world;

import java.util.Random;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.blocks.BlockHotSpringWater;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class HotSpringsWorldGen implements IWorldGenerator
{

    public static final HotSpringsWorldGen INSTANCE = new HotSpringsWorldGen();

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        if (!SimplyHotSpringsConfig.WorldGen.generationReasonWorld(world).allowsGeneration())
            return;

        // pretty much copy-pasted from net.minecraft.world.gen.feature.WorldGenLakes

        BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(chunkX * 16 + rand.nextInt(16), 255, chunkZ * 16 + rand.nextInt(16))).down(rand.nextInt(3));

        if (pos.getY() > 4 && rand.nextInt(Math.max(SimplyHotSpringsConfig.WorldGen.chance, 1)) == 0
                && SimplyHotSpringsConfig.WorldGen.generationReasonBiome(world.getBiomeForCoordsBody(pos)).allowsGeneration())
        {
            pos = pos.down(4);
            boolean[] aboolean = new boolean[2048];
            int i = rand.nextInt(4) + 4;

            for (int j = 0; j < i; ++j)
            {
                double d0 = rand.nextDouble() * 6.0D + 3.0D;
                double d1 = rand.nextDouble() * 4.0D + 2.0D;
                double d2 = rand.nextDouble() * 6.0D + 3.0D;
                double d3 = rand.nextDouble() * (16.0D - d0 - 2.0D) + 1.0D + d0 / 2.0D;
                double d4 = rand.nextDouble() * (8.0D - d1 - 4.0D) + 2.0D + d1 / 2.0D;
                double d5 = rand.nextDouble() * (16.0D - d2 - 2.0D) + 1.0D + d2 / 2.0D;

                for (int l = 1; l < 15; ++l)
                {
                    for (int i1 = 1; i1 < 15; ++i1)
                    {
                        for (int j1 = 1; j1 < 7; ++j1)
                        {
                            double d6 = ((double) l - d3) / (d0 / 2.0D);
                            double d7 = ((double) j1 - d4) / (d1 / 2.0D);
                            double d8 = ((double) i1 - d5) / (d2 / 2.0D);
                            double d9 = d6 * d6 + d7 * d7 + d8 * d8;

                            if (d9 < 1.0D)
                                aboolean[(l * 16 + i1) * 8 + j1] = true;
                        }
                    }
                }
            }

            for (int k1 = 0; k1 < 16; ++k1)
            {
                for (int l2 = 0; l2 < 16; ++l2)
                {
                    for (int k = 0; k < 8; ++k)
                    {
                        if (!aboolean[(k1 * 16 + l2) * 8 + k]
                                && (k1 < 15 && aboolean[((k1 + 1) * 16 + l2) * 8 + k] || k1 > 0 && aboolean[((k1 - 1) * 16 + l2) * 8 + k]
                                        || l2 < 15 && aboolean[(k1 * 16 + l2 + 1) * 8 + k] || l2 > 0 && aboolean[(k1 * 16 + (l2 - 1)) * 8 + k]
                                        || k < 7 && aboolean[(k1 * 16 + l2) * 8 + k + 1] || k > 0 && aboolean[(k1 * 16 + l2) * 8 + (k - 1)]))
                        {
                            IBlockState blockstate = world.getBlockState(pos.add(k1, k, l2));

                            if (k >= 4 && blockstate.getMaterial().isLiquid())
                                return;

                            if (k < 4 && !blockstate.getMaterial().isSolid() && blockstate.getBlock() != BlockHotSpringWater.BLOCK_INSTANCE)
                                return;
                        }
                    }
                }
            }

            if (SimplyHotSpringsConfig.WorldGen.debug)
                SimplyHotSprings.modlog.info("Generated a hot spring around {} {} {}", pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8);

            for (int l1 = 0; l1 < 16; ++l1)
            {
                for (int i3 = 0; i3 < 16; ++i3)
                {
                    for (int i4 = 0; i4 < 8; ++i4)
                    {
                        if (aboolean[(l1 * 16 + i3) * 8 + i4])
                        {
                            BlockPos setBlockPos = pos.add(l1, i4, i3);
                            IBlockState setBlockState = world.getBlockState(setBlockPos);

                            if (setBlockState.getBlockHardness(world, setBlockPos) != -1.0 && !setBlockState.getBlock().hasTileEntity(setBlockState))
                                world.setBlockState(setBlockPos, i4 >= 4 || world.provider.doesWaterVaporize() ? Blocks.AIR.getDefaultState()
                                        : BlockHotSpringWater.BLOCK_INSTANCE.getDefaultState(), 2);
                        }
                    }
                }
            }

            for (int i2 = 0; i2 < 16; ++i2)
            {
                for (int j3 = 0; j3 < 16; ++j3)
                {
                    for (int j4 = 4; j4 < 8; ++j4)
                    {
                        if (aboolean[(i2 * 16 + j3) * 8 + j4])
                        {
                            BlockPos blockpos = pos.add(i2, j4 - 1, j3);

                            if (world.getBlockState(blockpos).getBlock() == Blocks.DIRT && world.getLightFor(EnumSkyBlock.SKY, pos.add(i2, j4, j3)) > 0)
                            {
                                Biome biome = world.getBiome(blockpos);

                                if (biome.topBlock.getBlock() == Blocks.MYCELIUM)
                                    world.setBlockState(blockpos, Blocks.MYCELIUM.getDefaultState(), 2);
                                else
                                    world.setBlockState(blockpos, Blocks.GRASS.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }
        }
    }

}
