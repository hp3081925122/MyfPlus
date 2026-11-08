package com.hp.myfplus.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEffects;
import com.hp.myfplus.registry.MyfItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = Myfplus.MOD_ID)
public final class AccessoryEvents {
    private static final int SPIKE_FALL = 8;
    private static final int BELL_RESET = 100;
    private static final List<Spike> SPIKES = new ArrayList<>();
    private static final Map<UUID, int[]> BELL = new HashMap<>();
    private static boolean busy;

    private record Spike(ServerLevel level, Player owner, LivingEntity victim, long at, float damage) {
    }

    private AccessoryEvents() {
    }

    private static boolean worn(LivingEntity e, RegistryObject<Item> item) {
        return CuriosApi.getCuriosInventory(e).map(i -> i.isEquipped(item.get())).orElse(false);
    }

    private static double attack(Player p) {
        return p.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static boolean foe(Player p, LivingEntity v) {
        if (v == p || !v.isAlive() || v instanceof Player || v.isAlliedTo(p)) return false;
        return !(v instanceof OwnableEntity o && p.getUUID().equals(o.getOwnerUUID()));
    }

    private static void strike(Player p, LivingEntity v, DamageSource source, float amount) {
        busy = true;
        try {
            v.invulnerableTime = 0;
            v.hurt(source, amount);
        }
        finally {
            busy = false;
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        LivingEntity victim = e.getEntity();
        if (busy || victim.level().isClientSide || !(e.getSource().getEntity() instanceof Player p) || p == victim) return;
        boolean melee = e.getSource().getDirectEntity() == p;
        if (worn(p, MyfItems.HOUSE_DIE_RING)) e.setAmount(e.getAmount() * roll(p, victim));
        if (melee && worn(p, MyfItems.FOSSIL_FANG_BRACELET)) fang(p, victim);
        if (worn(p, MyfItems.RESONANT_BELT_BELL)) bell(p, victim, e.getAmount());
    }

    private static float roll(Player p, LivingEntity victim) {
        int n = 1 + p.getRandom().nextInt(6);
        float mult = n == 1 ? MyfConfig.DIE_LOW.get().floatValue() : n == 6 ? MyfConfig.DIE_HIGH.get().floatValue() : 1 + n * MyfConfig.DIE_STEP.get().floatValue();
        Vec3 at = victim.position().add(0, victim.getBbHeight() + 0.4, 0);
        victim.level().playSound(null, at.x, at.y, at.z, SoundEvents.NOTE_BLOCK_HAT.get(), SoundSource.PLAYERS, 0.6f, 0.6f + n * 0.15f);
        if (n == 6) {
            FxEntity.at(victim.level(), FxType.FT_JACKPOT, at).size(0.6f).spawn();
            victim.level().playSound(null, at.x, at.y, at.z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.4f);
        }
        else if (n == 1) FxEntity.at(victim.level(), FxType.FT_SMOKE, at).size(0.5f).spawn();
        return mult;
    }

    private static void fang(Player p, LivingEntity victim) {
        Item item = MyfItems.FOSSIL_FANG_BRACELET.get();
        if (p.getCooldowns().isOnCooldown(item) || p.getRandom().nextDouble() >= MyfConfig.FANG_CHANCE.get()) return;
        if (MyfConfig.FANG_COOLDOWN.get() > 0) p.getCooldowns().addCooldown(item, MyfConfig.FANG_COOLDOWN.get());
        ServerLevel level = (ServerLevel) victim.level();
        Vec3 g = new Vec3(victim.getX(), GroundProbe.under(level, victim.getX(), victim.getZ(), victim.getY() + 0.5), victim.getZ());
        FxEntity spike = FxEntity.at(level, FxType.SJ_SPIKE, g).count(p.getRandom().nextInt(3)).life(SPIKE_FALL + 20);
        spike.offY = -0.6;
        spike.spawn();
        float dmg = (float) (MyfConfig.FANG_BASE.get() + MyfConfig.FANG_SCALE.get() * attack(p));
        SPIKES.add(new Spike(level, p, victim, level.getGameTime() + SPIKE_FALL, dmg));
    }

    private static void bell(Player p, LivingEntity victim, float amount) {
        long now = victim.level().getGameTime();
        int[] st = BELL.computeIfAbsent(p.getUUID(), k -> new int[2]);
        if (now - st[1] > BELL_RESET) st[0] = 0;
        st[1] = (int) now;
        if (++st[0] < MyfConfig.BELL_HITS.get()) return;
        st[0] = 0;
        float r = MyfConfig.BELL_RADIUS.get().floatValue();
        float dmg = amount * MyfConfig.BELL_RATIO.get().floatValue();
        Vec3 c = victim.position();
        Vec3 g = new Vec3(c.x, GroundProbe.under(victim.level(), c.x, c.z, c.y + 0.5), c.z);
        FxEntity.at(victim.level(), FxType.BR_PILLAR, g).size(r * 0.5f).spawn();
        FxEntity.at(victim.level(), FxType.BR_IMPACT, g).size(r * 2.2f).spawn();
        victim.level().playSound(null, c.x, c.y, c.z, SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2f, 1.3f);
        if (dmg <= 0) return;
        DamageSource src = p.damageSources().indirectMagic(p, p);
        for (LivingEntity v : victim.level().getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r, 2, r), v -> foe(p, v))) {
            if (v.distanceToSqr(c) > r * r + 4) continue;
            strike(p, v, src, dmg);
            Vec3 d = v.position().subtract(c);
            if (d.lengthSqr() > 1.0E-4) v.knockback(0.4, -d.x, -d.z);
        }
    }

    @SubscribeEvent
    public static void onAttacked(LivingAttackEvent e) {
        LivingEntity victim = e.getEntity();
        DamageSource src = e.getSource();
        if (busy || victim.level().isClientSide || !(victim instanceof Player p) || src.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        Entity a = src.getEntity();
        if (!(a instanceof LivingEntity atk) || atk == p || src.getDirectEntity() != atk || !worn(p, MyfItems.SUNSET_ROSE_CLOAK)) return;
        Item item = MyfItems.SUNSET_ROSE_CLOAK.get();
        if (p.getCooldowns().isOnCooldown(item)) return;
        p.getCooldowns().addCooldown(item, MyfConfig.CLOAK_COOLDOWN.get());
        e.setCanceled(true);
        float yaw = Combat.yawTo(p.position(), atk.position());
        FxEntity.at(p.level(), FxType.RS_SIGIL_BREAK, p.position().add(0, 1.0, 0)).size(0.6f).spawn();
        Fx.arc(p.level(), p.position(), yaw, 2.2f, 160f, 1.0f, false);
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.8f, 1.8f);
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.2f);
        float dmg = (float) (MyfConfig.CLOAK_BASE.get() + MyfConfig.CLOAK_SCALE.get() * attack(p));
        if (dmg > 0) strike(p, atk, p.damageSources().playerAttack(p), dmg);
        int stun = MyfConfig.CLOAK_STUN.get();
        if (stun > 0 && atk.isAlive() && !atk.getType().is(Tags.EntityTypes.BOSSES)) {
            atk.addEffect(new MobEffectInstance(MyfEffects.STUNNED.get(), stun, 0, false, true, true), p);
            FxEntity.at(p.level(), FxType.FT_STARS, atk.position()).follow(atk, 0, atk.getBbHeight() + 0.3, 0).life(stun).spawn();
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent e) {
        SPIKES.clear();
        BELL.clear();
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.level instanceof ServerLevel level) || SPIKES.isEmpty()) return;
        long now = level.getGameTime();
        Iterator<Spike> it = SPIKES.iterator();
        while (it.hasNext()) {
            Spike s = it.next();
            if (s.level != level || now < s.at) continue;
            it.remove();
            LivingEntity v = s.victim;
            if (!v.isAlive() || v.isRemoved() || s.owner.isRemoved()) continue;
            Fx.rockBurst(level, v.position(), 0.7f, 5);
            level.playSound(null, v.getX(), v.getY(), v.getZ(), SoundEvents.BONE_BLOCK_BREAK, SoundSource.PLAYERS, 1.2f, 0.7f);
            strike(s.owner, v, s.owner.damageSources().thorns(s.owner), s.damage);
            if (MyfConfig.FANG_SLOW.get() > 0) v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, MyfConfig.FANG_SLOW.get(), 1), s.owner);
        }
    }
}
