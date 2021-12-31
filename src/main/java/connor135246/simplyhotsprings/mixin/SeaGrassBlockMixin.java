package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.block.BushBlock;
import net.minecraft.block.SeaGrassBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.ITag;

/**
 * Instead of becoming waterlogged with regular water when placed in hot spring water, seagrass simply can't be placed in hot spring water.
 */
@Mixin(SeaGrassBlock.class)
public abstract class SeaGrassBlockMixin extends BushBlock
{

    public SeaGrassBlockMixin(Properties properties)
    {
        super(properties);
    }

    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isTagged(Lnet/minecraft/tags/ITag;)Z"))
    public boolean redirectWaterTag(FluidState fluidstate, ITag<Fluid> waterTag)
    {
        return SimplyHotSprings.redirectWaterTag(fluidstate, waterTag);
    }

}