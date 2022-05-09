package connor135246.simplyhotsprings.common.blocks;

import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class HotSpringWaterBlock extends LiquidBlock
{

    public HotSpringWaterBlock()
    {
        super(SimplyHotSpringsCommon.HOT_SPRING_WATER,
                BlockBehaviour.Properties.of(Material.WATER, MaterialColor.DIAMOND).noCollission().strength(100.0F).noDrops());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
    {
        super.entityInside(state, level, pos, entity);

        if (!level.isClientSide && entity instanceof LivingEntity)
            SimplyHotSpringsConfig.addHotSpringsEffect((LivingEntity) entity);
    }

}
