package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.features.IcicleClusterFeature;
import com.ordana.immersive_weathering.features.IcicleClusterFeatureConfig;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.function.Supplier;

public class ModFeatures {

    public static final Supplier<Feature<IcicleClusterFeatureConfig>> ICICLE_FEATURE = RegHelper.registerFeature(
            ImmersiveWeathering.res("icicle_cluster"), () ->
                    new IcicleClusterFeature(IcicleClusterFeatureConfig.CODEC));

    public static void init() {

        //icy
        ResourceKey<PlacedFeature> icicles = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("icicles"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.ICY), GenerationStep.Decoration.TOP_LAYER_MODIFICATION, icicles);

        ResourceKey<PlacedFeature> frost_patch = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("frost_patch"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.ICY), GenerationStep.Decoration.TOP_LAYER_MODIFICATION, frost_patch);


        //soil patches
        ResourceKey<PlacedFeature> loam = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("loam"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_LOAM), GenerationStep.Decoration.RAW_GENERATION, loam);

        ResourceKey<PlacedFeature> silt = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("silt"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_SILT), GenerationStep.Decoration.RAW_GENERATION, silt);

        ResourceKey<PlacedFeature> silt_aquifer = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("silt_aquifer"));
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD), GenerationStep.Decoration.RAW_GENERATION, silt_aquifer);

        ResourceKey<PlacedFeature> sandy_dirt = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("sandy_dirt"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_SANDY_DIRT), GenerationStep.Decoration.RAW_GENERATION, sandy_dirt);

        ResourceKey<PlacedFeature> earthen_clay = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("earthen_clay"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_EARTHEN_CLAY), GenerationStep.Decoration.RAW_GENERATION, earthen_clay);

        ResourceKey<PlacedFeature> permafrost = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("permafrost"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_PERMAFROST), GenerationStep.Decoration.RAW_GENERATION, permafrost);


        //dry lakebed
        ResourceKey<PlacedFeature> dry_lakebed = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("dry_lakebed"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_LAKEBED), GenerationStep.Decoration.RAW_GENERATION, dry_lakebed);

        ResourceKey<PlacedFeature> dry_lakebed_large = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("dry_lakebed_large"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_LAKEBED), GenerationStep.Decoration.RAW_GENERATION, dry_lakebed_large);


        //ivy
        ResourceKey<PlacedFeature> ivy_patch = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("ivy_patch"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_IVY), GenerationStep.Decoration.VEGETAL_DECORATION, ivy_patch);

        ResourceKey<PlacedFeature> dune_grass_patch = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("dune_grass_patch"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_DUNE_GRASS), GenerationStep.Decoration.VEGETAL_DECORATION, dune_grass_patch);


        //moss
        ResourceKey<PlacedFeature> moss_patch = ResourceKey.create(Registries.PLACED_FEATURE, ImmersiveWeathering.res("moss_patch"));
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_MOSS), GenerationStep.Decoration.VEGETAL_DECORATION, moss_patch);


    }
}
