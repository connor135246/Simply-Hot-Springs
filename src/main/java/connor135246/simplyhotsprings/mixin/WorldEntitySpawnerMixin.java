package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.ITag;
import net.minecraft.world.spawner.WorldEntitySpawner;

/**
 * Water mobs will not spawn in hot spring water.
 */
@Mixin(WorldEntitySpawner.class)
public abstract class WorldEntitySpawnerMixin
{

    @Redirect(method = "canSpawnAtBody", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isTagged(Lnet/minecraft/tags/ITag;)Z"))
    private static boolean redirectWaterTag(FluidState fluidstate, ITag<Fluid> waterTag)
    {
        return SimplyHotSprings.redirectWaterTag(fluidstate, waterTag);
    }

}
