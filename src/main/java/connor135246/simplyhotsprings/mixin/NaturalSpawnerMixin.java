package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import connor135246.simplyhotsprings.SimplyHotSprings;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Water mobs will not spawn in hot spring water.
 */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin
{

    @Redirect(method = "canSpawnAtBody", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean redirectWaterTag(FluidState fluidstate, TagKey<Fluid> waterTag)
    {
        return SimplyHotSprings.redirectWaterTag(fluidstate, waterTag);
    }

}
