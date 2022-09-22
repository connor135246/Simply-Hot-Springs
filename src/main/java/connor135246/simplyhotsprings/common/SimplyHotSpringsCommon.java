package connor135246.simplyhotsprings.common;

import static connor135246.simplyhotsprings.SimplyHotSprings.MODID;
import static net.minecraftforge.common.BiomeDictionary.Type.*;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.common.blocks.HotSpringWaterBlock;
import connor135246.simplyhotsprings.common.fluids.HotSpringWaterFluid;
import connor135246.simplyhotsprings.common.world.gen.feature.HotSpringsFeature;
import connor135246.simplyhotsprings.common.world.gen.placement.CeilingAwareHeightmapPlacement;
import connor135246.simplyhotsprings.common.world.gen.placement.ConfigChanceFilter;
import connor135246.simplyhotsprings.util.BiomeTypeArgument;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
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
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.Tags.IOptionalNamedTag;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
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

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);

    // registry objects

    public static final RegistryObject<LiquidBlock> HOT_SPRING_WATER_BLOCK = BLOCKS.register(NAME, HotSpringWaterBlock::new);

    public static final RegistryObject<FlowingFluid> HOT_SPRING_WATER = FLUIDS.register(NAME, HotSpringWaterFluid.Source::new);
    public static final RegistryObject<FlowingFluid> FLOWING_HOT_SPRING_WATER = FLUIDS.register("flowing_" + NAME, HotSpringWaterFluid.Flowing::new);

    public static final RegistryObject<Item> HOT_SPRING_WATER_BUCKET = ITEMS.register(NAME + "_bucket",
            () -> new BucketItem(HOT_SPRING_WATER, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET).tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<SimpleParticleType> HOT_SPRING_WATER_STEAM = PARTICLES.register(NAME + "_steam", particleSimple(true));
    public static final RegistryObject<SimpleParticleType> HOT_SPRING_WATER_STEAM_SMALL = PARTICLES.register(NAME + "_steam_small", particleSimple(true));
    public static final RegistryObject<SimpleParticleType> DRIPPING_HOT_SPRING_WATER = PARTICLES.register("dripping_" + NAME, particleSimple(false));
    public static final RegistryObject<SimpleParticleType> FALLING_HOT_SPRING_WATER = PARTICLES.register("falling_" + NAME, particleSimple(false));
    public static final RegistryObject<SimpleParticleType> SPLASHING_HOT_SPRING_WATER = PARTICLES.register("splashing_" + NAME, particleSimple(false));
    public static final RegistryObject<SimpleParticleType> HOT_SPRING_WATER_UNDERWATER = PARTICLES.register(NAME + "_underwater", particleSimple(false));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> HOT_SPRINGS_FEATURE = FEATURES.register("hot_springs",
            () -> new HotSpringsFeature(NoneFeatureConfiguration.CODEC));

    // other

    public static @Nullable ConfiguredFeature<?, ?> CONFIGURED_HOT_SPRINGS_FEATURE = null;
    public static @Nullable PlacementModifierType<?> CONFIG_CHANCE_FILTER = null;
    public static @Nullable PlacementModifierType<?> CEILING_AWARE_HEIGHTMAP_PLACEMENT = null;
    public static @Nullable PlacedFeature PLACED_HOT_SPRINGS_FEATURE = null;

    public static final IOptionalNamedTag<Fluid> TAG_HOT_SPRING_WATER = FluidTags.createOptional(new ResourceLocation(MODID, NAME));

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
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            CONFIG_CHANCE_FILTER = Registry.register(Registry.PLACEMENT_MODIFIERS, new ResourceLocation(MODID, "config_chance"), ConfigChanceFilter.TYPE);
            CEILING_AWARE_HEIGHTMAP_PLACEMENT = Registry.register(Registry.PLACEMENT_MODIFIERS, new ResourceLocation(MODID, "ceiling_aware_heightmap"), CeilingAwareHeightmapPlacement.TYPE);

            if (HOT_SPRINGS_FEATURE.isPresent())
            {
                CONFIGURED_HOT_SPRINGS_FEATURE = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(MODID, "hot_springs"),
                        HOT_SPRINGS_FEATURE.get().configured(NoneFeatureConfiguration.INSTANCE));

                PLACED_HOT_SPRINGS_FEATURE = Registry.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation(MODID, "hot_springs_default"),
                        CONFIGURED_HOT_SPRINGS_FEATURE.placed(ConfigChanceFilter.configChance(), InSquarePlacement.spread(),
                                CeilingAwareHeightmapPlacement.simpleSurface(), BiomeFilter.biome()));
            }

            HOT_SPRING_WATER_BUCKET.ifPresent(bucket -> DispenserBlock.registerBehavior(bucket, dispenseHotSpringWaterBehaviour));

            ArgumentTypes.register("simplyhotsprings.biome_type", BiomeTypeArgument.class, new EmptyArgumentSerializer<>(BiomeTypeArgument::biomeTypeArgument));

            if (ModList.get().isLoaded("terralith"))
            {
                // Terralith, why don't you do this yourself???
                SimplyHotSprings.log.info("Terralith is installed. Adding some Biome Dictionary Types to Terralith biomes.");

                addTypes("terralith:cave/andesite_caves", UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/crystal_caves", MAGICAL, UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/deep_caves", UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/desert_caves", SANDY, UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/diorite_caves", UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/frostfire_caves", MAGICAL, SPOOKY, UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/fungal_caves", MUSHROOM, UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/granite_caves", UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/ice_caves", COLD, UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/infested_caves", SPOOKY, UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/mantle_caves", HOT, UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/thermal_caves", HOT, WET, UNDERGROUND, OVERWORLD);
                addTypes("terralith:cave/tuff_caves", UNDERGROUND, OVERWORLD);
                addTypes("terralith:alpha_islands_winter", FOREST, COLD, SNOWY, RARE, OVERWORLD);
                addTypes("terralith:alpha_islands", FOREST, RARE, OVERWORLD);
                addTypes("terralith:alpine_grove", FOREST, COLD, CONIFEROUS, SNOWY, SLOPE, OVERWORLD);
                addTypes("terralith:alpine_highlands", PLAINS, CONIFEROUS, PLATEAU, OVERWORLD);
                addTypes("terralith:amethyst_canyon", JUNGLE, HOT, DENSE, WET, MAGICAL, RARE, OVERWORLD);
                addTypes("terralith:amethyst_rainforest", JUNGLE, HOT, DENSE, WET, MAGICAL, OVERWORLD);
                addTypes("terralith:ancient_sands", HOT, DRY, SANDY, RARE, OVERWORLD);
                addTypes("terralith:arid_highlands", SAVANNA, PLAINS, HOT, SPARSE, DRY, PLATEAU, OVERWORLD);
                addTypes("terralith:ashen_savanna", SAVANNA, PLAINS, HOT, SPARSE, DRY, DEAD, RARE, OVERWORLD);
                addTypes("terralith:basalt_cliffs", BEACH, RARE, OVERWORLD);
                addTypes("terralith:birch_taiga", FOREST, COLD, CONIFEROUS, OVERWORLD);
                addTypes("terralith:blooming_plateau", PLAINS, PLATEAU, SLOPE, OVERWORLD);
                addTypes("terralith:blooming_valley", FOREST, LUSH, OVERWORLD);
                addTypes("terralith:brushland", SAVANNA, PLAINS, HOT, DRY, OVERWORLD);
                addTypes("terralith:bryce_canyon", MESA, HOT, DRY, CONIFEROUS, SANDY, RARE, OVERWORLD);
                addTypes("terralith:caldera", RARE, SLOPE, OVERWORLD);
                addTypes("terralith:cloud_forest", FOREST, HILLS, COLD, CONIFEROUS, RARE, OVERWORLD);
                addTypes("terralith:cold_shrubland", PLAINS, COLD, CONIFEROUS, OVERWORLD);
                addTypes("terralith:desert_canyon", HOT, DRY, SANDY, MODIFIED, SLOPE, OVERWORLD);
                addTypes("terralith:desert_oasis", HOT, SANDY, LUSH, OVERWORLD);
                addTypes("terralith:desert_spires", HOT, DRY, SANDY, RARE, PEAK, OVERWORLD);
                addTypes("terralith:emerald_peaks", COLD, SNOWY, RARE, PEAK, OVERWORLD);
                addTypes("terralith:forested_highlands", FOREST, CONIFEROUS, PLATEAU, OVERWORLD);
                addTypes("terralith:fractured_savanna", SAVANNA, HILLS, HOT, SPARSE, DRY, RARE, OVERWORLD);
                addTypes("terralith:frozen_cliffs", BEACH, COLD, SNOWY, RARE, OVERWORLD);
                addTypes("terralith:glacial_chasm", COLD, SNOWY, RARE, PEAK, OVERWORLD);
                addTypes("terralith:granite_cliffs", BEACH, RARE, OVERWORLD);
                addTypes("terralith:gravel_beach", BEACH, OVERWORLD);
                addTypes("terralith:gravel_desert", PLAINS, COLD, SNOWY, WASTELAND, OVERWORLD);
                addTypes("terralith:haze_mountain", FOREST, HILLS, OVERWORLD);
                addTypes("terralith:highlands", PLAINS, SPARSE, PLATEAU, OVERWORLD);
                addTypes("terralith:hot_shrubland", PLAINS, HOT, OVERWORLD);
                addTypes("terralith:ice_marsh", SWAMP, COLD, WET, SNOWY, OVERWORLD);
                addTypes("terralith:jungle_mountains", JUNGLE, HILLS, HOT, WET, RARE, OVERWORLD);
                addTypes("terralith:lavender_forest", FOREST, RARE, OVERWORLD);
                addTypes("terralith:lavender_valley", FOREST, PLATEAU, OVERWORLD);
                addTypes("terralith:lush_valley", FOREST, CONIFEROUS, LUSH, RARE, PLATEAU, OVERWORLD);
                addTypes("terralith:mirage_isles", FOREST, MUSHROOM, MAGICAL, RARE, OVERWORLD);
                addTypes("terralith:moonlight_grove", FOREST, MAGICAL, RARE, OVERWORLD);
                addTypes("terralith:moonlight_valley", FOREST, MAGICAL, PLATEAU, OVERWORLD);
                addTypes("terralith:mountain_steppe", PLAINS, PLATEAU, OVERWORLD);
                addTypes("terralith:orchid_swamp", SWAMP, WET, LUSH, OVERWORLD);
                addTypes("terralith:painted_mountains", MESA, HOT, RARE, PEAK, OVERWORLD);
                addTypes("terralith:red_oasis", HOT, SANDY, LUSH, RARE, OVERWORLD);
                addTypes("terralith:rocky_jungle", JUNGLE, HOT, WET, OVERWORLD);
                addTypes("terralith:rocky_mountains", DRY, PEAK, OVERWORLD);
                addTypes("terralith:rocky_shrubland", PLAINS, COLD, CONIFEROUS, RARE, OVERWORLD);
                addTypes("terralith:sakura_grove", FOREST, OVERWORLD);
                addTypes("terralith:sakura_valley", FOREST, RARE, PLATEAU, OVERWORLD);
                addTypes("terralith:sandstone_valley", HOT, SANDY, LUSH, RARE, OVERWORLD);
                addTypes("terralith:savanna_badlands", SAVANNA, PLAINS, MESA, HOT, SPARSE, DRY, OVERWORLD);
                addTypes("terralith:savanna_slopes", SAVANNA, HOT, SPARSE, DRY, RARE, SLOPE, OVERWORLD);
                addTypes("terralith:scarlet_mountains", COLD, SNOWY, RARE, PEAK, OVERWORLD);
                addTypes("terralith:shield_clearing", PLAINS, SPARSE, CONIFEROUS, RARE, OVERWORLD);
                addTypes("terralith:shield", FOREST, CONIFEROUS, OVERWORLD);
                addTypes("terralith:shrubland", SAVANNA, PLAINS, HOT, DRY, OVERWORLD);
                addTypes("terralith:siberian_grove", FOREST, COLD, CONIFEROUS, SNOWY, RARE, SLOPE, OVERWORLD);
                addTypes("terralith:siberian_taiga", FOREST, WET, CONIFEROUS, OVERWORLD);
                addTypes("terralith:skylands_autumn", FOREST, MAGICAL, OVERWORLD);
                addTypes("terralith:skylands_spring", FOREST, MAGICAL, OVERWORLD);
                addTypes("terralith:skylands_summer", JUNGLE, HOT, LUSH, MAGICAL, RARE, OVERWORLD);
                addTypes("terralith:skylands_winter", FOREST, COLD, CONIFEROUS, SNOWY, MAGICAL, RARE, OVERWORLD);
                addTypes("terralith:skylands", FOREST, MAGICAL, OVERWORLD);
                addTypes("terralith:snowy_badlands", MESA, COLD, DRY, SNOWY, RARE, OVERWORLD);
                addTypes("terralith:snowy_maple_forest", FOREST, COLD, CONIFEROUS, SNOWY, RARE, SLOPE, OVERWORLD);
                addTypes("terralith:snowy_shield", FOREST, COLD, CONIFEROUS, SNOWY, RARE, OVERWORLD);
                addTypes("terralith:steppe", PLAINS, PLATEAU, OVERWORLD);
                addTypes("terralith:stony_spires", RARE, PEAK, OVERWORLD);
                addTypes("terralith:temperate_highlands", FOREST, PLATEAU, OVERWORLD);
                addTypes("terralith:tropical_jungle", JUNGLE, HOT, DENSE, WET, OVERWORLD);
                addTypes("terralith:valley_clearing", PLAINS, SPARSE, OVERWORLD);
                addTypes("terralith:volcanic_crater", HOT, RARE, SLOPE, OVERWORLD);
                addTypes("terralith:volcanic_peaks", HOT, RARE, PEAK, OVERWORLD);
                addTypes("terralith:warm_river", RIVER, HOT, OVERWORLD);
                addTypes("terralith:warped_mesa", MESA, HOT, DRY, MAGICAL, RARE, OVERWORLD);
                addTypes("terralith:white_cliffs", BEACH, LUSH, RARE, OVERWORLD);
                addTypes("terralith:white_mesa", MESA, HOT, DRY, RARE, OVERWORLD);
                addTypes("terralith:windswept_spires", HILLS, COLD, RARE, OVERWORLD);
                addTypes("terralith:wintry_forest", FOREST, COLD, CONIFEROUS, SNOWY, OVERWORLD);
                addTypes("terralith:wintry_lowlands", FOREST, COLD, CONIFEROUS, SNOWY, MODIFIED, OVERWORLD);
                addTypes("terralith:yellowstone", FOREST, COLD, CONIFEROUS, OVERWORLD);
                addTypes("terralith:yosemite_cliffs", FOREST, SPARSE, CONIFEROUS, RARE, SLOPE, OVERWORLD);
                addTypes("terralith:yosemite_lowlands", FOREST, CONIFEROUS, RARE, PLATEAU, OVERWORLD);
            }
        });
    }

    // other methods

    private static Supplier<SimpleParticleType> particleSimple(boolean alwaysShow)
    {
        return () -> new SimpleParticleType(alwaysShow);
    }

    private static void addTypes(String id, BiomeDictionary.Type... types)
    {
        BiomeDictionary.addTypes(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(id)), types);
    }

}
