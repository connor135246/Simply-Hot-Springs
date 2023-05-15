package connor135246.simplyhotsprings.common.world.gen.feature;

import com.mojang.serialization.Codec;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

/**
 * pretty much copy-pasted from {@link net.minecraft.world.level.levelgen.feature.LakeFeature}
 */
public class HotSpringsFeature extends Feature<HotSpringsConfiguration>
{

    public HotSpringsFeature(Codec<HotSpringsConfiguration> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<HotSpringsConfiguration> context)
    {
        WorldGenLevel glevel = context.level();
        RandomSource rand = context.random();
        BlockPos origin = context.origin();
        HotSpringsConfiguration config = context.config();

        if (origin.getY() < glevel.getMinBuildHeight() + config.featureSizeXZ() / 2 + 1)
            return false;

        final BlockPos pos = origin.below(config.featureSizeY() / 2);
        final boolean postProcess = glevel.getChunk(pos).getStatus().getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK;

        final boolean[] blocksChanged = new boolean[config.featureSizeXZ() * config.featureSizeXZ() * config.featureSizeY()];
        final int spheres = config.spheres().sample(rand);

        final int sphereDiameterXZExtraBelowLevelValue = config.sphereDiameterXZExtraBelowLevel().sample(rand);

        for (int s = 0; s < spheres; ++s)
        {
            double diameterY = config.sphereDiameterY().sample(rand);
            double centerY = rand.nextDouble() * (config.featureSizeY() - diameterY - config.spherePaddingY() * 2) + config.spherePaddingY() + diameterY / 2.0D;

            double diameterX = config.sphereDiameterXZ().sample(rand);
            double diameterZ = config.sphereDiameterXZ().sample(rand);

            if (sphereDiameterXZExtraBelowLevelValue != 0 && (sphereDiameterXZExtraBelowLevelValue > 0 ? centerY < sphereDiameterXZExtraBelowLevelValue
                    : centerY > config.featureSizeY() + sphereDiameterXZExtraBelowLevelValue))
            {
                diameterX += config.sphereDiameterXZExtra().sample(rand);
                diameterZ += config.sphereDiameterXZExtra().sample(rand);
            }

            double centerX = rand.nextDouble() * (config.featureSizeXZ() - diameterX - config.spherePaddingXZ() * 2)
                    + config.spherePaddingXZ() + diameterX / 2.0D;
            double centerZ = rand.nextDouble() * (config.featureSizeXZ() - diameterZ - config.spherePaddingXZ() * 2)
                    + config.spherePaddingXZ() + diameterZ / 2.0D;

            for (int x = 1; x < config.featureSizeXZ() - 1; ++x)
            {
                for (int z = 1; z < config.featureSizeXZ() - 1; ++z)
                {
                    for (int y = 1; y < config.featureSizeY() - 1; ++y)
                    {
                        double xRadii = ((double) x - centerX) / (diameterX / 2.0D);
                        double yRadii = ((double) y - centerY) / (diameterY / 2.0D);
                        double zRadii = ((double) z - centerZ) / (diameterZ / 2.0D);
                        double radiiSquared = xRadii * xRadii + yRadii * yRadii + zRadii * zRadii;

                        if (radiiSquared < 1.0D)
                            blocksChanged[(x * config.featureSizeXZ() + z) * config.featureSizeY() + y] = true;
                    }
                }
            }
        }

        final BlockState theFluid = config.fluid().getState(rand, pos);
        final boolean normalFluid = config.fluidLevel() >= 0; // as opposed to a fluid that's upside down.

        for (int x = 0; x < config.featureSizeXZ(); ++x)
        {
            for (int z = 0; z < config.featureSizeXZ(); ++z)
            {
                for (int y = 0; y < config.featureSizeY(); ++y)
                {
                    if (isBorderPos(config, blocksChanged, x, y, z))
                    {
                        BlockPos checkPos = pos.offset(x, y, z);
                        BlockState checkState = glevel.getBlockState(checkPos);

                        if (isWithinSolidCheckBelowLevel(config, y))
                        {
                            if (!checkState.getMaterial().isSolid() && !checkState.is(theFluid.getBlock()))
                                return false;
                        }
                        else
                        {
                            if (checkState.getMaterial().isLiquid())
                                return false;
                        }
                    }
                }
            }
        }

        if (SimplyHotSpringsConfig.SERVER.debug.get())
            SimplyHotSprings.log.info("Generated a hot spring around {} {} {}",
                    pos.getX() + config.featureSizeXZ() / 2, pos.getY() + config.featureSizeY() / 2, pos.getZ() + config.featureSizeXZ() / 2);

        for (int x = 0; x < config.featureSizeXZ(); ++x)
        {
            for (int z = 0; z < config.featureSizeXZ(); ++z)
            {
                for (int y = 0; y < config.featureSizeY(); ++y)
                {
                    // if this block will be changed:
                    if (blocksChanged[(x * config.featureSizeXZ() + z) * config.featureSizeY() + y])
                    {
                        // set the fluid here
                        BlockPos setPos = pos.offset(x, y, z);
                        BlockState setState = theFluid;
                        boolean didSetFluid = false;
                        if (canReplaceBlock(glevel.getBlockState(setPos)))
                        {
                            if (!isWithinFluidLevel(config, y))
                                setState = Blocks.AIR.defaultBlockState();
                            else
                            {
                                // handle vaporization. water is turned to air, blocks with a waterlogged property have it set to false. this may not work with every case.
                                FluidState fluidState = setState.getFluidState();
                                if (!fluidState.isEmpty() && fluidState.getFluidType().isVaporizedOnPlacement(
                                        glevel.getLevel(), setPos, new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME)))
                                {
                                    if (setState.hasProperty(BlockStateProperties.WATERLOGGED))
                                        setState = setState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
                                    else if (setState.getBlock() == fluidState.createLegacyBlock().getBlock())
                                        setState = Blocks.AIR.defaultBlockState();
                                }
                            }

                            // actually set the fluid!
                            didSetFluid = glevel.setBlock(setPos, setState, 2);
                            // schedule the fluid to flow
                            FluidState fluidState = setState.getFluidState();
                            if (!fluidState.isEmpty())
                                glevel.scheduleTick(setPos, fluidState.getType(), 0);
                            // schedule air blocks for some reason
                            if (setState.isAir())
                                glevel.scheduleTick(setPos, setState.getBlock(), 0);
                        }

                        // if the block below this one won't be changed:
                        if (y <= 0 || !blocksChanged[(x * config.featureSizeXZ() + z) * config.featureSizeY() + (y - 1)])
                        {
                            BlockPos belowPos = pos.offset(x, y - 1, z);
                            BlockState belowState = glevel.getBlockState(belowPos);
                            if (canReplaceBlock(belowState))
                            {
                                // if this block is above the fluid level, do biome grasses
                                if (!isWithinFluidLevel(config, y))
                                {
                                    Holder<Biome> biome = glevel.getBiome(pos);
                                    boolean isCrimsonForest = biome.is(key -> key == Biomes.CRIMSON_FOREST);
                                    if (isCrimsonForest || biome.is(key -> key == Biomes.WARPED_FOREST))
                                    {
                                        if (belowState.is(Blocks.NETHERRACK) && glevel.isEmptyBlock(belowPos.above()))
                                            glevel.setBlock(belowPos, isCrimsonForest ? Blocks.CRIMSON_NYLIUM.defaultBlockState()
                                                    : Blocks.WARPED_NYLIUM.defaultBlockState(), 2);
                                    }
                                    // can't do glevel.getBrightness - lighting manager sometimes isn't ready in adjacent chunks
                                    if (belowState.is(Blocks.DIRT) && glevel.isEmptyBlock(belowPos.above()))
                                        glevel.setBlock(belowPos, biomeGrasses.get(biome), 2);
                                }
                                // modify the bottom of the lake
                                else if (normalFluid)
                                    modifyBlocksHoldingFluid(belowPos, belowState, glevel);
                            }
                        }

                        // if the block above this one won't be changed:
                        if (y >= config.featureSizeY() - 1 || !blocksChanged[(x * config.featureSizeXZ() + z) * config.featureSizeY() + (y + 1)])
                        {
                            // modify the "bottom" of the lake for upside down fluids
                            BlockPos abovePos = pos.offset(x, y + 1, z);
                            BlockState aboveState = glevel.getBlockState(abovePos);
                            boolean didSetAbove = false;
                            if (!normalFluid && canReplaceBlock(aboveState) && isWithinFluidLevel(config, y))
                                didSetAbove = modifyBlocksHoldingFluid(abovePos, aboveState, glevel);

                            // postprocess
                            if (postProcess)
                            {
                                if (didSetAbove)
                                    markAboveForPostProcessing(glevel, abovePos);
                                else if (didSetFluid)
                                    markAboveForPostProcessing(glevel, setPos);
                            }
                        }
                    }
                }
            }
        }

        if (config.barrier().isPresent())
        {
            for (int x = 0; x < config.featureSizeXZ(); ++x)
            {
                for (int z = 0; z < config.featureSizeXZ(); ++z)
                {
                    for (int y = 0; y < config.featureSizeY(); ++y)
                    {
                        if (isBorderPos(config, blocksChanged, x, y, z) && (isWithinFluidLevel(config, y) || rand.nextInt(2) != 0))
                        {
                            BlockPos borderPos = pos.offset(x, y, z);
                            BlockState borderState = glevel.getBlockState(borderPos);
                            if (borderState.getMaterial().isSolid() && !borderState.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE))
                            {
                                glevel.setBlock(borderPos, config.barrier().get().getState(rand, borderPos), 2);
                                if (postProcess)
                                    markAboveForPostProcessing(glevel, borderPos);
                            }
                        }
                    }
                }
            }
        }

        // note: depend on freeze_top_layer to freeze water instead of doing it here

        // note: postprocess console spam if you /place feature simplyhotsprings:hot_springs_big_wellspring -> caused by the vanilla lake feature.
        if (config.additionalPlacedFeatures().isPresent())
            config.additionalPlacedFeatures().get().forEach(placedFeature -> placedFeature.get().place(glevel, context.chunkGenerator(), rand, pos));

        return true;
    }

    private static boolean modifyBlocksHoldingFluid(BlockPos pos, BlockState state, WorldGenLevel glevel)
    {
        if (state.getMaterial().isSolid())
        {
            if (isDirt(state) || state.is(BlockTags.SNOW) || state.is(BlockTags.ICE))
                return glevel.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
            else if (state.is(Blocks.SAND))
                return glevel.setBlock(pos, Blocks.SANDSTONE.defaultBlockState(), 2);
            else if (state.is(Blocks.RED_SAND))
                return glevel.setBlock(pos, Blocks.RED_SANDSTONE.defaultBlockState(), 2);
        }
        return false;
    }

    private static boolean isWithinFluidLevel(HotSpringsConfiguration config, int y)
    {
        return config.fluidLevel() >= 0 ? y <= config.fluidLevel() : y >= config.featureSizeY() + config.fluidLevel();
    }

    private static boolean isWithinSolidCheckBelowLevel(HotSpringsConfiguration config, int y)
    {
        return config.solidCheckBelowLevel() >= 0 ? y < config.solidCheckBelowLevel() : y > config.featureSizeY() + config.solidCheckBelowLevel();
    }

    /**
     * checks if the given position is one that's adjacent to the excavated area
     */
    private static boolean isBorderPos(HotSpringsConfiguration config, boolean[] blocksChanged, int x, int y, int z)
    {
        return !blocksChanged[(x * config.featureSizeXZ() + z) * config.featureSizeY() + y]
                && (x < config.featureSizeXZ() - 1 && blocksChanged[((x + 1) * config.featureSizeXZ() + z) * config.featureSizeY() + y]
                        || x > 0 && blocksChanged[((x - 1) * config.featureSizeXZ() + z) * config.featureSizeY() + y]
                        || z < config.featureSizeXZ() - 1 && blocksChanged[(x * config.featureSizeXZ() + z + 1) * config.featureSizeY() + y]
                        || z > 0 && blocksChanged[(x * config.featureSizeXZ() + (z - 1)) * config.featureSizeY() + y]
                        || y < config.featureSizeY() - 1 && blocksChanged[(x * config.featureSizeXZ() + z) * config.featureSizeY() + y + 1]
                        || y > 0 && blocksChanged[(x * config.featureSizeXZ() + z) * config.featureSizeY() + (y - 1)]);
    }

    private static boolean canReplaceBlock(BlockState state)
    {
        return !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    //

    /**
     * the map of biomes to biome-specific grasses. damn you surface rules!!!
     */
    private static final Object2ObjectOpenHashMap<Holder<Biome>, BlockState> biomeGrasses = Util.make(
            new Object2ObjectOpenHashMap<Holder<Biome>, BlockState>(10, 0.95F), map -> map.defaultReturnValue(Blocks.GRASS_BLOCK.defaultBlockState()));

    public static void addBiomeGrass(Holder<Biome> biome, BlockState state)
    {
        biomeGrasses.put(biome, state);
    }

    public static void clearBiomeGrasses()
    {
        biomeGrasses.clear();
    }

}
