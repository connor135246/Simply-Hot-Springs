package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Instead of becoming waterlogged with regular water when placed in hot spring water, seagrass simply can't be placed in hot spring water.
 */
@Mixin(SeagrassBlock.class)
public abstract class SeagrassBlockMixin extends BushBlock
{

    public SeagrassBlockMixin(Properties properties)
    {
        super(properties);
    }

    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean redirectWaterTag(FluidState fluidstate, TagKey<Fluid> waterTag)
    {
        return SimplyHotSprings.redirectWaterTag(fluidstate, waterTag);
    }

}