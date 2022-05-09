package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Instead of becoming waterlogged with regular water when placed in hot spring water, kelp simply can't be placed in hot spring water.
 */
@Mixin(KelpBlock.class)
public abstract class KelpHeadBlockMixin extends GrowingPlantHeadBlock
{

    protected KelpHeadBlockMixin(Properties properties, Direction direction, VoxelShape shape, boolean waterloggable, double growthChance)
    {
        super(properties, direction, shape, waterloggable, growthChance);
    }

    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean redirectWaterTag(FluidState fluidstate, TagKey<Fluid> waterTag)
    {
        return SimplyHotSprings.redirectWaterTag(fluidstate, waterTag);
    }

}