package connor135246.simplyhotsprings.common;

import static connor135246.simplyhotsprings.SimplyHotSprings.MODID;

import java.util.function.Supplier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.Codec;

import connor135246.simplyhotsprings.common.blocks.HotSpringWaterBlock;
import connor135246.simplyhotsprings.common.fluids.HotSpringWaterFluid;
import connor135246.simplyhotsprings.common.fluids.HotSpringWaterFluidType;
import connor135246.simplyhotsprings.common.world.gen.AddHotSpringsBiomeModifier;
import connor135246.simplyhotsprings.common.world.gen.feature.HotSpringsConfiguration;
import connor135246.simplyhotsprings.common.world.gen.feature.HotSpringsFeature;
import connor135246.simplyhotsprings.common.world.gen.placement.CeilingAwareHeightmapPlacement;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import connor135246.simplyhotsprings.util.TagKeyArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidInteractionRegistry.InteractionInformation;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class SimplyHotSpringsCommon
{

    public static final String NAME = "hot_spring_water";

    // deferred registers

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registry.BLOCK_REGISTRY, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registry.ITEM_REGISTRY, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registry.FLUID_REGISTRY, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registry.PARTICLE_TYPE_REGISTRY, MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registry.FEATURE_REGISTRY, MODID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registry.PLACEMENT_MODIFIER_REGISTRY, MODID);
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, MODID);

    // registry objects

    public static final RegistryObject<FluidType> HOT_SPRING_WATER_FLUID_TYPE = FLUID_TYPES.register(NAME, HotSpringWaterFluidType::new);

    public static final RegistryObject<FlowingFluid> HOT_SPRING_WATER = FLUIDS.register(NAME, HotSpringWaterFluid.Source::new);
    public static final RegistryObject<FlowingFluid> FLOWING_HOT_SPRING_WATER = FLUIDS.register("flowing_" + NAME, HotSpringWaterFluid.Flowing::new);

    public static final RegistryObject<LiquidBlock> HOT_SPRING_WATER_BLOCK = BLOCKS.register(NAME, HotSpringWaterBlock::new);

    public static final RegistryObject<Item> HOT_SPRING_WATER_BUCKET = ITEMS.register(NAME + "_bucket",
            () -> new BucketItem(HOT_SPRING_WATER, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET).tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<SimpleParticleType> HOT_SPRING_WATER_STEAM = PARTICLES.register(NAME + "_steam", particleSimple(true));
    public static final RegistryObject<SimpleParticleType> HOT_SPRING_WATER_STEAM_SMALL = PARTICLES.register(NAME + "_steam_small", particleSimple(true));
    public static final RegistryObject<SimpleParticleType> DRIPPING_HOT_SPRING_WATER = PARTICLES.register("dripping_" + NAME, particleSimple(false));
    public static final RegistryObject<SimpleParticleType> FALLING_HOT_SPRING_WATER = PARTICLES.register("falling_" + NAME, particleSimple(false));
    public static final RegistryObject<SimpleParticleType> SPLASHING_HOT_SPRING_WATER = PARTICLES.register("splashing_" + NAME, particleSimple(false));
    public static final RegistryObject<SimpleParticleType> HOT_SPRING_WATER_UNDERWATER = PARTICLES.register(NAME + "_underwater", particleSimple(false));

    public static final RegistryObject<Feature<HotSpringsConfiguration>> HOT_SPRINGS_FEATURE = FEATURES.register("hot_springs",
            () -> new HotSpringsFeature(HotSpringsConfiguration.CODEC));
    public static final RegistryObject<PlacementModifierType<?>> CEILING_AWARE_HEIGHTMAP_PLACEMENT = PLACEMENT_MODIFIERS.register("ceiling_aware_heightmap",
            () -> CeilingAwareHeightmapPlacement.TYPE);

    public static final RegistryObject<Codec<AddHotSpringsBiomeModifier>> ADD_HOT_SPRINGS = BIOME_MODIFIER_SERIALIZERS.register("add_hot_springs",
            () -> AddHotSpringsBiomeModifier.CODEC);

    public static final RegistryObject<TagKeyArgument.Info<Object>> TAG_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("tag",
            () -> ArgumentTypeInfos.registerByClass(fixClassType(TagKeyArgument.class), new TagKeyArgument.Info<Object>()));

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
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        PARTICLES.register(eventBus);
        FEATURES.register(eventBus);
        PLACEMENT_MODIFIERS.register(eventBus);
        BIOME_MODIFIER_SERIALIZERS.register(eventBus);
        COMMAND_ARGUMENT_TYPES.register(eventBus);

        eventBus.register(SimplyHotSpringsConfig.class);
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            HOT_SPRING_WATER_BUCKET.ifPresent(bucket -> DispenserBlock.registerBehavior(bucket, dispenseHotSpringWaterBehaviour));

            HOT_SPRING_WATER_FLUID_TYPE.ifPresent(type -> FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new InteractionInformation(type,
                    fluidState -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState())));
        });
    }

    // other methods

    private static Supplier<SimpleParticleType> particleSimple(boolean alwaysShow)
    {
        return () -> new SimpleParticleType(alwaysShow);
    }

    @SuppressWarnings("unchecked")
    private static <T extends ArgumentType<?>> Class<T> fixClassType(Class<? super T> clazz)
    {
        return (Class<T>) clazz;
    }

}
