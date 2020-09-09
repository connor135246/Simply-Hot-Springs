package connor135246.simplyhotsprings.common.fluids;

import connor135246.simplyhotsprings.util.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class FluidHotSpringWater extends Fluid
{

    public static final String FLUID_NAME = "hot_spring_water";

    public static final Fluid FLUID_INSTANCE = new FluidHotSpringWater();

    public FluidHotSpringWater()
    {
        super(FLUID_NAME, new ResourceLocation(Reference.MODID, "blocks/still_hot_springs"),
                new ResourceLocation(Reference.MODID, "blocks/flowing_hot_springs"));

        this.setTemperature(325);
    }

}
