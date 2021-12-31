package connor135246.simplyhotsprings.common.blocks;

import connor135246.simplyhotsprings.common.SimplyHotSpringsCommon;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HotSpringWaterBlock extends FlowingFluidBlock
{

    public HotSpringWaterBlock()
    {
        super(SimplyHotSpringsCommon.HOT_SPRING_WATER,
                AbstractBlock.Properties.create(Material.WATER, MaterialColor.DIAMOND).doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
    {
        super.onEntityCollision(state, world, pos, entity);

        if (!world.isRemote && entity instanceof LivingEntity)
            SimplyHotSpringsConfig.addHotSpringsEffect((LivingEntity) entity);
    }

}
