package connor135246.simplyhotsprings.common;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.blocks.HotSpringWaterBlock;
import connor135246.simplyhotsprings.common.fluids.HotSpringWaterFluid;
import connor135246.simplyhotsprings.common.world.gen.feature.HotSpringsFeature;
import connor135246.simplyhotsprings.common.world.gen.placement.CeilingAwareHeightmapPlacement;
import connor135246.simplyhotsprings.common.world.gen.placement.ConfigChancePlacement;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = SimplyHotSprings.MODID, bus = Bus.MOD)
public class SimplyHotSpringsCommon
{

    public static final String NAME = "hot_spring_water";

    // deferred registers

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SimplyHotSprings.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SimplyHotSprings.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, SimplyHotSprings.MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SimplyHotSprings.MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, SimplyHotSprings.MODID);
    public static final DeferredRegister<Placement<?>> DECORATORS = DeferredRegister.create(ForgeRegistries.DECORATORS, SimplyHotSprings.MODID);

    // registry objects

    public static final RegistryObject<FlowingFluidBlock> HOT_SPRING_WATER_BLOCK = BLOCKS.register(NAME, HotSpringWaterBlock::new);

    public static final RegistryObject<FlowingFluid> HOT_SPRING_WATER = FLUIDS.register(NAME, HotSpringWaterFluid.Source::new);
    public static final RegistryObject<FlowingFluid> FLOWING_HOT_SPRING_WATER = FLUIDS.register("flowing_" + NAME, HotSpringWaterFluid.Flowing::new);

    public static final RegistryObject<Item> HOT_SPRING_WATER_BUCKET = ITEMS.register(NAME + "_bucket",
            () -> new BucketItem(HOT_SPRING_WATER, new Item.Properties().maxStackSize(1).containerItem(Items.BUCKET).group(ItemGroup.MISC)));

    public static final RegistryObject<BasicParticleType> HOT_SPRING_WATER_STEAM = PARTICLES.register(NAME + "_steam", particleBasic(false));
    public static final RegistryObject<BasicParticleType> DRIPPING_HOT_SPRING_WATER = PARTICLES.register("dripping_" + NAME, particleBasic(false));
    public static final RegistryObject<BasicParticleType> FALLING_HOT_SPRING_WATER = PARTICLES.register("falling_" + NAME, particleBasic(false));
    public static final RegistryObject<BasicParticleType> SPLASHING_HOT_SPRING_WATER = PARTICLES.register("splashing_" + NAME, particleBasic(false));
    public static final RegistryObject<BasicParticleType> HOT_SPRING_WATER_UNDERWATER = PARTICLES.register(NAME + "_underwater", particleBasic(false));

    public static final RegistryObject<Feature<NoFeatureConfig>> HOT_SPRINGS_FEATURE = FEATURES.register("hot_springs",
            () -> new HotSpringsFeature(NoFeatureConfig.CODEC));
    public static final RegistryObject<ConfigChancePlacement> CONFIG_CHANCE_PLACEMENT = DECORATORS.register("config_chance",
            () -> new ConfigChancePlacement(NoPlacementConfig.CODEC));
    public static final RegistryObject<CeilingAwareHeightmapPlacement> CEILING_AWARE_HEIGHTMAP_PLACEMENT = DECORATORS.register("ceiling_aware_heightmap",
            () -> new CeilingAwareHeightmapPlacement(TopSolidRangeConfig.CODEC));

    // other

    public static @Nullable ConfiguredFeature<?, ?> CONFIGURED_HOT_SPRINGS_FEATURE = null;

    public static final ITag.INamedTag<Fluid> TAG_HOT_SPRING_WATER = FluidTags.createOptional(new ResourceLocation(SimplyHotSprings.MODID, NAME));

    /** copy-pasted from vanilla filled bucket behaviours from {@link net.minecraft.dispenser.IDispenseItemBehavior} */
    public static final IDispenseItemBehavior dispenseHotSpringWaterBehaviour = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior defaultBehaviour = new DefaultDispenseItemBehavior();

        public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
        {
            BucketItem bucketitem = (BucketItem) stack.getItem();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            World world = source.getWorld();
            if (bucketitem.tryPlaceContainedLiquid((PlayerEntity) null, world, blockpos, (BlockRayTraceResult) null))
            {
                bucketitem.onLiquidPlaced(world, stack, blockpos);
                return new ItemStack(Items.BUCKET);
            }
            else
                return this.defaultBehaviour.dispense(source, stack);
        }
    };

    // registering methods

    public static void register(IEventBus eventBus)
    {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        FLUIDS.register(eventBus);
        PARTICLES.register(eventBus);
        FEATURES.register(eventBus);
        DECORATORS.register(eventBus);
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            if (HOT_SPRINGS_FEATURE.isPresent() && CONFIG_CHANCE_PLACEMENT.isPresent() && CEILING_AWARE_HEIGHTMAP_PLACEMENT.isPresent())
            {
                CONFIGURED_HOT_SPRINGS_FEATURE = Registry.register(WorldGenRegistries.CONFIGURED_FEATURE,
                        new ResourceLocation(SimplyHotSprings.MODID, "hot_springs"),
                        getDefaultConfiguredHotSprings(HOT_SPRINGS_FEATURE.get(), CONFIG_CHANCE_PLACEMENT.get(), CEILING_AWARE_HEIGHTMAP_PLACEMENT.get()));
            }

            HOT_SPRING_WATER_BUCKET.ifPresent(bucket -> DispenserBlock.registerDispenseBehavior(bucket, dispenseHotSpringWaterBehaviour));
        });
    }

    // other methods

    private static ConfiguredFeature<?, ?> getDefaultConfiguredHotSprings(Feature<NoFeatureConfig> springs, ConfigChancePlacement chance,
            CeilingAwareHeightmapPlacement ceiling)
    {
        return springs.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
                .withPlacement(ceiling.configure(new TopSolidRangeConfig(5, 16, -1)))
                .square()
                .withPlacement(chance.configure(NoPlacementConfig.INSTANCE));
    }

    private static Supplier<BasicParticleType> particleBasic(boolean alwaysShow)
    {
        return () -> new BasicParticleType(alwaysShow);
    }

}
