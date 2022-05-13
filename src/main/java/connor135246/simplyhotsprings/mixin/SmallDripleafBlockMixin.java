package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small Dripleafs can't be placed in hot spring water. TODO: instead allow small dripleafs to be placed in hot spring water without them becoming waterlogged
 */
@Mixin(SmallDripleafBlock.class)
public abstract class SmallDripleafBlockMixin extends DoublePlantBlock
{

    public SmallDripleafBlockMixin(Properties properties)
    {
        super(properties);
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public void onGetStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<Boolean> cir)
    {
        BlockState state = super.getStateForPlacement(context);
        BlockPos pos;
        if (state == null || context.getLevel().getFluidState(pos = context.getClickedPos()).is(SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER)
                || context.getLevel().getFluidState(pos.above()).is(SimplyHotSpringsCommon.TAG_HOT_SPRING_WATER))
            cir.setReturnValue(null);
    }

}
