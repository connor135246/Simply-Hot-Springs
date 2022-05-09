package connor135246.simplyhotsprings.common.fluids;

import static connor135246.simplyhotsprings.common.SimplyHotSpringsCommon.*;

import java.util.Random;

import javax.annotation.Nullable;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class HotSpringWaterFluid extends ForgeFlowingFluid
{

    public static final ResourceLocation STILL_TEXTURE = new ResourceLocation(SimplyHotSprings.MODID, "block/still_hot_springs");
    public static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation(SimplyHotSprings.MODID, "block/flowing_hot_springs");

    public HotSpringWaterFluid()
    {
        super(new ForgeFlowingFluid.Properties(HOT_SPRING_WATER, FLOWING_HOT_SPRING_WATER, FluidAttributes.builder(STILL_TEXTURE, FLOWING_TEXTURE)
                .color(0xFFFFFFFF).temperature(325).sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY))
                        .block(HOT_SPRING_WATER_BLOCK).bucket(HOT_SPRING_WATER_BUCKET));
    }

    @Override
    protected boolean canConvertToSource()
    {
        return SimplyHotSpringsConfig.COMMON.createsSources.get();
    }

    @Override
    protected boolean isSolidFace(BlockGetter level, BlockPos neighborPos, Direction side)
    {
        return false;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction)
    {
        return direction == Direction.DOWN && !state.is(FluidTags.WATER);
    }

    @Override
    public void animateTick(Level level, BlockPos pos, FluidState state, Random rand)
    {
        // steam particles
        BlockPos posAbove = pos.above();
        if (HOT_SPRING_WATER_STEAM.isPresent() && level.getBlockState(posAbove).propagatesSkylightDown(level, posAbove) && rand.nextInt(24) == 0
                && rand.nextInt(Minecraft.getInstance().options.particles.getId() + 1) == 0)
        {
            level.addParticle(HOT_SPRING_WATER_STEAM.get(), posAbove.getX() + 0.1F + rand.nextFloat() * 0.8F, posAbove.getY() + 0.5F,
                    posAbove.getZ() + 0.1F + rand.nextFloat() * 0.8F, 0.0D, 0.0D, 0.0D);
        }

        // flowing sound
        if (!state.isSource() && !state.getValue(FALLING))
        {
            if (rand.nextInt(64) == 0)
            {
                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS,
                        rand.nextFloat() * 0.25F + 0.75F, rand.nextFloat() + 0.5F, false);
            }
        }
        // suspended particle
        else if (rand.nextInt(10) == 0)
        {
            HOT_SPRING_WATER_UNDERWATER.ifPresent(suspended -> level.addParticle(suspended, pos.getX() + rand.nextDouble(), pos.getY() + rand.nextDouble(),
                    pos.getZ() + rand.nextDouble(), 0.01D * rand.nextDouble(), 0.075D + rand.nextDouble() * 0.025D, 0.01D * rand.nextDouble()));
        }
    }

    @Override
    @Nullable
    public ParticleOptions getDripParticle()
    {
        return DRIPPING_HOT_SPRING_WATER.orElse(null);
    }

    public static class Source extends HotSpringWaterFluid
    {
        @Override
        public int getAmount(FluidState state)
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
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
        {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state)
        {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state)
        {
            return false;
        }
    }

}
