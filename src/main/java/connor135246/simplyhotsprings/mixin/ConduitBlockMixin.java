package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.ConduitBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Conduits will not become waterlogged with regular water when placed in hot spring water.
 */
@Mixin(ConduitBlock.class)
public abstract class ConduitBlockMixin extends BaseEntityBlock
{

    protected ConduitBlockMixin(Properties properties)
    {
        super(properties);
    }

    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean redirectWaterTag(FluidState fluidstate, TagKey<Fluid> waterTag)
    {
        return SimplyHotSprings.redirectWaterTag(fluidstate, waterTag);
    }

}