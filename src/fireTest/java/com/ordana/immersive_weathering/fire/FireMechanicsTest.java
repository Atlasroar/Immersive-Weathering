package com.ordana.immersive_weathering.fire;

import com.mojang.authlib.GameProfile;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.mixins.FireMixin;
import com.ordana.immersive_weathering.mixins.accessors.FireBlockInvoker;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FluidState;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/** Executes production helpers; fixtures replace chunk storage, not fire decisions. No Mixin weaving here. */
public final class FireMechanicsTest {
    private static final BlockPos POS = new BlockPos(0, 64, 0);
    private static final Unsafe UNSAFE = unsafe();
    private static int cases;
    private static ProbeFire fire;
    private static ProbeMixin mixin;

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Field bootstrapped = Bootstrap.class.getDeclaredField("isBootstrapped");
        bootstrapped.setAccessible(true);
        bootstrapped.setBoolean(null, true);
        BuiltInRegistries.REGISTRY.keySet();
        fire = Registry.register(BuiltInRegistries.BLOCK, "iw_fire_test:fire", new ProbeFire());
        mixin = Registry.register(BuiltInRegistries.BLOCK, "iw_fire_test:mixin", new ProbeMixin());
        Field field = Blocks.class.getDeclaredField("FIRE");
        UNSAFE.putObject(UNSAFE.staticFieldBase(field), UNSAFE.staticFieldOffset(field), fire);
        bootstrapped.setBoolean(null, false);
        Bootstrap.bootStrap();
        probabilities();
        history();
        heat();
        placement();
        entityGates();
        ignitionSources();
        torches();
        grass();
        thunder();
        hooks();
        System.out.println("Fire mechanics: " + cases + " production assertions and vanilla-hook checks passed");
    }

    private static void probabilities() {
        FireSettings settings = FireSettings.current();
        near(settings.chance(0, 0.8F, false), 0.02, "Small default chance");
        near(settings.chance(8, 2, false), 0.064, "Bounded default hot/heat chance");
        near(settings.chance(8, 2, true), 0.004, "Projectile reduction after heat");
        near(settings.chance(100000, 100, false), settings.chance(8, 2, false), "Source and temperature clamps");
        near(settings.chance(-10, -5, false), 0.02, "Cold/negative source baseline");
        FireSettings capped = new FireSettings(20, 1, 2, 8, 1, 0.0625, 1, 4, 0.25);
        near(capped.chance(8, 2, true), 0.25 / 16, "Projectile cap must remain reduced");
        FireSettings disabled = new FireSettings(20, 1, 2, 8, 0, 0.0625, 1, 4, 1);
        near(disabled.chance(8, 2, false), 0, "Zero chance cannot be revived by heat");
        for (int id : new int[]{0, 1, 19, 20, Integer.MAX_VALUE}) {
            int trials = 0;
            for (int tick = 0; tick < 200; tick++) if (FireSettings.isDue(tick, id, 20)) trials++;
            check(trials == 10, "Exactly one staggered trial per interval");
        }
        for (int sources = 0; sources <= 70; sources++) {
            for (float temp : new float[]{-1, 0, 0.8F, 1, 2, 20}) {
                double chance = capped.chance(sources, temp, false);
                check(chance >= 0 && chance <= 0.25, "Probability bounds");
                check(capped.chance(sources, temp, true) <= chance / 16 + 1e-12, "Projectile ordering");
            }
        }
    }

    private static void history() {
        IgnitionHistory history = new IgnitionHistory();
        check(!history.canSpread(160), "Sunlight/unattributed fire is not authorized");
        history.authorize(0);
        check(!history.canSpread(160), "Zero-duration source cannot authorize");
        history.authorize(160);
        check(history.canSpread(160), "Later legitimate ignition authorizes same burn");
        CompoundTag tag = new CompoundTag();
        history.save(tag, 160);
        IgnitionHistory restored = new IgnitionHistory();
        restored.load(tag, 159);
        check(restored.canSpread(159), "Known ignition survives reload");
        restored.load(tag, 0);
        check(!restored.canSpread(160), "Extinguished load cannot retain permission");
        restored.load(new CompoundTag(), 160);
        check(!restored.canSpread(160), "Legacy/unknown save defaults to no spread");
        history.update(-1);
        check(!history.canSpread(160), "Water/rain/clearFire reset prevents next sun burn spreading");
        history.authorize(1);
        history.update(0);
        check(!history.canSpread(160), "Natural expiry resets provenance");
        history.save(tag, 160);
        restored.load(tag, 160);
        check(!restored.canSpread(160), "False provenance overwrites stale saved true");
    }

    private static void heat() throws Exception {
        Fixture world = world();
        FireSettings settings = FireSettings.current();
        for (Block block : List.of(Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK, Blocks.LAVA)) {
            check(EntityFire.isHeatSource(block.defaultBlockState()), "Required heat inventory " + block);
        }
        for (Block block : List.of(Blocks.TORCH, Blocks.SOUL_TORCH, Blocks.WALL_TORCH,
                Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.OAK_LOG, Blocks.WATER)) {
            check(!EntityFire.isHeatSource(block.defaultBlockState()), "Excluded heat inventory " + block);
        }
        world.states.put(POS, Blocks.LAVA.defaultBlockState());
        world.states.put(POS.east(), Blocks.MAGMA_BLOCK.defaultBlockState());
        world.states.put(POS.west(), Blocks.TORCH.defaultBlockState());
        world.states.put(POS.above(3), Blocks.FIRE.defaultBlockState());
        check(EntityFire.heatSources(world, POS, settings) == 2, "Heat scan radius and inventory");
        for (BlockPos pos : BlockPos.betweenClosed(POS.offset(-2, -2, -2), POS.offset(2, 2, 2))) {
            world.states.put(pos.immutable(), Blocks.MAGMA_BLOCK.defaultBlockState());
        }
        world.reads = 0;
        check(EntityFire.heatSources(world, POS, settings) == 8 && world.reads == 8, "Heat cap short circuits scan");
        world.loaded = false;
        world.reads = 0;
        check(EntityFire.heatSources(world, POS, settings) == 0 && world.reads == 0, "No unloaded heat reads");
        world.loaded = true;
        world.reads = 0;
        EntityFire.heatSources(world, POS.above(500), settings);
        check(world.reads == 0, "No out-of-height reads");
    }

    private static void placement() throws Exception {
        Fixture world = world();
        FireSettings zeroChance = new FireSettings(20, 1, 2, 8, 0, 0.0625, 0.125, 0.5, 0.25);
        FireSettings zeroProjectile = new FireSettings(20, 1, 2, 8, 0.02, 0, 0.125, 0.5, 0.25);
        FireSettings zeroCap = new FireSettings(20, 1, 2, 8, 0.02, 0.0625, 0.125, 0.5, 0);
        check(!EntityFire.spread(world, POS, false, zeroChance)
                && !EntityFire.spread(world, POS, true, zeroProjectile)
                && !EntityFire.spread(world, POS, false, zeroCap) && world.reads == 0, "Zero controls exit before scans");
        world.states.put(POS.below(), Blocks.OAK_PLANKS.defaultBlockState());
        check(EntityFire.canIgnite(world, POS), "Dry supported air next to fuel");
        world.states.put(POS, Blocks.TORCH.defaultBlockState());
        check(!EntityFire.canIgnite(world, POS), "Never overwrite occupied replaceable cell");
        world.states.put(POS, Blocks.WATER.defaultBlockState());
        check(!EntityFire.canIgnite(world, POS), "No water replacement");
        world.states.remove(POS);
        world.rain.add(POS);
        check(!EntityFire.canIgnite(world, POS), "Exposed rain blocks ignition");
        world.rain.clear();
        world.rain.add(POS.north());
        check(!EntityFire.canIgnite(world, POS), "Vanilla-style horizontal rain exposure");
        world.rain.clear();
        world.unloaded.add(POS.east());
        world.reads = 0;
        check(!EntityFire.canIgnite(world, POS) && world.reads == 0, "All neighbors must be loaded before query");
        world.unloaded.clear();
        world.states.put(POS.below(), Blocks.STONE.defaultBlockState());
        check(!EntityFire.canIgnite(world, POS), "Solid support alone is not flammable adjacency");
        world.states.clear();
        check(!EntityFire.canIgnite(world, POS), "Unsupported air is rejected");
        world.states.put(POS.east(), Blocks.OAK_PLANKS.defaultBlockState());
        check(EntityFire.canIgnite(world, POS), "Side fuel permits vanilla fire survival");
        check(!EntityFire.canIgnite(world, new BlockPos(0, -64, 0)), "Build-bottom neighbor is excluded");
        check(!EntityFire.canIgnite(world, new BlockPos(30000001, 64, 0)), "World border is excluded");
        world.states.clear();
        world.states.put(POS.below(), Blocks.GRASS_BLOCK.defaultBlockState());
        check(EntityFire.canIgnite(world, POS), "Grass is eligible fuel");
        CommonConfigs.grass = false;
        check(!EntityFire.canIgnite(world, POS), "Grass toggle updates query without registry mutation");
        CommonConfigs.grass = true;
        FireSettings certain = new FireSettings(20, 1, 2, 8, 1, 1, 0, 0, 1);
        world.rules.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
        check(!EntityFire.spread(world, POS, false, certain) && world.writes == 0, "doFireTick gate");
        world.rules.getRule(GameRules.RULE_DOFIRETICK).set(true, null);
        check(EntityFire.spread(world, POS, false, certain) && world.writes == 1, "At most one ignition per trial");
        check(world.sawPlacementScope && !EntityFire.placingFire(), "Portal suppression scope is temporary");
        check(world.states.get(POS.below()).is(Blocks.GRASS_BLOCK), "Entity ignition never consumes ground directly");
        world.failWrites = true;
        try {
            EntityFire.placeFire(world, POS);
            throw new AssertionError("Expected fixture failure");
        } catch (IllegalStateException expected) {
            check(!EntityFire.placingFire(), "Portal suppression clears on placement failure");
        }
        world.failWrites = false;
        clearFires(world);
        world.writes = 0;
        world.roll = 0.02;
        check(!EntityFire.spread(world, POS, false, FireSettings.current()), "Strict 2% default threshold");
        world.roll = 0.019;
        check(EntityFire.spread(world, POS, false, FireSettings.current()), "Default trial below threshold");
        clearFires(world);
        world.roll = 0.00125;
        check(!EntityFire.spread(world, POS, true, FireSettings.current()), "Exact projectile threshold");
        world.roll = 0.00124;
        check(EntityFire.spread(world, POS, true, FireSettings.current()), "Projectile trial below threshold");
        clearFires(world);
        world.temperature = 2;
        world.roll = 0.025;
        check(EntityFire.spread(world, POS, false, FireSettings.current()), "Hot biome increases actual trial chance");
        Fixture bounded = world();
        FireSettings largest = new FireSettings(20, 3, 4, 64, 1, 1, 0, 0, 1);
        check(EntityFire.heatSources(bounded, POS, largest) == 0 && bounded.reads == 729, "Maximum heat cube is bounded to 729 reads");
    }

    private static void entityGates() throws Exception {
        Fixture world = world();
        ProbeLiving entity = new ProbeLiving(world);
        entity.setPos(POS.getX(), POS.getY(), POS.getZ());
        entity.setSecondsOnFire(8);
        CommonConfigs.chance = 1;
        entity.tickCount = Math.floorMod(entity.getId(), 20);
        EntityFire.tick(entity);
        check(world.reads == 0, "Sunlight-only burning does not scan");
        EntityFire.authorize(entity);
        check(EntityFire.authorized(entity), "Explicit non-sun event authorizes");
        CommonConfigs.spread = false;
        EntityFire.tick(entity);
        check(world.reads == 0, "Disabled spread does not scan");
        CommonConfigs.spread = true;
        world.rules.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
        EntityFire.tick(entity);
        check(world.reads == 0, "doFireTick gate exits before scans");
        world.rules.getRule(GameRules.RULE_DOFIRETICK).set(true, null);
        world.rules.getRule(GameRules.RULE_MOBGRIEFING).set(false, null);
        EntityFire.tick(entity);
        check(world.reads == 0, "Non-player carriers respect mobGriefing");
        world.rules.getRule(GameRules.RULE_MOBGRIEFING).set(true, null);
        entity.wet = true;
        EntityFire.tick(entity);
        check(world.reads == 0, "Wet carriers do not scan");
        entity.wet = false;
        entity.wasInPowderSnow = true;
        EntityFire.tick(entity);
        check(world.reads == 0, "Previous-tick powder snow is respected");
        entity.wasInPowderSnow = false;
        entity.tickCount++;
        EntityFire.tick(entity);
        check(world.reads == 0, "Off-interval carrier does not scan");
        entity.tickCount--;
        EntityFire.tick(entity);
        check(world.reads > 0, "Due authorized carrier executes production scan");
        ProbePlayer player = new ProbePlayer(world);
        Arrow arrow = new Arrow(world, 0, 64, 0);
        check(EntityFire.mayGrief(player, false), "Player spread remains direct player grief");
        check(!EntityFire.mayGrief(arrow, false), "Ownerless projectile respects mobGriefing");
        arrow.setOwner(player);
        check(EntityFire.mayGrief(arrow, false), "Player-owned projectile exemption");
        entity.clearFire();
        entity.immersiveWeathering$ignitionHistory().update(entity.getRemainingFireTicks());
        entity.setSecondsOnFire(8);
        check(!EntityFire.authorized(entity), "Extinguished then sun-burned carrier stays ineligible");
        CommonConfigs.chance = 0.02;
    }

    private static void torches() throws Exception {
        Fixture world = world();
        ProbePlayer player = new ProbePlayer(world);
        for (var item : List.of(Items.TORCH, Items.SOUL_TORCH)) {
            ProbeLiving target = new ProbeLiving(world);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(item, 3));
            EntityFire.afterMeleeHit(player, target, false);
            check(player.getMainHandItem().getCount() == 3 && target.getRemainingFireTicks() <= 0, "Failed/blocked attack keeps torch");
            EntityFire.afterMeleeHit(player, target, true);
            check(player.getMainHandItem().getCount() == 2 && target.getRemainingFireTicks() == 80, "Torch uses one item and ordinary four-second burn");
            check(EntityFire.authorized(target), "Torch marks non-sun origin");
            target.setSecondsOnFire(20);
            EntityFire.afterMeleeHit(player, target, true);
            check(target.getRemainingFireTicks() == 400, "Torch never shortens longer burn");
        }
        ProbeLiving target = new ProbeLiving(world);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.TORCH, 5));
        player.getAbilities().instabuild = true;
        check(EntityFire.torchHit(player, target) && player.getMainHandItem().getCount() == 5, "Creative exemption");
        player.getAbilities().instabuild = false;
        target.wet = true;
        check(!EntityFire.torchHit(player, target), "Wet target does not consume");
        target.wet = false;
        target.immune = true;
        check(!EntityFire.torchHit(player, target), "Fire-immune target does not consume");
        target.immune = false;
        target.resistant = true;
        check(!EntityFire.torchHit(player, target), "Fire-resistant target does not consume");
        target.resistant = false;
        target.isInPowderSnow = true;
        check(!EntityFire.torchHit(player, target), "Powder snow target does not consume");
        target.isInPowderSnow = false;
        CommonConfigs.torches = false;
        check(!EntityFire.torchHit(player, target), "Torch toggle is live");
        CommonConfigs.torches = true;
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Items.TORCH));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        check(!EntityFire.torchHit(player, target), "Off-hand torch is not an attack torch");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.TORCH));
        check(!EntityFire.torchHit(player, new Arrow(world, 0, 64, 0)), "Unsupported nonliving target keeps torch");
        target.preventIgnition = true;
        target.clearFire();
        check(!EntityFire.torchHit(player, target) && player.getMainHandItem().getCount() == 1, "Rejected actual ignition keeps torch");
        target.preventIgnition = false;
        target.setHealth(0);
        check(!EntityFire.torchHit(player, target) && player.getMainHandItem().getCount() == 1, "Dead target keeps torch");
    }

    private static void ignitionSources() throws Exception {
        Fixture world = world();
        ProbeLiving target = new ProbeLiving(world);
        target.setSecondsOnFire(8);
        Arrow arrow = new Arrow(world, 0, 64, 0);
        EntityFire.afterProjectileHit(arrow, target, true);
        check(!EntityFire.authorized(target), "Nonburning successful arrow cannot authorize a sun burn");
        arrow.setSecondsOnFire(100);
        EntityFire.afterProjectileHit(arrow, target, false);
        check(!EntityFire.authorized(target), "Failed burning arrow cannot authorize existing sun fire");
        EntityFire.afterProjectileHit(arrow, target, true);
        check(EntityFire.authorized(target), "Successful burning arrow authorizes an existing sun burn");
        target = new ProbeLiving(world);
        target.setSecondsOnFire(8);
        var fireball = new net.minecraft.world.entity.projectile.SmallFireball(world, 0, 64, 0, 1, 0, 0);
        EntityFire.afterProjectileHit(fireball, target, false);
        check(!EntityFire.authorized(target), "Failed small fireball cannot authorize sun fire");
        EntityFire.afterProjectileHit(fireball, target, true);
        check(EntityFire.authorized(target), "Successful small fireball authorizes sun fire");
        ProbeLiving enderman = new ProbeLiving(world, EntityType.ENDERMAN);
        enderman.setSecondsOnFire(8);
        EntityFire.afterProjectileHit(arrow, enderman, true);
        check(!EntityFire.authorized(enderman), "Arrow Enderman exception remains intact");
        for (LivingEntity attacker : List.of(new ProbeLiving(world), new ProbePlayer(world))) {
            ItemStack weapon = new ItemStack(Items.WOODEN_SWORD);
            weapon.enchant(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT, 1);
            attacker.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            target = new ProbeLiving(world);
            target.setSecondsOnFire(8);
            EntityFire.afterMeleeHit(attacker, target, false);
            check(!EntityFire.authorized(target), "Failed player/mob Fire Aspect does not authorize");
            EntityFire.afterMeleeHit(attacker, target, true);
            check(EntityFire.authorized(target), "Successful player/mob Fire Aspect authorizes same-duration sun fire");
        }
        Fixture client = world();
        Field side = Level.class.getDeclaredField("isClientSide");
        UNSAFE.putBoolean(client, UNSAFE.objectFieldOffset(side), true);
        ProbeLiving clientTarget = new ProbeLiving(client);
        clientTarget.setSecondsOnFire(8);
        EntityFire.authorize(clientTarget);
        check(!EntityFire.authorized(clientTarget), "Client cannot authorize provenance");
        client.states.put(POS, Blocks.GRASS_BLOCK.defaultBlockState());
        check(GrassFire.consume(client, POS) && client.writes == 0, "Client never consumes grass");
    }

    private static void grass() throws Exception {
        Fixture world = world();
        world.states.put(POS, Blocks.GRASS_BLOCK.defaultBlockState());
        check(GrassFire.odds(world.getBlockState(POS), 0, 5) == 5, "Grass ignition odds");
        check(GrassFire.odds(world.getBlockState(POS), 70, 5) == 70, "Preserve larger third-party odds");
        CallbackInfoReturnable<Integer> cir = new CallbackInfoReturnable<>("getBurnOdds", true, 0);
        invokeMixin("grassBurning", new Class<?>[]{BlockState.class, CallbackInfoReturnable.class}, world.getBlockState(POS), cir);
        check(cir.getReturnValue() == 20, "Production burn-odds injection");
        CallbackInfo ci = new CallbackInfo("checkBurnOut", true);
        invokeMixin("preserveGrassGround", new Class<?>[]{Level.class, BlockPos.class, int.class, RandomSource.class, int.class, CallbackInfo.class},
                world, POS, 250, world.random, 0, ci);
        check(ci.isCancelled() && world.getBlockState(POS).is(Blocks.DIRT) && world.writes == 1, "Successful burn keeps dirt at same position");
        for (Block block : List.of(Blocks.PODZOL, Blocks.MYCELIUM, Blocks.DIRT, Blocks.OAK_LOG, Blocks.SNOW)) {
            world.states.put(POS, block.defaultBlockState());
            check(!GrassFire.consume(world, POS) && world.getBlockState(POS).is(block), "Unrelated block retained " + block);
        }
        world.states.put(POS, Blocks.GRASS_BLOCK.defaultBlockState());
        CommonConfigs.grass = false;
        check(GrassFire.odds(world.getBlockState(POS), 0, 5) == 0
                && GrassFire.odds(world.getBlockState(POS), 10, 5) == 10, "Disable restores native/mod odds");
        check(!GrassFire.consume(world, POS) && world.getBlockState(POS).is(Blocks.GRASS_BLOCK), "Disable retains grass");
        CommonConfigs.grass = true;
        world.rules.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
        check(GrassFire.consume(world, POS) && world.getBlockState(POS).is(Blocks.GRASS_BLOCK), "doFireTick false never consumes grass");
    }

    private static void hooks() throws Exception {
        MethodNode burn = method(FireBlock.class, "checkBurnOut", "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/util/RandomSource;I)V");
        int states = 0;
        boolean successBranch = false;
        for (AbstractInsnNode insn : burn.instructions) {
            if (insn instanceof JumpInsnNode jump && jump.getOpcode() == Opcodes.IF_ICMPGE) successBranch = true;
            if (insn instanceof MethodInsnNode call && call.name.equals("getBlockState")) {
                if (states++ == 1) check(successBranch, "Grass hook is inside successful burn branch");
            }
        }
        check(states == 2, "Exact grass burn ordinal");
        check(calls(method(Player.class, "attack", "(Lnet/minecraft/world/entity/Entity;)V"), "net/minecraft/world/entity/Entity", "hurt") == 1,
                "Torch redirect selects only direct target, not sweeping LivingEntity.hurt calls");
        method(Entity.class, "setSecondsOnFire", "(I)V");
        method(Entity.class, "baseTick", "()V");
        method(Entity.class, "saveWithoutId", "(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;");
        method(Entity.class, "load", "(Lnet/minecraft/nbt/CompoundTag;)V");
        method(Entity.class, "lavaHurt", "()V");
        method(Entity.class, "thunderHit", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LightningBolt;)V");
        method(BaseFireBlock.class, "entityInside", "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V");
        check(calls(method(net.minecraft.world.entity.projectile.AbstractArrow.class, "onHitEntity", "(Lnet/minecraft/world/phys/EntityHitResult;)V"),
                "net/minecraft/world/entity/Entity", "hurt") == 1, "Burning arrow success hook");
        check(calls(method(net.minecraft.world.entity.projectile.SmallFireball.class, "onHitEntity", "(Lnet/minecraft/world/phys/EntityHitResult;)V"),
                "net/minecraft/world/entity/Entity", "hurt") == 1, "Small fireball success hook");
        method(Mob.class, "doHurtTarget", "(Lnet/minecraft/world/entity/Entity;)Z");
        check(calls(method(net.minecraft.world.entity.monster.Zombie.class, "doHurtTarget", "(Lnet/minecraft/world/entity/Entity;)Z"),
                "net/minecraft/world/entity/Entity", "setSecondsOnFire") == 1, "Zombie melee ignition hook");
        check(calls(method(BaseFireBlock.class, "onPlace", "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"),
                "net/minecraft/world/level/portal/PortalShape", "findEmptyPortalShape") == 1, "Portal suppression hook");
        MethodNode tick = method(FireBlock.class, "tick", "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V");
        check(calls(tick, "net/minecraft/server/level/ServerLevel", "removeBlock") >= 4, "Existing soot ordinal remains available");
        check(calls(tick, "net/minecraft/world/level/block/FireBlock", "checkBurnOut") == 6, "All six neighboring burn attempts are wrapped");
        check(calls(tick, "net/minecraft/server/level/ServerLevel", "setBlock") == 2, "Both age write and distant spread hook exist");
    }

    private static void thunder() throws Exception {
        Fixture world = world();
        world.sky = true;
        check(!ThunderFire.affected(world, POS), "Clear weather preserves fire");
        world.thunder = true;
        check(world.rain.isEmpty() && ThunderFire.affected(world, POS), "Thunder applies in dry biomes without rainfall");
        near(ThunderFire.multiplier(world, POS), 0.1, "Default 90 percent suppression");
        world.roll = 0.099999;
        check(ThunderFire.allowSpread(world, POS, world.random), "Below suppression threshold spreads");
        world.roll = 0.1;
        check(!ThunderFire.allowSpread(world, POS, world.random), "Exact suppression threshold is rejected");
        int permitted = 0;
        for (int i = 0; i < 1000; i++) {
            world.roll = i / 1000.0;
            if (ThunderFire.allowSpread(world, POS, world.random)) permitted++;
        }
        check(permitted == 100, "Exact default permits ten percent of deterministic quantiles");
        CommonConfigs.thunderMultiplier = 0;
        world.roll = 0;
        check(!ThunderFire.allowSpread(world, POS, world.random), "Zero multiplier prevents every attempt");
        CommonConfigs.thunderMultiplier = 1;
        world.roll = 0.999;
        check(ThunderFire.allowSpread(world, POS, world.random), "Multiplier one restores attempts");
        CommonConfigs.thunderMultiplier = 0.1;
        world.states.put(POS, Blocks.FIRE.defaultBlockState());
        world.roll = 0.25;
        check(!ThunderFire.extinguish(world, POS, world.random) && world.getBlockState(POS).is(Blocks.FIRE),
                "Exact extinguish threshold is rejected");
        world.roll = 0.249999;
        CallbackInfo callback = new CallbackInfo("tick", true);
        invokeMixin("thunderExtinguish", new Class<?>[]{BlockState.class, ServerLevel.class, BlockPos.class,
                RandomSource.class, CallbackInfo.class}, Blocks.FIRE.defaultBlockState(), world, POS, world.random, callback);
        check(callback.isCancelled() && world.getBlockState(POS).isAir(), "Actual mixin entry cancels an extinguished fire tick");
        world.states.put(POS, Blocks.FIRE.defaultBlockState());
        world.roll = 0;
        CommonConfigs.thunderExtinguish = 0;
        check(!ThunderFire.extinguish(world, POS, world.random), "Zero extinguish chance preserves fire");
        CommonConfigs.thunderExtinguish = 0.25;
        world.sky = false;
        check(!ThunderFire.affected(world, POS) && !ThunderFire.extinguish(world, POS, world.random),
                "Roof preserves fire and normal spreading");
        near(ThunderFire.multiplier(world, POS), 1, "Sheltered entity trials retain their probability");
        world.sky = true;
        world.rules.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
        check(!ThunderFire.affected(world, POS) && !ThunderFire.extinguish(world, POS, world.random), "doFireTick false preserves fire");
        world.rules.getRule(GameRules.RULE_DOFIRETICK).set(true, null);
        CommonConfigs.thunderDampening = false;
        check(!ThunderFire.affected(world, POS), "Live config disables thunder effects");
        CommonConfigs.thunderDampening = true;
        world.states.put(POS.below(), Blocks.OAK_PLANKS.defaultBlockState());
        world.states.remove(POS);
        FireSettings certain = new FireSettings(20, 1, 0, 1, 1, 1, 0, 0, 1);
        world.roll = 0.1;
        check(!EntityFire.spread(world, POS, false, certain), "Exposed burning-entity trial is reduced before target scanning");
        world.roll = 0.099999;
        check(EntityFire.spread(world, POS, false, certain), "Reduced entity trial still can ignite");
    }

    private static MethodNode method(Class<?> type, String name, String descriptor) throws Exception {
        ClassNode node = new ClassNode();
        try (var stream = type.getResourceAsStream("/" + type.getName().replace('.', '/') + ".class")) {
            new ClassReader(Objects.requireNonNull(stream)).accept(node, 0);
        }
        MethodNode result = node.methods.stream().filter(m -> m.name.equals(name) && m.desc.equals(descriptor)).findFirst().orElseThrow();
        cases++;
        return result;
    }

    private static int calls(MethodNode method, String owner, String name) {
        int count = 0;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals(name)) count++;
        }
        return count;
    }

    private static void invokeMixin(String name, Class<?>[] types, Object... args) throws Exception {
        Method method = FireMixin.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        method.invoke(mixin, args);
    }

    private static void clearFires(Fixture world) {
        world.states.entrySet().removeIf(entry -> entry.getValue().is(Blocks.FIRE) || entry.getValue().is(Blocks.SOUL_FIRE));
    }

    private static Fixture world() throws Exception {
        Fixture world = (Fixture) UNSAFE.allocateInstance(Fixture.class);
        world.states = new HashMap<>();
        world.rain = new HashSet<>();
        world.unloaded = new HashSet<>();
        world.rules = new GameRules();
        world.border = new WorldBorder();
        world.loaded = true;
        world.roll = 0;
        world.temperature = 0.8F;
        Field random = Level.class.getDeclaredField("random");
        UNSAFE.putObject(world, UNSAFE.objectFieldOffset(random), new net.minecraft.world.level.levelgen.LegacyRandomSource(1) {
            @Override public double nextDouble() { return world.roll; }
        });
        return world;
    }

    private static void near(double actual, double expected, String message) { check(Math.abs(actual - expected) < 1e-8, message + ": " + actual); }
    private static void check(boolean passed, String message) { cases++; if (!passed) throw new AssertionError(message); }
    private static Unsafe unsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); }
    }

    private static final class ProbeMixin extends FireMixin {
        private ProbeMixin() { super(BlockBehaviour.Properties.of(), 1); }
        @Override protected boolean canBurn(BlockState state) { return false; }
    }

    private static final class ProbeFire extends FireBlock implements FireBlockInvoker {
        private ProbeFire() {
            super(BlockBehaviour.Properties.copy(Blocks.FIRE));
            for (String name : List.of("igniteOdds", "burnOdds")) {
                try {
                    Field field = FireBlock.class.getDeclaredField(name);
                    field.setAccessible(true);
                    field.set(this, field.get(Blocks.FIRE));
                } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
            }
        }
        @Override public int immersiveWeathering$igniteOdds(LevelReader level, BlockPos pos) {
            if (!level.isEmptyBlock(pos)) return 0;
            int odds = 0;
            for (Direction direction : Direction.values()) {
                BlockState state = level.getBlockState(pos.relative(direction));
                try {
                    Method method = FireBlock.class.getDeclaredMethod("getIgniteOdds", BlockState.class);
                    method.setAccessible(true);
                    int original = (int) method.invoke(this, state);
                    odds = Math.max(odds, GrassFire.odds(state, original, 5));
                } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
            }
            return odds;
        }
        @Override protected boolean canBurn(BlockState state) {
            return super.canBurn(state) || GrassFire.odds(state, 0, 5) > 0;
        }
    }

    private static final class ProbeLiving extends LivingEntity implements FireCarrier {
        private final IgnitionHistory history = new IgnitionHistory();
        private final Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        private boolean wet, immune, resistant, preventIgnition;
        private ProbeLiving(Level world) { this(world, EntityType.PIG); }
        private ProbeLiving(Level world, EntityType<? extends LivingEntity> type) { super(type, world); }
        @Override public IgnitionHistory immersiveWeathering$ignitionHistory() { return history; }
        @Override public Iterable<ItemStack> getArmorSlots() { return List.of(); }
        @Override public ItemStack getItemBySlot(EquipmentSlot slot) { return equipment.getOrDefault(slot, ItemStack.EMPTY); }
        @Override public void setItemSlot(EquipmentSlot slot, ItemStack stack) { equipment.put(slot, stack); }
        @Override public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }
        @Override public boolean isInWaterRainOrBubble() { return wet; }
        @Override public boolean fireImmune() { return immune; }
        @Override public void setSecondsOnFire(int seconds) { if (!preventIgnition) super.setSecondsOnFire(seconds); }
        @Override public boolean hasEffect(net.minecraft.world.effect.MobEffect effect) { return resistant && effect == MobEffects.FIRE_RESISTANCE; }
    }

    private static final class ProbePlayer extends Player implements FireCarrier {
        private final IgnitionHistory history = new IgnitionHistory();
        private ProbePlayer(Level world) { super(world, POS, 0, new GameProfile(UUID.randomUUID(), "fire_fixture")); }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return getAbilities().instabuild; }
        @Override public IgnitionHistory immersiveWeathering$ignitionHistory() { return history; }
    }

    private static final class Fixture extends ServerLevel {
        private Map<BlockPos, BlockState> states;
        private Set<BlockPos> rain, unloaded;
        private GameRules rules;
        private WorldBorder border;
        private boolean loaded, failWrites, sawPlacementScope, thunder, sky;
        private int reads, writes;
        private double roll;
        private float temperature;
        private Fixture() { super(null, null, null, null, null, null, null, false, 0, List.of(), false, null); throw new AssertionError(); }
        @Override public BlockState getBlockState(BlockPos pos) {
            check(isLoaded(pos) && !isOutsideBuildHeight(pos), "Fixture caught an unloaded/out-of-height read");
            reads++;
            return states.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }
        @Override public FluidState getFluidState(BlockPos pos) { return getBlockState(pos).getFluidState(); }
        @Override public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursion) {
            if (failWrites) throw new IllegalStateException("Fixture write failure");
            sawPlacementScope |= EntityFire.placingFire();
            states.put(pos.immutable(), state);
            writes++;
            return true;
        }
        @Override public int getMinBuildHeight() { return -64; }
        @Override public int getHeight() { return 384; }
        @Override public boolean isLoaded(BlockPos pos) { return loaded && !unloaded.contains(pos); }
        @Override public boolean isRainingAt(BlockPos pos) { return rain.contains(pos); }
        @Override public boolean isThundering() { return thunder; }
        @Override public boolean canSeeSky(BlockPos pos) { return sky; }
        @Override public WorldBorder getWorldBorder() { return border; }
        @Override public GameRules getGameRules() { return rules; }
        @Override public RegistryAccess registryAccess() { return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY); }
        @Override public Holder<Biome> getBiome(BlockPos pos) {
            return Holder.direct(new Biome.BiomeBuilder().hasPrecipitation(true).temperature(temperature).downfall(0)
                    .specialEffects(new BiomeSpecialEffects.Builder().fogColor(0).waterColor(0).waterFogColor(0).skyColor(0).build())
                    .generationSettings(BiomeGenerationSettings.EMPTY).mobSpawnSettings(MobSpawnSettings.EMPTY).build());
        }
    }
}
