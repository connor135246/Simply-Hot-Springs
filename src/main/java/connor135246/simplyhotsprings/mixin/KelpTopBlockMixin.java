package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.block.AbstractTopPlantBlock;
import net.minecraft.block.KelpTopBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.ITag;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;

/**
 * Instead of becoming waterlogged with regular water when placed in hot spring water, kelp simply can't be placed in hot spring water.
 */
@Mixin(KelpTopBlock.class)
public abstract class KelpTopBlockMixin extends AbstractTopPlantBlock
{

    protected KelpTopBlockMixin(Properties properties, Direction direction, VoxelShape shape, boolean waterloggable, double growthChance)
    {
        super(properties, direction, shape, waterloggable, growthChance);
    }

    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isTagged(Lnet/minecraft/tags/ITag;)Z"))
    public boolean redirectWaterTag(FluidState fluidstate, ITag<Fluid> waterTag)
    {
        return SimplyHotSprings.redirectWaterTag(fluidstate, waterTag);
    }

}