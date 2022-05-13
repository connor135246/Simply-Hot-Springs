package connor135246.simplyhotsprings.common;

import static connor135246.simplyhotsprings.SimplyHotSprings.MODID;

import java.util.List;
import java.util.function.Supplier;

import connor135246.simplyhotsprings.common.blocks.HotSpringWaterBlock;
import connor135246.simplyhotsprings.common.fluids.HotSpringWaterFluid;
import connor135246.simplyhotsprings.common.world.gen.feature.HotSpringsFeature;
import connor135246.simplyhotsprings.common.world.gen.placement.CeilingAwareHeightmapPlacement;
import connor135246.simplyhotsprings.common.world.gen.placement.ConfigChanceFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class SimplyHotSpringsCommon
{

    public static final String NAME = "hot_spring_water";

    // deferred registers

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registry.BLOCK_REGISTRY, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registry.ITEM_REGISTRY, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registry.FLUID_REGISTRY, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registry.PARTICLE_TYPE_REGISTRY, MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registry.FEATURE_REGISTRY, MODID);
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, MODID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registry.PLACEMENT_MODIFIER_REGISTRY, MODID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, MODID);

    // registry objects

    public static final RegistryObject<LiquidBlock> HOT_SPRING_WATER_BLOCK = BLOCKS.register(NAME, HotSpringWaterBlock::new);

    public static final RegistryObject<FlowingFluid> HOT_SPRING_WATER = FLUIDS.register(NAME, HotSpringWaterFluid.Source::new);
    public static final RegistryObject<FlowingFluid> FLOWING_HOT_SPRING_WATER = FLUIDS.register("flowing_" + NAME, HotSpringWaterFluid.Flowing::new);

    public static final RegistryObject<Item> HOT_SPRING_WATER_BUCKET = ITEMS.register(NAME + "_bucket",
            () -> new BucketItem(HOT_SPRING_WATER, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET).tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<SimpleParticleType> HOT_SPRING_WATER_STEAM = PARTICLES.register(NAME + "_steam", particleSimple(false));
    public static final RegistryObject<SimpleParticleType> DRIPPING_HOT_SPRING_WATER = PARTICLES.register("dripping_" + NAME, particleSimple(false));
    public static final RegistryObject<SimpleParticleType> FALLING_HOT_SPRING_WATER = PARTICLES.register("falling_" + NAME, particleSimple(false));
    public static final RegistryObject<SimpleParticleType> SPLASHING_HOT_SPRING_WATER = PARTICLES.register("splashing_" + NAME, particleSimple(false));
    public static final RegistryObject<SimpleParticleType> HOT_SPRING_WATER_UNDERWATER = PARTICLES.register(NAME + "_underwater", particleSimple(false));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> HOT_SPRINGS_FEATURE = FEATURES.register("hot_springs",
            () -> new HotSpringsFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<ConfiguredFeature<?, ?>> CONFIGURED_HOT_SPRINGS_FEATURE = CONFIGURED_FEATURES.register("hot_springs",
            () -> new ConfiguredFeature<>(HOT_SPRINGS_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
    public static final RegistryObject<PlacementModifierType<?>> CONFIG_CHANCE_FILTER = PLACEMENT_MODIFIERS.register("config_chance",
            () -> ConfigChanceFilter.TYPE);
    public static final RegistryObject<PlacementModifierType<?>> CEILING_AWARE_HEIGHTMAP_PLACEMENT = PLACEMENT_MODIFIERS.register("ceiling_aware_heightmap",
            () -> CeilingAwareHeightmapPlacement.TYPE);
    public static final RegistryObject<PlacedFeature> PLACED_HOT_SPRINGS_FEATURE = PLACED_FEATURES.register("hot_springs_default",
            () -> new PlacedFeature(CONFIGURED_HOT_SPRINGS_FEATURE.getHolder().get(),
                    List.of(ConfigChanceFilter.configChance(), InSquarePlacement.spread(), CeilingAwareHeightmapPlacement.simpleSurface(), BiomeFilter.biome())));

    // other

    public static final TagKey<Fluid> TAG_HOT_SPRING_WATER = FluidTags.create(new ResourceLocation(MODID, NAME));

    /** copy-pasted from vanilla filled bucket behaviours from {@link net.minecraft.core.dispenser.DispenseItemBehavior} */
    public static final DispenseItemBehavior dispenseHotSpringWaterBehaviour = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior defaultBehaviour = new DefaultDispenseItemBehavior();

        public ItemStack execute(BlockSource source, ItemStack stack)
        {
            DispensibleContainerItem dispensiblecontaineritem = (DispensibleContainerItem) stack.getItem();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            Level level = source.getLevel();
            if (dispensiblecontaineritem.emptyContents((Player) null, level, blockpos, (BlockHitResult) null))
            {
                dispensiblecontaineritem.checkExtraContent((Player) null, level, stack, blockpos);
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
        CONFIGURED_FEATURES.register(eventBus);
        PLACEMENT_MODIFIERS.register(eventBus);
        PLACED_FEATURES.register(eventBus);
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            HOT_SPRING_WATER_BUCKET.ifPresent(bucket -> DispenserBlock.registerBehavior(bucket, dispenseHotSpringWaterBehaviour));
        });
    }

    // other methods

    private static Supplier<SimpleParticleType> particleSimple(boolean alwaysShow)
    {
        return () -> new SimpleParticleType(alwaysShow);
    }

}