package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;

/**
 * Fishing bobbers in hot spring water will never start catching a fish.
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity
{

    public FishingHookMixin(EntityType<?> entityTypeIn, Level level)
    {
        super(entityTypeIn, level);
    }

    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    public void onCatchingFish(BlockPos pos, CallbackInfo ci)
    {
        if (this.level.getFluidState(pos).is(SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER))
            ci.cancel();
    }

}
