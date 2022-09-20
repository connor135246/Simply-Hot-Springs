package connor135246.simplyhotsprings.common.blocks;

import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import connor135246.simplyhotsprings.client.particles.ParticleSteam;
import connor135246.simplyhotsprings.common.CommonProxy;
import connor135246.simplyhotsprings.common.fluids.FluidHotSpringWater;
import connor135246.simplyhotsprings.util.Reference;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockHotSpringWater extends BlockFluidClassic
{

    public static final Block BLOCK_INSTANCE = new BlockHotSpringWater();
    public static final ItemBlock ITEMBLOCK_INSTANCE = (ItemBlock) new ItemBlock(BLOCK_INSTANCE).setRegistryName(BLOCK_INSTANCE.getRegistryName());

    /** The potion effect that hot spring water applies. */
    public static Potion potionEffect = MobEffects.REGENERATION;
    public static int timer = 50;
    public static int amplifier = 0;

    public BlockHotSpringWater()
    {
        super(CommonProxy.fluidToUse, Material.WATER, MapColor.DIAMOND);
        this.setUnlocalizedName(FluidHotSpringWater.FLUID_NAME);
        this.setRegistryName(Reference.MODID, FluidHotSpringWater.FLUID_NAME);

        this.setHardness(100.0F);
        this.setLightOpacity(1);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
    {
        super.onEntityCollidedWithBlock(world, pos, state, entity);

        if (!world.isRemote && potionEffect != null && entity instanceof EntityLivingBase && isWithinFluid(world, pos, entity.posY + 1F - quantaFraction)
                && !((EntityLivingBase) entity).isPotionActive(potionEffect))
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(potionEffect, timer, amplifier, true, true));
    }

    @Override
    protected boolean causesDownwardCurrent(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand)
    {
        super.randomDisplayTick(state, world, pos, rand);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int level = ((Integer) state.getValue(LEVEL)).intValue();

        // steam particles
        if (world.getBlockLightOpacity(pos.up()) == 0)
        {
            if (SimplyHotSpringsConfig.Config.alternateParticles)
            {
                if (rand.nextInt(12) == 0)
                    world.spawnParticle(EnumParticleTypes.CLOUD, x + 0.1F + rand.nextFloat() * 0.8F, y + 1.2F, z + 0.1F + rand.nextFloat() * 0.8F,
                            0.0D, 0.025 + rand.nextFloat() / 250.0F, 0.0D);
            }
            else if (rand.nextInt(24) == 0)
            {
                Particle steam = new ParticleSteam(world, x + 0.1F + rand.nextFloat() * 0.8F, y + 1.2F, z + 0.1F + rand.nextFloat() * 0.8F);
                Minecraft.getMinecraft().effectRenderer.addEffect(steam);
            }
        }

        // vanilla water stuff
        if (0 < level && level < 8)
        {
            if (rand.nextInt(64) == 0)
            {
                world.playSound(x + 0.5D, y + 0.5D, z + 0.5D, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS, rand.nextFloat() * 0.25F + 0.75F,
                        rand.nextFloat() + 0.5F, false);
            }
        }
        else if (rand.nextInt(10) == 0)
        {
            Particle suspended = Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(EnumParticleTypes.SUSPENDED.getParticleID(),
                    x + (double) rand.nextFloat(), y + (double) rand.nextFloat(), z + (double) rand.nextFloat(), 0.0, 0.0, 0.0);
            suspended.multiplyVelocity(rand.nextFloat() * 0.25F); // makes them float up
            suspended.setRBGColorF(0.1F, 0.7F, 0.7F);
        }

        if (rand.nextInt(10) == 0 && world.getBlockState(pos.down()).isSideSolid(world, pos, EnumFacing.UP))
        {
            Material material = world.getBlockState(pos.down(2)).getMaterial();

            if (!material.blocksMovement() && !material.isLiquid())
            {
                world.spawnParticle(EnumParticleTypes.DRIP_WATER, x + (double) rand.nextFloat(), y - 1.05D, z + (double) rand.nextFloat(), 0.0D, 0.0D, 0.0D);
                // can't change the colour of this :(
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks)
    {
        if (!isWithinFluid(world, pos, ActiveRenderInfo.projectViewFromEntity(entity, partialTicks).y))
        {
            BlockPos otherPos = pos.down(densityDir);
            IBlockState otherState = world.getBlockState(otherPos);
            return otherState.getBlock().getFogColor(world, otherPos, otherState, entity, originalColor, partialTicks);
        }

        if (getFluid() != null)
        {
            float fogModifier = 0.0F;
            if (entity instanceof EntityLivingBase)
            {
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                fogModifier = EnchantmentHelper.getRespirationModifier(entityLivingBase) * 0.1F;

                if (entityLivingBase.isPotionActive(MobEffects.WATER_BREATHING))
                    fogModifier = fogModifier * 0.3F + 0.3F;
            }
            return new Vec3d(0.01F + fogModifier, 0.3F + fogModifier, 0.3F + fogModifier);
        }

        return super.getFogColor(world, pos, state, entity, originalColor, partialTicks);
    }

    protected boolean isWithinFluid(IBlockAccess world, BlockPos pos, double y)
    {
        float filled = getFilledPercentage(world, pos);
        return filled < 0 ? y > pos.getY() + filled + 1 : y < pos.getY() + filled;
    }

    /**
     * Updates {@link #canCreateSources} and {@link #potionEffect} to the config setting. Called after postInit or when the config changes.
     */
    public static void updateConfigSettings()
    {
        ((BlockHotSpringWater) BLOCK_INSTANCE).canCreateSources = SimplyHotSpringsConfig.Config.createsSources;

        if (!StringUtils.isBlank(SimplyHotSpringsConfig.Config.potionEffect))
        {
            potionEffect = ForgeRegistries.POTIONS.getValue(new ResourceLocation(SimplyHotSpringsConfig.Config.potionEffect));
            if (potionEffect == null)
                SimplyHotSpringsConfig.warnInvalidEntry("Potion Effect", SimplyHotSpringsConfig.Config.potionEffect);
        }
        else
            potionEffect = null;

        timer = SimplyHotSpringsConfig.Config.potionEffectSettings.length > 0 ? Math.max(SimplyHotSpringsConfig.Config.potionEffectSettings[0], 1) : 50;
        amplifier = SimplyHotSpringsConfig.Config.potionEffectSettings.length > 1
                ? MathHelper.clamp(SimplyHotSpringsConfig.Config.potionEffectSettings[1], 0, 255)
                : 0;
    }

}
