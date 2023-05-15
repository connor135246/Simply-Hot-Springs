package connor135246.simplyhotsprings.common.world.gen;

import java.util.HashSet;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import connor135246.simplyhotsprings.SimplyHotSprings;
import connor135246.simplyhotsprings.util.GenerationReason;
import connor135246.simplyhotsprings.util.SimplyHotSpringsConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

//TODO forge has its own custom HolderSet things like forge:not
/**
 * To add hot springs to biomes, put a .json in a datapack under data/[namespace]/forge/biome_modifier with:
 * 
 * <pre>
 * {
 *   "type": "simplyhotsprings:add_hot_springs",
 *   "hot_springs": "your_namespace:your_feature", // accepts a placed feature id, [list of placed feature ids], or #namespace:feature_tag
 *   "biomes": "#your_namespace:your_biome_tag", // accepts a biome id, [list of biome ids], or #namespace:biome_tag.
 *   "is_blacklist": false, // accepts a boolean. if true, biomes will be used as a blacklist instead.
 *   "step": "lakes" // optional. accepts a Decoration enum name. if not included, defaults to "lakes"
 * }
 * </pre>
 * 
 * Also, it allows you to test where the features will generate using '/simplyhotsprings locationinfo'.
 */
public record AddHotSpringsBiomeModifier(HolderSet<PlacedFeature> hotSprings, HolderSet<Biome> biomes, boolean isBlacklist, GenerationStep.Decoration step) implements BiomeModifier
{

    public static final Codec<AddHotSpringsBiomeModifier> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            PlacedFeature.LIST_CODEC.fieldOf("hot_springs").forGetter(AddHotSpringsBiomeModifier::hotSprings),
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddHotSpringsBiomeModifier::biomes),
            Codec.BOOL.fieldOf("is_blacklist").forGetter(AddHotSpringsBiomeModifier::isBlacklist),
            GenerationStep.Decoration.CODEC.optionalFieldOf("step", GenerationStep.Decoration.LAKES).forGetter(AddHotSpringsBiomeModifier::step))
            .apply(builder, AddHotSpringsBiomeModifier::new));

    public static final ResourceLocation HOT_SPRINGS_DEFAULT = new ResourceLocation(SimplyHotSprings.MODID, "hot_springs_default");
    public static final ResourceLocation HOT_SPRINGS_BIG = new ResourceLocation(SimplyHotSprings.MODID, "hot_springs_big");
    public static final ResourceLocation HOT_SPRINGS_WELLSPRINGS = new ResourceLocation(SimplyHotSprings.MODID, "hot_springs_wellsprings");

    public static final Set<AddHotSpringsBiomeModifier> warned = new HashSet<AddHotSpringsBiomeModifier>();

    @Override
    public void modify(Holder<Biome> biome, Phase phase, Builder builder)
    {
        if (phase == Phase.ADD)
        {
            final ResourceLocation id;
            try
            {
                id = ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(ForgeRegistries.Keys.BIOME_MODIFIERS).getKey(this);
                if (id == null)
                    throw new IllegalStateException("Null biome modifier id!");
            }
            catch (IllegalStateException excep)
            {
                if (!warned.contains(this))
                {
                    SimplyHotSprings.log.error("An error occurred when preparing a Hot Springs biome modifier with hot_springs: " + hotSprings
                            + ", biome_list: " + biomes + ", is_blacklist: " + isBlacklist + " and step: " + step, excep);
                    warned.add(this);
                }
                return;
            }

            if (hotSprings.size() < 1)
            {
                if (!warned.contains(this))
                {
                    SimplyHotSprings.log.warn("Ignoring Hot Springs biome modifier \"" + id + "\" with empty feature list!");
                    warned.add(this);
                }
                return;
            }

            final GenerationReason reason;
            if ((!SimplyHotSpringsConfig.SERVER.worldGenDefault.get() && HOT_SPRINGS_DEFAULT.equals(id))
                    || (!SimplyHotSpringsConfig.SERVER.worldGenBig.get() && HOT_SPRINGS_BIG.equals(id))
                    || (!SimplyHotSpringsConfig.SERVER.worldGenWellsprings.get() && HOT_SPRINGS_WELLSPRINGS.equals(id)))
            {
                reason = GenerationReason.NO_WORLD_GEN_CONFIG;
                return; // no reason to even put it in the table if it's always false
            }
            else
                reason = getGenerationReason(biome);

            SimplyHotSpringsConfig.biomeModifierReasons.put(biome, id, reason);

            if (reason.allowsGeneration())
            {
                BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
                hotSprings.forEach(holder -> generationSettings.addFeature(step, holder));
            }
        }
    }

    public GenerationReason getGenerationReason(Holder<Biome> biome)
    {
        if (biomes.contains(biome))
            return isBlacklist ? GenerationReason.BLACKLISTED : GenerationReason.WHITELISTED;
        else
            return isBlacklist ? GenerationReason.NOT_BLACKLISTED : GenerationReason.NOT_WHITELISTED;
    }

    @Override
    public Codec<? extends BiomeModifier> codec()
    {
        return CODEC;
    }

}
