package connor135246.simplyhotsprings.common.fluids;

import java.util.function.Consumer;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

public class HotSpringWaterFluidType extends FluidType
{

    public HotSpringWaterFluidType()
    {
        super(FluidType.Properties.create()
                .fallDistanceModifier(0.0F)
                .canDrown(true)
                .canSwim(true)
                .pathType(BlockPathTypes.WATER)
                .adjacentPathType(BlockPathTypes.WATER_BORDER)
                .canExtinguish(true)
                .supportsBoating(true)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                .canHydrate(true)
                .temperature(325));
    }

    @Override
    public boolean canConvertToSource(FluidStack stack)
    {
        return SimplyHotSpringsConfig.SERVER.createsSources.get();
    }

    @Override
    public boolean canConvertToSource(FluidState state, LevelReader reader, BlockPos pos)
    {
        return SimplyHotSpringsConfig.SERVER.createsSources.get();
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
    {
        consumer.accept(new IClientFluidTypeExtensions() {

            private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png"),
                    STILL_TEXTURE = new ResourceLocation(SimplyHotSprings.MODID, "block/still_hot_springs"),
                    FLOWING_TEXTURE = new ResourceLocation(SimplyHotSprings.MODID, "block/flowing_hot_springs"),
                    WATER_OVERLAY = new ResourceLocation("block/water_overlay");

            @Override
            public ResourceLocation getStillTexture()
            {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture()
            {
                return FLOWING_TEXTURE;
            }

            @Override
            public ResourceLocation getOverlayTexture()
            {
                return WATER_OVERLAY;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc)
            {
                return UNDERWATER_LOCATION;
            }

            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount,
                    Vector3f fluidFogColor)
            {
                // reduce red; set green equal to blue
                return new Vector3f(fluidFogColor.x() * 0.01F, fluidFogColor.z(), fluidFogColor.z());
            }

            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance,
                    FogShape shape)
            {
                float fogStart = -8.0F;
                float fogEnd = 96.0F;
                if (camera.getEntity() instanceof LocalPlayer)
                {
                    fogEnd *= 0.7875;
                    // TODO if forge ever lets custom fluids use waterVisionTime, do it like this instead:
                    // fogEnd *= 0.15F + Math.max(0.25F, ((LocalPlayer) camera.getEntity()).getWaterVision()) * 0.85F;
                }

                if (fogEnd > renderDistance)
                {
                    fogEnd = renderDistance;
                    RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                }

                RenderSystem.setShaderFogStart(fogStart);
                RenderSystem.setShaderFogEnd(fogEnd);
            }

        });
    }

}
