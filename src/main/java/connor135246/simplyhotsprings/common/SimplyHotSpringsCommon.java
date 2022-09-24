package connor135246.simplyhotsprings.common;

import static connor135246.simplyhotsprings.SimplyHotSprings.MODID;
import static net.minecraftforge.common.BiomeDictionary.Type.*;

import java.util.List;
import java.util.function.Supplier;

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
import net.minecraft.resources.ResourceKey;
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
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
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

    public static final RegistryObject<SimpleParticleType> HOT_SPRING_WATER_STEAM = PARTICLES.register(NAME + "_steam", particleSimple(true));
    public static final RegistryObject<SimpleParticleType> HOT_SPRING_WATER_STEAM_SMALL = PARTICLES.register(NAME + "_steam_small", particleSimple(true));
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

            if (ModList.get().isLoaded("wwoo_forge"))
            {
                // Overhauled Overworld, why don't you do this yourself???
                SimplyHotSprings.log.info("William Wythers' Overhauled Overworld is installed. Adding some Biome Dictionary Types to William Wythers' Overhauled Overworld biomes.");

                addTypes("wythers:ancient_copper_beech_forest", FOREST, HOT, DENSE, OVERWORLD);
                addTypes("wythers:ancient_emerald_beech_forest", FOREST, DENSE, OVERWORLD);
                addTypes("wythers:ancient_golden_beech_forest", FOREST, HOT, DENSE, OVERWORLD);
                addTypes("wythers:ancient_mossy_swamp", SWAMP, WET, OVERWORLD);
                addTypes("wythers:ancient_moss_forest", FOREST, DENSE, SPOOKY, OVERWORLD);
                addTypes("wythers:ancient_oak_swamp", SWAMP, DENSE, WET, SPOOKY, OVERWORLD);
                addTypes("wythers:ancient_taiga", FOREST, CONIFEROUS, OVERWORLD);
                addTypes("wythers:andesite_crags", HILLS, SLOPE, OVERWORLD);
                addTypes("wythers:aspen_crags", FOREST, HILLS, CONIFEROUS, SLOPE, OVERWORLD);
                addTypes("wythers:autumnal_birch_forest", FOREST, OVERWORLD);
                addTypes("wythers:autumnal_crags", HOT, SLOPE, OVERWORLD);
                addTypes("wythers:autumnal_flower_forest", FOREST, OVERWORLD);
                addTypes("wythers:autumnal_forest", FOREST, OVERWORLD);
                addTypes("wythers:autumnal_forest_edge", FOREST, SPARSE, OVERWORLD);
                addTypes("wythers:autumnal_plains", PLAINS, SPARSE, OVERWORLD);
                addTypes("wythers:autumnal_swamp", SWAMP, WET, OVERWORLD);
                addTypes("wythers:ayers_rock", MESA, HOT, DRY, OVERWORLD);
                addTypes("wythers:badlands_canyon", MESA, DRY, SLOPE, OVERWORLD);
                addTypes("wythers:badlands_desert", MESA, DRY, SANDY, OVERWORLD);
                addTypes("wythers:badlands_river", MESA, RIVER, OVERWORLD);
                addTypes("wythers:bamboo_jungle_canyon", JUNGLE, SLOPE, OVERWORLD);
                addTypes("wythers:bamboo_jungle_highlands", JUNGLE, SLOPE, OVERWORLD);
                addTypes("wythers:bamboo_jungle_swamp", JUNGLE, SWAMP, WET, OVERWORLD);
                addTypes("wythers:bamboo_swamp", JUNGLE, SWAMP, WET, OVERWORLD);
                addTypes("wythers:bayou", SWAMP, HOT, WET, OVERWORLD);
                addTypes("wythers:berry_bog", SWAMP, COLD, WET, OVERWORLD);
                addTypes("wythers:billabong", RIVER, HOT, OVERWORLD);
                addTypes("wythers:birch_swamp", SWAMP, WET, OVERWORLD);
                addTypes("wythers:birch_taiga", FOREST, CONIFEROUS, OVERWORLD);
                addTypes("wythers:black_beach", BEACH, OVERWORLD);
                addTypes("wythers:black_river", RIVER, OVERWORLD);
                addTypes("wythers:boreal_forest_red", FOREST, CONIFEROUS, OVERWORLD);
                addTypes("wythers:boreal_forest_yellow", FOREST, CONIFEROUS, OVERWORLD);
                addTypes("wythers:cactus_desert", HOT, DRY, SANDY, OVERWORLD);
                addTypes("wythers:calcite_caverns", UNDERGROUND, OVERWORLD);
                addTypes("wythers:calcite_coast", BEACH, OVERWORLD);
                addTypes("wythers:chaparral", SAVANNA, SPARSE, OVERWORLD);
                addTypes("wythers:coastal_mangroves", BEACH, OVERWORLD);
                addTypes("wythers:cold_island", PLAINS, COLD, SPARSE, CONIFEROUS, OVERWORLD);
                addTypes("wythers:cold_plains", PLAINS, COLD, SPARSE, OVERWORLD);
                addTypes("wythers:cold_stony_shore", BEACH, COLD, OVERWORLD);
                addTypes("wythers:cool_forest", FOREST, OVERWORLD);
                addTypes("wythers:cool_forest_edge", FOREST, SPARSE, OVERWORLD);
                addTypes("wythers:cool_plains", PLAINS, SPARSE, OVERWORLD);
                addTypes("wythers:cool_stony_canyons", SLOPE, OVERWORLD);
                addTypes("wythers:cool_stony_peaks", PEAK, OVERWORLD);
                addTypes("wythers:crimson_tundra", PLAINS, COLD, SPARSE, SNOWY, OVERWORLD);
                addTypes("wythers:danakil_desert", WATER, HOT, WET, WASTELAND, OVERWORLD);
                addTypes("wythers:deepslate_shore", BEACH, OVERWORLD);
                addTypes("wythers:deep_dark_forest", FOREST, DENSE, SPOOKY, OVERWORLD);
                addTypes("wythers:deep_desert", HOT, DRY, SANDY, OVERWORLD);
                addTypes("wythers:deep_desert_river", RIVER, HOT, SANDY, OVERWORLD);
                addTypes("wythers:deep_icy_ocean", OCEAN, COLD, SNOWY, OVERWORLD);
                addTypes("wythers:deep_snowy_taiga", FOREST, COLD, CONIFEROUS, SNOWY, OVERWORLD);
                addTypes("wythers:deep_underground", UNDERGROUND, OVERWORLD);
                addTypes("wythers:desert_beach", BEACH, HOT, DRY, OVERWORLD);
                addTypes("wythers:desert_island", HOT, DRY, SANDY, OVERWORLD);
                addTypes("wythers:desert_lakes", SWAMP, WATER, HOT, WET, SANDY, OVERWORLD);
                addTypes("wythers:desert_pinnacles", HOT, DRY, SANDY, OVERWORLD);
                addTypes("wythers:desert_river", RIVER, HOT, SANDY, LUSH, OVERWORLD);
                addTypes("wythers:dripleaf_swamp", SWAMP, HOT, WET, LUSH, OVERWORLD);
                addTypes("wythers:dry_savanna", SAVANNA, HOT, SPARSE, DRY, OVERWORLD);
                addTypes("wythers:dry_tropical_forest", FOREST, SAVANNA, HOT, DRY, OVERWORLD);
                addTypes("wythers:dry_tropical_grassland", PLAINS, SAVANNA, HOT, SPARSE, DRY, OVERWORLD);
                addTypes("wythers:eucalyptus_deanei_forest", FOREST, DENSE, OVERWORLD);
                addTypes("wythers:eucalyptus_jungle", JUNGLE, HOT, DENSE, WET, OVERWORLD);
                addTypes("wythers:eucalyptus_jungle_canyon", JUNGLE, HOT, DENSE, WET, SLOPE, OVERWORLD);
                addTypes("wythers:eucalyptus_salubris_woodland", SAVANNA, HOT, DRY, OVERWORLD);
                addTypes("wythers:eucalyptus_woodland", SAVANNA, HOT, DRY, OVERWORLD);
                addTypes("wythers:fen", SWAMP, COLD, WET, CONIFEROUS, OVERWORLD);
                addTypes("wythers:flooded_jungle", JUNGLE, SWAMP, DENSE, WET, OVERWORLD);
                addTypes("wythers:flooded_rainforest", JUNGLE, SWAMP, DENSE, WET, OVERWORLD);
                addTypes("wythers:flooded_savanna", SAVANNA, RIVER, SWAMP, HOT, WET, OVERWORLD);
                addTypes("wythers:flooded_temperate_rainforest", FOREST, SWAMP, DENSE, WET, CONIFEROUS, OVERWORLD);
                addTypes("wythers:flowering_pantanal", JUNGLE, SWAMP, HOT, WET, OVERWORLD);
                addTypes("wythers:forbidden_forest", FOREST, DENSE, SPOOKY, OVERWORLD);
                addTypes("wythers:forested_highlands", FOREST, CONIFEROUS, SLOPE, OVERWORLD);
                addTypes("wythers:forest_edge", FOREST, SPARSE, OVERWORLD);
                addTypes("wythers:frigid_island", PLAINS, COLD, WASTELAND, OVERWORLD);
                addTypes("wythers:frozen_island", COLD, SNOWY, OVERWORLD);
                addTypes("wythers:fungous_dripstone_caves", MUSHROOM, UNDERGROUND, OVERWORLD);
                addTypes("wythers:giant_sequoia_forest", FOREST, SAVANNA, HOT, OVERWORLD);
                addTypes("wythers:glacial_cliffs", COLD, SNOWY, OVERWORLD);
                addTypes("wythers:granite_canyon", HOT, SLOPE, OVERWORLD);
                addTypes("wythers:gravelly_beach", BEACH, OVERWORLD);
                addTypes("wythers:gravelly_river", RIVER, OVERWORLD);
                addTypes("wythers:guelta", HOT, DRY, SANDY, OVERWORLD);
                addTypes("wythers:harvest_fields", PLAINS, OVERWORLD);
                addTypes("wythers:highlands", COLD, SNOWY, SLOPE, OVERWORLD);
                addTypes("wythers:highland_plains", PLAINS, SLOPE, OVERWORLD);
                addTypes("wythers:highland_tropical_rainforest", JUNGLE, HOT, WET, SLOPE, OVERWORLD);
                addTypes("wythers:huangshan_highlands", SPARSE, SLOPE, OVERWORLD);
                addTypes("wythers:humid_tropical_grassland", JUNGLE, SAVANNA, HOT, SPARSE, WET, OVERWORLD);
                addTypes("wythers:ice_cap", COLD, SNOWY, OVERWORLD);
                addTypes("wythers:icy_crags", COLD, SNOWY, PEAK, OVERWORLD);
                addTypes("wythers:icy_ocean", OCEAN, COLD, SNOWY, OVERWORLD);
                addTypes("wythers:icy_river", RIVER, COLD, SNOWY, OVERWORLD);
                addTypes("wythers:icy_shore", BEACH, COLD, SNOWY, OVERWORLD);
                addTypes("wythers:icy_volcano", COLD, SNOWY, PEAK, OVERWORLD);
                addTypes("wythers:jacaranda_savanna", SAVANNA, HOT, OVERWORLD);
                addTypes("wythers:jade_highlands", FOREST, CONIFEROUS, SLOPE, OVERWORLD);
                addTypes("wythers:jungle_canyon", JUNGLE, HOT, WET, SLOPE, OVERWORLD);
                addTypes("wythers:jungle_island", JUNGLE, HOT, WET, OVERWORLD);
                addTypes("wythers:jungle_river", JUNGLE, RIVER, HOT, WET, OVERWORLD);
                addTypes("wythers:kwongan_heath", MESA, HOT, SPARSE, DRY, OVERWORLD);
                addTypes("wythers:lantern_river", RIVER, MAGICAL, OVERWORLD);
                addTypes("wythers:lapacho_plains", PLAINS, SPARSE, OVERWORLD);
                addTypes("wythers:larch_taiga", FOREST, CONIFEROUS, OVERWORLD);
                addTypes("wythers:lichenous_caves", UNDERGROUND, OVERWORLD);
                addTypes("wythers:lichenous_dripstone_caves", UNDERGROUND, OVERWORLD);
                addTypes("wythers:lush_dripstone_caves", WET, LUSH, UNDERGROUND, OVERWORLD);
                addTypes("wythers:lush_fungous_dripstone_caves", WET, LUSH, MUSHROOM, UNDERGROUND, OVERWORLD);
                addTypes("wythers:lush_shroom_caves", WET, LUSH, MUSHROOM, UNDERGROUND, OVERWORLD);
                addTypes("wythers:maple_mountains", FOREST, CONIFEROUS, SLOPE, OVERWORLD);
                addTypes("wythers:marsh", SWAMP, WET, OVERWORLD);
                addTypes("wythers:mediterranean_island", PLAINS, SPARSE, OVERWORLD);
                addTypes("wythers:mediterranean_island_thermal_springs", PLAINS, WATER, SPARSE, WET, OVERWORLD);
                addTypes("wythers:mossy_caves", UNDERGROUND, OVERWORLD);
                addTypes("wythers:mossy_dripstone_caves", UNDERGROUND, OVERWORLD);
                addTypes("wythers:mud_pools", SWAMP, WATER, HOT, WET, WASTELAND, OVERWORLD);
                addTypes("wythers:mushroom_caves", MUSHROOM, UNDERGROUND, OVERWORLD);
                addTypes("wythers:mushroom_island", JUNGLE, HOT, WET, MUSHROOM, OVERWORLD);
                addTypes("wythers:old_growth_taiga_crags", FOREST, CONIFEROUS, PEAK, OVERWORLD);
                addTypes("wythers:old_growth_taiga_swamp", FOREST, SWAMP, WET, CONIFEROUS, OVERWORLD);
                addTypes("wythers:outback", MESA, HOT, DRY, SANDY, OVERWORLD);
                addTypes("wythers:outback_desert", MESA, HOT, DRY, SANDY, OVERWORLD);
                addTypes("wythers:pantanal", JUNGLE, SWAMP, HOT, WET, OVERWORLD);
                addTypes("wythers:phantasmal_forest", FOREST, DENSE, MAGICAL, SPOOKY, OVERWORLD);
                addTypes("wythers:phantasmal_swamp", SWAMP, WET, MAGICAL, SPOOKY, OVERWORLD);
                addTypes("wythers:pine_barrens", FOREST, CONIFEROUS, OVERWORLD);
                addTypes("wythers:red_desert", MESA, HOT, DRY, SANDY, OVERWORLD);
                addTypes("wythers:red_rock_canyon", MESA, HOT, DRY, SLOPE, OVERWORLD);
                addTypes("wythers:sakura_forest", FOREST, OVERWORLD);
                addTypes("wythers:salt_lakes_pink", SWAMP, WATER, HOT, WET, WASTELAND, OVERWORLD);
                addTypes("wythers:salt_lakes_turquoise", SWAMP, WATER, HOT, WET, WASTELAND, OVERWORLD);
                addTypes("wythers:salt_lakes_white", SWAMP, WATER, HOT, WET, WASTELAND, OVERWORLD);
                addTypes("wythers:sandy_jungle", JUNGLE, HOT, SANDY, OVERWORLD);
                addTypes("wythers:sand_dunes", BEACH, OVERWORLD);
                addTypes("wythers:savanna_badlands", MESA, SAVANNA, HOT, SPARSE, DRY, OVERWORLD);
                addTypes("wythers:savanna_basaltic_incursions", SAVANNA, HOT, DRY, DEAD, WASTELAND, OVERWORLD);
                addTypes("wythers:savanna_river", SAVANNA, RIVER, HOT, OVERWORLD);
                addTypes("wythers:scrubland", SAVANNA, HOT, SPARSE, DRY, OVERWORLD);
                addTypes("wythers:scrub_forest", FOREST, SAVANNA, HOT, SPARSE, DRY, OVERWORLD);
                addTypes("wythers:snowy_bog", SWAMP, COLD, WET, SNOWY, OVERWORLD);
                addTypes("wythers:snowy_canyon", COLD, SNOWY, SLOPE, OVERWORLD);
                addTypes("wythers:snowy_fen", SWAMP, COLD, WET, SNOWY, OVERWORLD);
                addTypes("wythers:snowy_peaks", COLD, SNOWY, PEAK, OVERWORLD);
                addTypes("wythers:snowy_thermal_taiga", WATER, COLD, WET, CONIFEROUS, SNOWY, OVERWORLD);
                addTypes("wythers:snowy_tundra", COLD, SNOWY, WASTELAND, OVERWORLD);
                addTypes("wythers:sparse_bamboo_jungle", JUNGLE, HOT, SPARSE, WET, OVERWORLD);
                addTypes("wythers:sparse_eucalyptus_jungle", JUNGLE, HOT, SPARSE, WET, OVERWORLD);
                addTypes("wythers:sparse_eucalyptus_woodland", JUNGLE, HOT, SPARSE, DRY, OVERWORLD);
                addTypes("wythers:spring_flower_fields", PLAINS, OVERWORLD);
                addTypes("wythers:spring_flower_forest", FOREST, OVERWORLD);
                addTypes("wythers:stony_canyon", SLOPE, OVERWORLD);
                addTypes("wythers:subtropical_forest", FOREST, HOT, OVERWORLD);
                addTypes("wythers:subtropical_forest_edge", FOREST, HOT, SPARSE, OVERWORLD);
                addTypes("wythers:subtropical_grassland", PLAINS, HOT, SPARSE, OVERWORLD);
                addTypes("wythers:taiga_crags", FOREST, CONIFEROUS, PEAK, OVERWORLD);
                addTypes("wythers:tangled_forest", FOREST, SWAMP, HOT, WET, OVERWORLD);
                addTypes("wythers:temperate_island", PLAINS, SPARSE, OVERWORLD);
                addTypes("wythers:temperate_rainforest", FOREST, WET, CONIFEROUS, OVERWORLD);
                addTypes("wythers:temperate_rainforest_crags", FOREST, WET, CONIFEROUS, PEAK, OVERWORLD);
                addTypes("wythers:tepui", HILLS, WET, PLATEAU, WASTELAND, OVERWORLD);
                addTypes("wythers:thermal_taiga", FOREST, SWAMP, WATER, COLD, WET, CONIFEROUS, OVERWORLD);
                addTypes("wythers:thermal_taiga_crags", FOREST, WATER, COLD, WET, CONIFEROUS, PEAK, OVERWORLD);
                addTypes("wythers:tibesti_mountains", HOT, WASTELAND, PEAK, OVERWORLD);
                addTypes("wythers:tropical_beach", BEACH, HOT, OVERWORLD);
                addTypes("wythers:tropical_forest", FOREST, SAVANNA, HOT, OVERWORLD);
                addTypes("wythers:tropical_forest_canyon", FOREST, HOT, SLOPE, OVERWORLD);
                addTypes("wythers:tropical_forest_river", FOREST, RIVER, HOT, OVERWORLD);
                addTypes("wythers:tropical_grassland", SAVANNA, PLAINS, HOT, SPARSE, OVERWORLD);
                addTypes("wythers:tropical_island", PLAINS, HOT, SPARSE, OVERWORLD);
                addTypes("wythers:tropical_rainforest", JUNGLE, HOT, DENSE, WET, OVERWORLD);
                addTypes("wythers:tropical_volcano", HOT, DRY, WASTELAND, PEAK, OVERWORLD);
                addTypes("wythers:tsingy_forest", FOREST, HILLS, SAVANNA, HOT, OVERWORLD);
                addTypes("wythers:tundra", PLAINS, COLD, SPARSE, OVERWORLD);
                addTypes("wythers:underground", UNDERGROUND, OVERWORLD);
                addTypes("wythers:volcanic_chamber", HOT, DRY, UNDERGROUND, OVERWORLD);
                addTypes("wythers:volcanic_crater", HOT, DRY, WASTELAND, OVERWORLD);
                addTypes("wythers:volcano", HOT, DRY, WASTELAND, PEAK, OVERWORLD);
                addTypes("wythers:warm_birch_forest", FOREST, HOT, OVERWORLD);
                addTypes("wythers:warm_stony_shore", BEACH, HOT, OVERWORLD);
                addTypes("wythers:waterlily_swamp", SWAMP, WET, OVERWORLD);
                addTypes("wythers:windswept_jungle", JUNGLE, HILLS, HOT, DENSE, WET, OVERWORLD);
                addTypes("wythers:wistman_woods", FOREST, HILLS, DENSE, OVERWORLD);
                addTypes("wythers:wooded_desert", HOT, SPARSE, DRY, SANDY, OVERWORLD);
                addTypes("wythers:wooded_savanna", SAVANNA, HOT, DRY, OVERWORLD);
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
