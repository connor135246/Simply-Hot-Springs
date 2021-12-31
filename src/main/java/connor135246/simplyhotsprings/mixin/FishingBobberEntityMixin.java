package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Fishing bobbers in hot spring water will never start catching a fish.
 */
@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends Entity
{

    public FishingBobberEntityMixin(EntityType<?> entityTypeIn, World worldIn)
    {
        super(entityTypeIn, worldIn);
    }

    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    public void onCatchingFish(BlockPos pos, CallbackInfo ci)
    {
        if (this.world.getFluidState(pos).isTagged(SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER))
            ci.cancel();
    }

}
