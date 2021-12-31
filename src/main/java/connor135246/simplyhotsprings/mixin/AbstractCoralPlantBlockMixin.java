package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.block.AbstractCoralPlantBlock;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.ITag;

/**
 * Coral plants will not become waterlogged with regular water when placed in hot spring water.
 */
@Mixin(AbstractCoralPlantBlock.class)
public abstract class AbstractCoralPlantBlockMixin extends Block
{

    public AbstractCoralPlantBlockMixin(Properties properties)
    {
        super(properties);
    }

    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isTagged(Lnet/minecraft/tags/ITag;)Z"))
    public boolean redirectWaterTag(FluidState fluidstate, ITag<Fluid> waterTag)
    {
        return SimplyHotSprings.redirectWaterTag(fluidstate, waterTag);
    }

}