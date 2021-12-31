package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.TallSeaGrassBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.ITag;

/**
 * Instead of becoming waterlogged with regular water when placed in hot spring water, tall seagrass simply can't be placed in hot spring water. <br>
 * Note: tall seagrass doesn't actually have a BlockItem. I don't think that its getStateForPlacement method is ever called. Well, this is here just in case.
 */
@Mixin(TallSeaGrassBlock.class)
public abstract class TallSeaGrassBlockMixin extends DoublePlantBlock
{

    public TallSeaGrassBlockMixin(Properties properties)
    {
        super(properties);
    }

    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isTagged(Lnet/minecraft/tags/ITag;)Z"))
    public boolean redirectWaterTag(FluidState fluidstate, ITag<Fluid> waterTag)
    {
        return SimplyHotSprings.redirectWaterTag(fluidstate, waterTag);
    }

}