package connor135246.simplyhotsprings.common.fluids;

import static connor135246.simplyhotsprings.common.SimplyHotSpringsCommon.*;

import java.util.Random;

import javax.annotation.Nullable;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class HotSpringWaterFluid extends ForgeFlowingFluid
{

    public static final ResourceLocation STILL_TEXTURE = new ResourceLocation(SimplyHotSprings.MODID, "block/still_hot_springs");
    public static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation(SimplyHotSprings.MODID, "block/flowing_hot_springs");

    public HotSpringWaterFluid()
    {
        super(new ForgeFlowingFluid.Properties(HOT_SPRING_WATER, FLOWING_HOT_SPRING_WATER,
                FluidAttributes.builder(STILL_TEXTURE, FLOWING_TEXTURE).color(0xFFFFFFFF).temperature(325))
                        .block(HOT_SPRING_WATER_BLOCK).bucket(HOT_SPRING_WATER_BUCKET));
    }

    @Override
    protected boolean canSourcesMultiply()
    {
        return SimplyHotSpringsConfig.COMMON.createsSources.get();
    }

    @Override
    protected boolean causesDownwardCurrent(IBlockReader worldIn, BlockPos neighborPos, Direction side)
    {
        return false;
    }

    @Override
    protected boolean canDisplace(FluidState state, IBlockReader world, BlockPos pos, Fluid fluid, Direction direction)
    {
        return direction == Direction.DOWN && !fluid.isIn(FluidTags.WATER);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(World world, BlockPos pos, FluidState state, Random rand)
    {
        // steam particles
        BlockPos posAbove = pos.up();
        if (HOT_SPRING_WATER_STEAM.isPresent() && world.getBlockState(posAbove).propagatesSkylightDown(world, posAbove) && rand.nextInt(24) == 0
                && rand.nextInt(Minecraft.getInstance().gameSettings.particles.getId() + 1) == 0)
        {
            world.addParticle(HOT_SPRING_WATER_STEAM.get(), posAbove.getX() + 0.1F + rand.nextFloat() * 0.8F, posAbove.getY() + 0.5F,
                    posAbove.getZ() + 0.1F + rand.nextFloat() * 0.8F, 0.0D, 0.0D, 0.0D);
        }

        // flowing sound
        if (!state.isSource() && !state.get(FALLING))
        {
            if (rand.nextInt(64) == 0)
            {
                world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS,
                        rand.nextFloat() * 0.25F + 0.75F, rand.nextFloat() + 0.5F, false);
            }
        }
        // suspended particle
        else if (rand.nextInt(10) == 0)
        {
            HOT_SPRING_WATER_UNDERWATER.ifPresent(suspended -> world.addParticle(suspended, pos.getX() + rand.nextDouble(), pos.getY() + rand.nextDouble(),
                    pos.getZ() + rand.nextDouble(), 0.01D * rand.nextDouble(), 0.075D + rand.nextDouble() * 0.025D, 0.01D * rand.nextDouble()));
        }
    }

    @Override
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public IParticleData getDripParticleData()
    {
        return DRIPPING_HOT_SPRING_WATER.orElse(null);
    }

    public static class Source extends HotSpringWaterFluid
    {
        @Override
        public int getLevel(FluidState state)
        {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state)
        {
            return true;
        }
    }

    public static class Flowing extends HotSpringWaterFluid
    {
        public Flowing()
        {
            setDefaultState(getStateContainer().getBaseState().with(LEVEL_1_8, 7));
        }

        protected void fillStateContainer(StateContainer.Builder<Fluid, FluidState> builder)
        {
            super.fillStateContainer(builder);
            builder.add(LEVEL_1_8);
        }

        @Override
        public int getLevel(FluidState state)
        {
            return state.get(LEVEL_1_8);
        }

        @Override
        public boolean isSource(FluidState state)
        {
            return false;
        }
    }

}
