package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraftforge.common.PlantType;

/**
 * Allows modded blocks that extend WaterlilyBlock (and don't override this method) to be placed on any fluid with a water material, including hot spring water. <br>
 * Note: vanilla lily pads can be placed on hot spring water without this mixin because of the default value of this method. This is just for modded lily pads that don't indicate
 * their PlantType.
 */
@Mixin(WaterlilyBlock.class)
public class WaterlilyBlockMixin extends BushBlock
{

    public WaterlilyBlockMixin(Properties properties)
    {
        super(properties);
    }

    @Override
    public PlantType getPlantType(BlockGetter world, BlockPos pos)
    {
        return PlantType.WATER;
    }

}
