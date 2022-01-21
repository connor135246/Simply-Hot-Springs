package connor135246.simplyhotsprings.common.world;

import java.util.Random;

import com.google.common.base.Predicates;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.blocks.BlockHotSpringWater;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

/**
 * pretty much copy-pasted from {@link net.minecraft.world.gen.feature.WorldGenLakes}
 */
public class HotSpringsWorldGen implements IWorldGenerator
{

    public static final HotSpringsWorldGen INSTANCE = new HotSpringsWorldGen();

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        if (!SimplyHotSpringsConfig.allowedWorld(world))
            return;

        BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(chunkX * 16 + rand.nextInt(16), 255, chunkZ * 16 + rand.nextInt(16))).down(rand.nextInt(3));

        if (pos.getY() > 4 && rand.nextInt(Math.max(SimplyHotSpringsConfig.Config.WorldGen.chance, 1)) == 0
                && SimplyHotSpringsConfig.allowedBiome(world.getBiomeForCoordsBody(pos)))
        {
            pos = pos.down(4);
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
                                bls[(x * 16 + z) * 8 + y] = true;
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
                        if (bls[(x * 16 + z) * 8 + y])
                        {
                            // {@link IChunkGenerator#isInsideStructure} only checks if the position is within the bounding box of a village building,
                            // not within a village as a whole.
                            // to be reasonably sure that the spring isn't ruining a village, multiple positions around the spring have to be checked.
                            if (x % 5 == 0 && y % 5 == 0 && z % 5 == 0 && chunkGenerator.isInsideStructure(world, "Village", pos.add(x, y, z)))
                                return;
                        }
                        else if (x < 15 && bls[((x + 1) * 16 + z) * 8 + y] || x > 0 && bls[((x - 1) * 16 + z) * 8 + y]
                                || z < 15 && bls[(x * 16 + z + 1) * 8 + y] || z > 0 && bls[(x * 16 + (z - 1)) * 8 + y]
                                || y < 7 && bls[(x * 16 + z) * 8 + y + 1] || y > 0 && bls[(x * 16 + z) * 8 + (y - 1)])
                        {
                            IBlockState blockstate = world.getBlockState(pos.add(x, y, z));

                            if (y >= 4 && blockstate.getMaterial().isLiquid())
                                return;

                            if (y < 4 && !blockstate.getMaterial().isSolid() && blockstate.getBlock() != BlockHotSpringWater.BLOCK_INSTANCE)
                                return;
                        }
                    }
                }
            }

            if (SimplyHotSpringsConfig.Config.WorldGen.debug)
                SimplyHotSprings.modlog.info("Generated a hot spring around {} {} {}", pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8);

            for (int x = 0; x < 16; ++x)
            {
                for (int z = 0; z < 16; ++z)
                {
                    for (int y = 0; y < 8; ++y)
                    {
                        if (bls[(x * 16 + z) * 8 + y])
                        {
                            BlockPos setBlockPos = pos.add(x, y, z);
                            IBlockState setBlockState = world.getBlockState(setBlockPos);

                            if (setBlockState.getBlockHardness(world, setBlockPos) != -1.0 && !setBlockState.getBlock().hasTileEntity(setBlockState))
                                world.setBlockState(setBlockPos, y >= 4 || world.provider.doesWaterVaporize() ? Blocks.AIR.getDefaultState()
                                        : BlockHotSpringWater.BLOCK_INSTANCE.getDefaultState(), 2);
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
                            IBlockState belowState = world.getBlockState(belowPos);
                            boolean isDirt = DIRT_MATCHER.apply(belowState);

                            if (y < 4)
                            {
                                if (belowState.getMaterial().isSolid())
                                {
                                    if (isDirt || belowState.getBlock() == Blocks.SNOW)
                                        world.setBlockState(belowPos, Blocks.STONE.getDefaultState(), 2);
                                    else if (SAND_MATCHER.apply(belowState))
                                        world.setBlockState(belowPos, Blocks.SANDSTONE.getDefaultState(), 2);
                                    else if (RED_SAND_MATCHER.apply(belowState))
                                        world.setBlockState(belowPos, Blocks.RED_SANDSTONE.getDefaultState(), 2);
                                }
                            }
                            else if (y >= 4)
                            {
                                if (isDirt && world.getLightFor(EnumSkyBlock.SKY, belowPos.up()) > 0)
                                {
                                    // note that some biomes don't use their top block or set it to something weird.
                                    // the nether's top block is unused, so it's grass. but there's not going to be skylight there anyway.
                                    // the end's top block is regular dirt. but the matcher doesn't match regular dirt.
                                    // so this is fine, i guess.
                                    IBlockState topBlock = world.getBiome(belowPos).topBlock;

                                    if (topBlock.getBlock() == Blocks.MYCELIUM || topBlock.getBlock() == Blocks.GRASS || DIRT_VARIANT_MATCHER.apply(topBlock))
                                        world.setBlockState(belowPos, topBlock, 2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // matching

    protected static final BlockStateMatcher DIRT_MATCHER = BlockStateMatcher.forBlock(Blocks.DIRT).where(BlockDirt.VARIANT,
            Predicates.equalTo(BlockDirt.DirtType.DIRT));
    protected static final BlockStateMatcher DIRT_VARIANT_MATCHER = BlockStateMatcher.forBlock(Blocks.DIRT).where(BlockDirt.VARIANT,
            Predicates.not(Predicates.equalTo(BlockDirt.DirtType.DIRT)));
    protected static final BlockStateMatcher SAND_MATCHER = BlockStateMatcher.forBlock(Blocks.SAND).where(BlockSand.VARIANT,
            Predicates.equalTo(BlockSand.EnumType.SAND));
    protected static final BlockStateMatcher RED_SAND_MATCHER = BlockStateMatcher.forBlock(Blocks.SAND).where(BlockSand.VARIANT,
            Predicates.equalTo(BlockSand.EnumType.RED_SAND));

}
