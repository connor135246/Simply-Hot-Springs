package connor135246.simplyhotsprings.common.world.gen.feature;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * To create the random shapes of lakes, minecraft creates a bunch of overlapping spheres and excavates them. <br>
 * Then, any blocks excavated below a certain height from the bottom of the bounding box are filled with the liquid. <br>
 * The spheres aren't perfect spheres and are squashed horizontally and/or vertically - so they're technically "spheroids". <br>
 * The position passed to the hot springs feature by its placement will be horizontally in the north-west corner and vertically in the middle of the bounding box.
 */
public record HotSpringsConfiguration(
        /**
         * The fluid that will fill the hot spring.
         */
        BlockStateProvider fluid,
        /**
         * The horizontal size of the hot spring feature bounding box. <br>
         * Important: if you set this value to be greater than 16, your placed feature should not offset its x/z coordinates (for example, using the "minecraft:in_square" placement). <br>
         * This is because features are only supposed to modify blocks in a 3x3 chunk area.
         */
        int featureSizeXZ,
        /**
         * The vertical size of the hot spring feature bounding box.
         */
        int featureSizeY,
        /**
         * The height of the fluid, from the bottom of the bounding box. <br>
         * If it's zero, there won't be a fluid. <br>
         * If it's negative, the fluid will be generated from the top and count downward instead. This is generally only appropriate if the fluid is a gas that flows up instead of down.
         */
        int fluidLevel,
        /**
         * Ensures there is a solid base for the fluid below this height. <br>
         * If there would be holes that would spill the fluid out, the hot spring won't generate. Usually, this should be 1 above the {@link #fluidLevel}. <br>
         * If it's zero, there won't be any checks for leaks. <br>
         * If it's negative, the check will be from the top and count downward instead. This is generally only appropriate if the fluid is a gas that flows up instead of down.
         */
        int solidCheckBelowLevel,
        /**
         * The range of spheres that will be used to make a lake.
         */
        IntProvider spheres,
        /**
         * The range of diameters a sphere can have in the horizontal directions.
         */
        FloatProvider sphereDiameterXZ,
        /**
         * The range of diameters a sphere can have in the vertical direction.
         */
        FloatProvider sphereDiameterY,
        /**
         * Sphere padding on the sides. Reduces randomness by keeping the center of the sphere closer to the center of the bounding box. <br>
         * You probably don't want this value to be greater than: <code>{@link #featureSizeXZ} - maximum {@link #sphereDiameterXZ} / 2 + 1</code> <br>
         * Otherwise, spheres may be cut off on the sides of the bounding box.
         */
        float spherePaddingXZ,
        /**
         * Sphere padding on the top and bottom. Reduces randomness by keeping the center of the sphere closer to the center of the bounding box. <br>
         * You probably don't want this value to be greater than: <code>{@link #featureSizeY} -  maximum {@link #sphereDiameterY} / 2 + 1</code> <br>
         * Otherwise, spheres may be cut off on the sides of the bounding box.
         */
        float spherePaddingY,
        /**
         * Optional. If omitted, defaults to 0. <br>
         * If non-zero, the amount to change the horizontal sphere diameter when it's below {@link #sphereDiameterXZExtraBelowLevel}. May be positive or negative.
         */
        FloatProvider sphereDiameterXZExtra,
        /**
         * Optional. If omitted, defaults to 0. <br>
         * If non-zero, spheres with centers below this level will have their sphere diameter changed by {@link #sphereDiameterXZExtra}. <br>
         * If it's negative, the check will be from the top and count downward instead.
         */
        IntProvider sphereDiameterXZExtraBelowLevel,
        /**
         * Optional. If present, this is the block that will surround the hot spring. <br>
         * Works like the stone that surrounds vanilla lava lakes - it always replaces blocks touching the fluid, and has a 50% chance of replacing blocks not touching the fluid. <br>
         * Note that it's slightly different from lava pool stone in that the block state is sampled each time it wants to be placed, instead of just once at the start.
         */
        Optional<BlockStateProvider> barrier,
        /**
         * Optional. If present, these are additional placed features that will be placed after the hot spring. <br>
         * The position passed to the additional placed features is at the bottom-north-west corner of the hot spring. <br>
         * Important: the "minecraft:biome" placement cannot be used in placed features that are referenced from other configured features!
         */
        Optional<HolderSet<PlacedFeature>> additionalPlacedFeatures) implements FeatureConfiguration
{

    public static final int MAX_SIZE_XZ = 31, MAX_SIZE_Y = 63;

    public static final Codec<HotSpringsConfiguration> CODEC = RecordCodecBuilder.create(config -> config.group(
            BlockStateProvider.CODEC.fieldOf("fluid").forGetter(HotSpringsConfiguration::fluid),
            Codec.intRange(3, MAX_SIZE_XZ).fieldOf("feature_size_xz").forGetter(HotSpringsConfiguration::featureSizeXZ),
            Codec.intRange(3, MAX_SIZE_Y).fieldOf("feature_size_y").forGetter(HotSpringsConfiguration::featureSizeY),
            Codec.intRange(-MAX_SIZE_Y, MAX_SIZE_Y).fieldOf("fluid_level").forGetter(HotSpringsConfiguration::fluidLevel),
            Codec.intRange(-(MAX_SIZE_Y + 1), MAX_SIZE_Y + 1).fieldOf("solid_check_below_level").forGetter(HotSpringsConfiguration::solidCheckBelowLevel),
            IntProvider.codec(0, 100).fieldOf("spheres").forGetter(HotSpringsConfiguration::spheres),
            FloatProvider.codec(0.0F, MAX_SIZE_XZ).fieldOf("sphere_diameter_xz").forGetter(HotSpringsConfiguration::sphereDiameterXZ),
            FloatProvider.codec(0.0F, MAX_SIZE_Y).fieldOf("sphere_diameter_y").forGetter(HotSpringsConfiguration::sphereDiameterY),
            Codec.floatRange(0.0F, MAX_SIZE_XZ / 2 + 1).fieldOf("sphere_padding_xz").forGetter(HotSpringsConfiguration::spherePaddingXZ),
            Codec.floatRange(0.0F, MAX_SIZE_Y / 2 + 1).fieldOf("sphere_padding_y").forGetter(HotSpringsConfiguration::spherePaddingY),
            FloatProvider.codec(-MAX_SIZE_XZ, MAX_SIZE_XZ).fieldOf("sphere_diameter_xz_extra").orElse(ConstantFloat.of(0.0F)).forGetter(HotSpringsConfiguration::sphereDiameterXZExtra),
            IntProvider.codec(-(MAX_SIZE_Y + 1), MAX_SIZE_Y + 1).fieldOf("sphere_diameter_xz_extra_below_level").orElse(ConstantInt.of(0)).forGetter(HotSpringsConfiguration::sphereDiameterXZExtraBelowLevel),
            BlockStateProvider.CODEC.optionalFieldOf("barrier").forGetter(HotSpringsConfiguration::barrier),
            PlacedFeature.LIST_CODEC.optionalFieldOf("additional_placed_features").forGetter(HotSpringsConfiguration::additionalPlacedFeatures))
            .apply(config, HotSpringsConfiguration::new));

}
