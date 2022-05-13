package connor135246.simplyhotsprings.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.BushBlock;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.PlantType;

/**
 * Allows modded blocks that extend LilyPadBlock (and don't override this method) to be placed on any fluid with a water material, including hot spring water. <br>
 * Note: vanilla lily pads can be placed on hot spring water without this mixin because of the default value of this method. This is just for modded lily pads that don't indicate
 * their PlantType.
 */
@Mixin(LilyPadBlock.class)
public class LilyPadBlockMixin extends BushBlock
{

    public LilyPadBlockMixin(Properties properties)
    {
        super(properties);
    }

    @Override
    public PlantType getPlantType(IBlockReader world, BlockPos pos)
    {
        return PlantType.WATER;
    }

}
