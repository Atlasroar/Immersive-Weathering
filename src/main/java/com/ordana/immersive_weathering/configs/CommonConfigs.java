package com.ordana.immersive_weathering.configs;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CommonConfigs {


    public static final ConfigSpec SERVER_SPEC;
    public static final Supplier<Boolean> SANDSTORM_DEPOSITION;
    public static final Supplier<Integer> SANDSTORM_LAYER_CAP;
    public static final Supplier<Boolean> SANDSTORM_SLOWING;
    public static final Supplier<Double> SANDSTORM_SLOW_AMOUNT;

    public static final Supplier<Boolean> BLOCK_GROWTHS;
    public static final Supplier<List<String>> DISABLED_GROWTHS;
    public static final Supplier<Boolean> CREATIVE_TAB;
    public static final Supplier<Boolean> CREATIVE_DROP;
    public static final Supplier<Boolean> DEBUG_RESOURCES;

    public static final Supplier<Double> MOSS_INTERESTS_FOR_FACE;
    public static final Supplier<Double> MOSS_PATCHINESS;
    public static final Supplier<Double> MOSS_IMMUNE_CHANCE;
    public static final Supplier<Boolean> MOSS_NEEDS_AIR;
    public static final Supplier<Boolean> MOSS_SPREADING_ENABLED;


    public static final Supplier<Boolean> CRACKING_DROPS_BRICK;
    public static final Supplier<Double> CRACK_INTERESTS_FOR_FACE;
    public static final Supplier<Double> CRACK_PATCHINESS;
    public static final Supplier<Double> CRACK_IMMUNE_CHANCE;
    public static final Supplier<Boolean> CRACK_NEEDS_AIR;
    public static final Supplier<Boolean> CRACK_SPREADING_ENABLED;


    public static final Supplier<Boolean> FALLING_ICICLES;
    public static final Supplier<Integer> ICICLE_RARITY;
    public static final Supplier<Boolean> DISABLE_ICICLES;

    public static final Supplier<Integer> FREEZING_ICICLE_SEVERITY;

    public static final Supplier<Double> FIRE_CHARS_WOOD_CHANCE;
    public static final Supplier<Boolean> SOOT_SPAWN;
    public static final Supplier<Boolean> FLAMMABLE_COBWEBS;
    public static final Supplier<Boolean> FIRE_ENTITY_SPREAD;
    public static final Supplier<Boolean> FIRE_TORCH_ATTACKS;
    public static final Supplier<Boolean> FIRE_FLAMMABLE_GRASS;
    public static final Supplier<Integer> FIRE_SPREAD_INTERVAL;
    public static final Supplier<Integer> FIRE_SPREAD_RADIUS;
    public static final Supplier<Integer> FIRE_HEAT_RADIUS;
    public static final Supplier<Integer> FIRE_HEAT_SOURCE_CAP;
    public static final Supplier<Double> FIRE_SPREAD_CHANCE;
    public static final Supplier<Double> FIRE_PROJECTILE_MULTIPLIER;
    public static final Supplier<Double> FIRE_HEAT_BONUS;
    public static final Supplier<Double> FIRE_TEMPERATURE_BONUS;
    public static final Supplier<Double> FIRE_SPREAD_CHANCE_CAP;


    public static final Supplier<Boolean> PICKAXE_CRACKING;
    public static final Supplier<Boolean> PICKAXE_CRACKING_SHIFT;
    public static final Supplier<Boolean> BRICK_REPAIRING;
    public static final Supplier<Boolean> AZALEA_SHEARING;
    public static final Supplier<Boolean> MOSS_SHEARING;
    public static final Supplier<Boolean> MOSS_BURNING;
    public static final Supplier<Boolean> SHOVEL_EXTINGUISH;
    public static final Supplier<Boolean> SPONGE_RUSTING;
    public static final Supplier<Boolean> SPONGE_RUST_DRYING;
    public static final Supplier<Boolean> AXE_STRIPPING;
    public static final Supplier<Boolean> AXE_SCRAPING;


    public static final Supplier<Boolean> ICICLE_FOOD;
    public static final Supplier<Boolean> ICICLE_FIRE_RESISTANCE;

    public static final Supplier<Boolean> COMPOSTER_DIRT;
    public static final Supplier<Boolean> LEGGINGS_PREVENTS_THORN_DAMAGE;
    public static final Supplier<String> GENERIC_BARK;
    public static final Supplier<Boolean> GRASS_OVER_MYCELIUM;
    public static final Supplier<Boolean> MYCELIUM_OVER_GRASS;

    public static final Supplier<Boolean> LEAF_PILES_SLOW;
    public static final Supplier<Double> LEAF_PILES_FROM_DECAY_CHANCE;
    public static final Supplier<Integer> LEAF_PILE_MAX_HEIGHT;
    public static final Supplier<Integer> LEAF_PILES_REACH;
    public static final Supplier<List<String>> LEAF_PILES_BLACKLIST;


    public static final Supplier<Boolean> THIN_ICE_MELTING;
    public static final Supplier<Boolean> SNOW_HOSTS;
    public static final Supplier<Boolean> FALLING_SNOW;
    public static final Supplier<Boolean> REPLACE_PARTIAL_SNOW;
    public static final Supplier<Boolean> SNOW_ACCUMULATION;
    public static final Supplier<Boolean> SNOW_ACCUMULATION_WITHOUT_THUNDER;
    public static final Supplier<Integer> SNOW_COLUMN_CAP;

    public static final Supplier<Boolean> VITRIFIED_LIGHTNING;
    public static final Supplier<Double> FULGURITE_CHANCE;


    public static final Supplier<Boolean> RUSTING;
    public static final Supplier<Integer> RUSTING_INFLUENCE_RADIUS;
    public static final Supplier<Double> RUSTING_RATE;
    public static final Supplier<Boolean> RUST_STREAKING;


    public static void init() {
    }

    static{
        ConfigBuilder builder = ConfigBuilder.create(ImmersiveWeathering.res("common"), ConfigType.COMMON);

        builder.setSynced();

        builder.push("sandstorms");
        SANDSTORM_DEPOSITION = builder.comment("Deposit existing IW sand layers in exposed dry desert/badlands during vanilla rain. Independent of block growths/randomTickSpeed; no cauldron changes. Yields to installed Scorchful.")
                .define("deposition", true);
        SANDSTORM_LAYER_CAP = builder.comment("Maximum storm-deposited layers per pile (1-8). Never reduces existing piles; not a column-height limit or legacy sandy-block growth setting.")
                .define("layer_cap", 1, 1, 8);
        SANDSTORM_SLOWING = builder.comment("Slow exposed living entities during sandstorms. Camel and sandstorm_slow_immune entity tag are exempt. Existing modifiers are cleaned up when disabled.")
                .define("slowing", true);
        SANDSTORM_SLOW_AMOUNT = builder.comment("Fraction of total movement speed removed (0.3 = 30%). No damage, thirst or temperature effects.")
                .define("slow_amount", 0.3, 0, 1);
        builder.pop();

        builder.push("general");
        BLOCK_GROWTHS = builder.define("block_growths", true);
        DISABLED_GROWTHS = builder.comment("put here the name of a block growth json you want to disable i.e: [weeds, weeds_spread]." +
                        "Note that this is not the preferred way to do this as block growths are all data driven so it would be best to disable or tweak them by creating a datapack that overrides them" +
                        "Check the mod data folder for the required names. Requires resource reload (/data reload)")
                .define("block_growth_blacklist", new ArrayList<>());
        CREATIVE_TAB = builder.define("creative_tab", false);
        CREATIVE_DROP = builder.comment("Drop stuff when in creative").define("drop_in_creative", false);
        DEBUG_RESOURCES = builder.comment("Save generated resources to disk in a 'debug' folder in your game directory. Mainly for debug purposes but can be used to generate assets in all wood types for your mods :0")
                .define("debug_save_dynamic_pack", false);

        builder.pop();

        builder.push("mossy_blocks");
        MOSS_INTERESTS_FOR_FACE = builder.define("interest_for_face", 0.3, 0, 1d);
        MOSS_PATCHINESS = builder.define("patchiness", 0.5, 0, 1);
        MOSS_IMMUNE_CHANCE = builder.define("immune_chance", 0.4, 0, 1);
        MOSS_NEEDS_AIR = builder.define("needs_air", true);
        MOSS_SPREADING_ENABLED = builder.define("enabled", true);
        builder.pop();

        builder.push("cracked_blocks");
        CRACKING_DROPS_BRICK = builder.define("cracking_drops_brick", false);
        CRACK_INTERESTS_FOR_FACE = builder.define("interest_for_face", 0.6, 0, 1d);
        CRACK_PATCHINESS = builder.define("patchiness", 0.4, 0, 1);
        CRACK_IMMUNE_CHANCE = builder.define("immune_chance", 0.4, 0, 1);
        CRACK_NEEDS_AIR = builder.define("needs_air", false);
        CRACK_SPREADING_ENABLED = builder.define("enabled", true);

        builder.pop();

        builder.push("icicle");
        FALLING_ICICLES = builder.define("react_to_vibrations", true);
        ICICLE_RARITY = builder.define("spawn_rarity", 12, 1, 100);
        DISABLE_ICICLES = builder.define("disable_icicles", false);
        builder.pop();

        builder.push("freezing");
        //all these are disabled when at 0 of course
        FREEZING_ICICLE_SEVERITY = builder.define("icicle", 300, 0, 1000);
        builder.pop();
        builder.setSynced();

        builder.push("charring");
        FIRE_CHARS_WOOD_CHANCE = builder.define("fire_chars_wood", 0.3, 0, 1);
        SOOT_SPAWN = builder.define("soot_spawn", true);
        FLAMMABLE_COBWEBS = builder.define("flammable_cobweb", true);
        builder.pop();

        builder.push("fire");
        FIRE_ENTITY_SPREAD = builder.comment("Allow non-sun burning entities to ignite nearby flammable blocks. Requires doFireTick.")
                .define("entity_spread", true);
        FIRE_TORCH_ATTACKS = builder.comment("Successful direct melee with a torch or soul torch burns a living target for 4 seconds; consumes one item outside Creative.")
                .define("torch_attacks", true);
        FIRE_FLAMMABLE_GRASS = builder.comment("Grass blocks can burn into dirt at the same position, without drops. Requires doFireTick.")
                .define("flammable_grass", true);
        FIRE_SPREAD_INTERVAL = builder.comment("Ticks between entity-staggered spread trials.")
                .define("spread_interval", 20, 1, 1200);
        FIRE_SPREAD_RADIUS = builder.comment("Cube radius in blocks for nearby air cells adjacent to flammable blocks.")
                .define("spread_radius", 1, 1, 3);
        FIRE_HEAT_RADIUS = builder.comment("Cube radius in blocks for counting fire, soul fire, magma and lava. No torches or campfires.")
                .define("heat_radius", 2, 0, 4);
        FIRE_HEAT_SOURCE_CAP = builder.comment("Maximum counted heat sources per trial.")
                .define("heat_source_cap", 8, 1, 64);
        FIRE_SPREAD_CHANCE = builder.comment("Base probability of one spread trial, not a probability per nearby cell.")
                .define("spread_chance", 0.02, 0, 1);
        FIRE_PROJECTILE_MULTIPLIER = builder.comment("Projectile multiplier applied after the chance cap; 0 disables projectile spreading.")
                .define("projectile_multiplier", 0.0625, 0, 1);
        FIRE_HEAT_BONUS = builder.comment("Additive multiplier bonus per counted heat source.")
                .define("heat_bonus", 0.125, 0, 1);
        FIRE_TEMPERATURE_BONUS = builder.comment("Additive multiplier bonus per biome temperature above 0.8, clamped to temperature 2. Seasons are not integrated.")
                .define("temperature_bonus", 0.5, 0, 4);
        FIRE_SPREAD_CHANCE_CAP = builder.comment("Maximum trial probability before the projectile reduction.")
                .define("spread_chance_cap", 0.25, 0, 1);
        builder.pop();


        builder.push("item_interaction");
        PICKAXE_CRACKING = builder.define("pickaxe_cracking", true);
        PICKAXE_CRACKING_SHIFT = builder.define("pickaxe_cracking_shift", false);
        BRICK_REPAIRING = builder.define("brick_breaking", true);
        AZALEA_SHEARING = builder.define("azalea_shearing", true);
        MOSS_SHEARING = builder.define("moss_shearing", true);
        MOSS_BURNING = builder.define("moss_burning", true);
        SHOVEL_EXTINGUISH = builder.define("shovel_extinguish", true);
        SPONGE_RUSTING = builder.define("sponge_rusting", true);
        SPONGE_RUST_DRYING = builder.define("sponge_rust_drying", false);
        AXE_STRIPPING = builder.define("axe_stripping", true);
        AXE_SCRAPING = builder.define("axe_rusting", true);
        builder.pop();

        builder.push("food");
        ICICLE_FOOD = builder.define("icicle_food", true);
        ICICLE_FIRE_RESISTANCE = builder.define("icicle_fire_resistance", true);
        builder.pop();

        builder.push("misc");
        COMPOSTER_DIRT = builder.define("composter_dirt", true);
        GRASS_OVER_MYCELIUM = builder.define("grass_over_mycelium", true);
        MYCELIUM_OVER_GRASS = builder.define("mycelium_over_grass", true);
        GENERIC_BARK = builder.define("generic_bark", "");
        LEGGINGS_PREVENTS_THORN_DAMAGE = builder.define("leggings_prevents_thorn_damage", true);
        builder.pop();

        builder.push("leaf_piles");
        LEAF_PILES_SLOW = builder.define("leaf_piles_slow", true);
        LEAF_PILES_FROM_DECAY_CHANCE = builder.define("spawn_entity_from_decay", 0.3, 0, 1);

        LEAF_PILES_REACH = builder.define("reach", 12, 1, 256);
        LEAF_PILE_MAX_HEIGHT = builder.define("max_pile_height", 3, 1, 8);
        LEAF_PILES_BLACKLIST = builder.comment("leaves that wont spawn leaf piles").define("leaf_piles_blacklist", List.of());
        builder.pop();

        builder.push("thin_ice");
        THIN_ICE_MELTING = builder.define("natural_melting", false);
        builder.pop();

        builder.push("snow");
        SNOW_HOSTS = builder.comment("Allow snow to share a cell with supported dry fences, walls and plants.")
                .define("host_mixing", true);
        FALLING_SNOW = builder.comment("Unsupported snow falls without moving the plant, fence or wall.")
                .define("falling", true);
        REPLACE_PARTIAL_SNOW = builder.comment("Allow ordinary block items to replace partial snow and eligible covered plants, but not eight-layer covers.")
                .define("replace_partial_layers", true);
        SNOW_ACCUMULATION = builder.comment("Add weather snow up to the column cap; vanilla initial snowfall remains when disabled.")
                .define("accumulation", true);
        SNOW_ACCUMULATION_WITHOUT_THUNDER = builder.comment("Allow extra accumulation during ordinary snowfall instead of thunderstorms only.")
                .define("accumulate_without_thunder", false);
        SNOW_COLUMN_CAP = builder.comment("Maximum weather-deposited layers in one cell. Gamerule 0 disables weather snow; 1 uses this cap; other positive values limit it.")
                .define("column_cap", 8, 1, 8);
        builder.pop();

        builder.push("lightning_growths"); //TODO:move to data
        VITRIFIED_LIGHTNING = builder.define("vitrified_lightning", true);
        FULGURITE_CHANCE = builder.comment("chance that a lightning strike on sand creates fulgurite")
                .define("fulgurite_chance", 0.4, 0, 1);
        builder.pop();

        builder.push("rusting");
        RUSTING = builder.define("rusting", true);
        RUSTING_INFLUENCE_RADIUS = builder.define("rusting_influence_radius", 4, 1, 8);
        RUSTING_RATE = builder.define("rusting_rate", 0.06, 0, 1);
        RUST_STREAKING = builder.define("rust_streaking", true);
        builder.pop();

        SERVER_SPEC = builder.buildAndRegister();
        SERVER_SPEC.loadFromFile();
    }


}
