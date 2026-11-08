package com.hp.myfplus.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.entity.EnhancedMineEntity;
import com.hp.myfplus.registry.MyfItems;

import lykrast.meetyourfight.entity.FortunaBombEntity;
import lykrast.meetyourfight.entity.ProjectileBossAbstract;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = Myfplus.MOD_ID)
public final class AccessoryEvents {
    private static final int BELL_RESET = 100;
    private static final Vec3[] CARDINAL = {new Vec3(1, 0, 0), new Vec3(0, 0, 1), new Vec3(-1, 0, 0), new Vec3(0, 0, -1)};
    private static final Map<UUID, long[]> BELL = new HashMap<>();

    private AccessoryEvents() {
    }

    private static boolean worn(LivingEntity e, RegistryObject<Item> item) {
        return CuriosApi.getCuriosInventory(e).map(i -> i.isEquipped(item.get())).orElse(false);
    }

    private static boolean ready(Player p, RegistryObject<Item> item, double chance, int cooldown) {
        if (p.getCooldowns().isOnCooldown(item.get()) || p.getRandom().nextDouble() >= chance) return false;
        if (cooldown > 0) p.getCooldowns().addCooldown(item.get(), cooldown);
        return true;
    }

    private static double attack(Player p) {
        return p.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        LivingEntity victim = e.getEntity();
        if (victim.level().isClientSide) return;
        Entity direct = e.getSource().getDirectEntity();
        if (direct instanceof ProjectileBossAbstract || direct instanceof EnhancedMineEntity || direct instanceof FortunaBombEntity) return;
        if (victim instanceof Player wearer && e.getSource().getEntity() instanceof LivingEntity atk && atk != wearer && direct == atk
                && worn(wearer, MyfItems.SUNSET_ROSE_CLOAK)) cloak(wearer, atk);
        if (!(e.getSource().getEntity() instanceof Player p) || p == victim || !Shots.foe(p, victim)) return;
        if (direct == p && worn(p, MyfItems.FOSSIL_FANG_BRACELET)) fang(p, victim);
        if (worn(p, MyfItems.RESONANT_BELT_BELL)) bell(p, victim, e.getAmount());
        if (worn(p, MyfItems.HOUSE_DIE_RING)) dice(p, victim);
    }

    private static void fang(Player p, LivingEntity victim) {
        if (!ready(p, MyfItems.FOSSIL_FANG_BRACELET, MyfConfig.FANG_CHANCE.get(), MyfConfig.FANG_COOLDOWN.get())) return;
        float dmg = (float) (MyfConfig.FANG_BASE.get() + MyfConfig.FANG_SCALE.get() * attack(p));
        Vec3 at = Shots.feet(victim).add(0, 0.2, 0);
        p.level().addFreshEntity(EnhancedMineEntity.create(p, at, Vec3.ZERO, dmg, MyfConfig.FANG_RADIUS.get().floatValue(), 15));
        p.level().playSound(null, at.x, at.y, at.z, MYFSounds.swampjawBomb.get(), SoundSource.PLAYERS, 1.0f, 1.3f);
    }

    private static void bell(Player p, LivingEntity victim, float amount) {
        long now = victim.level().getGameTime();
        long[] st = BELL.computeIfAbsent(p.getUUID(), k -> new long[2]);
        if (now - st[1] > BELL_RESET) st[0] = 0;
        st[1] = now;
        if (++st[0] < MyfConfig.BELL_HITS.get()) return;
        st[0] = 0;
        float dmg = (float) (amount * MyfConfig.BELL_RATIO.get());
        if (dmg <= 0) return;
        Vec3 c = victim.position().add(0, victim.getBbHeight() * 0.5, 0);
        for (Vec3 d : CARDINAL) {
            ProjectileLineEntity line = Shots.line(p, ProjectileLineEntity.VAR_BELLRINGER, c.subtract(d.scale(4)), d, 8);
            line.getPersistentData().putFloat(Shots.FLAT, dmg);
            p.level().addFreshEntity(line);
        }
        victim.level().playSound(null, c.x, c.y, c.z, SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2f, 1.3f);
        Shots.particles(victim.level(), ParticleTypes.END_ROD, c, 12, 0.6, 0.05);
    }

    private static void dice(Player p, LivingEntity victim) {
        if (!ready(p, MyfItems.HOUSE_DIE_RING, MyfConfig.DIE_CHANCE.get(), MyfConfig.DIE_COOLDOWN.get())) return;
        FortunaBombEntity bomb = new FortunaBombEntity(p.level(), p.getX(), p.getEyeY(), p.getZ(), p);
        bomb.setup(25, 12, victim.getX(), victim.getY() + 0.5, victim.getZ());
        p.level().addFreshEntity(bomb);
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), MYFSounds.dameFortunaShoot.get(), SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void cloak(Player p, LivingEntity atk) {
        if (!ready(p, MyfItems.SUNSET_ROSE_CLOAK, 1.0, MyfConfig.CLOAK_COOLDOWN.get())) return;
        float dmg = (float) (MyfConfig.CLOAK_BASE.get() + MyfConfig.CLOAK_SCALE.get() * attack(p));
        Vec3 eye = p.getEyePosition().subtract(0, 0.3, 0);
        Vec3 aim = atk.position().add(0, atk.getBbHeight() * 0.5, 0).subtract(eye).normalize();
        for (int i = -2; i <= 2; i++) {
            Vec3 d = aim.yRot(i * 12f * Mth.DEG_TO_RAD);
            ProjectileLineEntity line = Shots.line(p, ProjectileLineEntity.VAR_ROSALYNE, eye.add(d), d, 6);
            line.getPersistentData().putFloat(Shots.FLAT, dmg);
            line.getPersistentData().putInt(Shots.STUN, MyfConfig.CLOAK_STUN.get());
            p.level().addFreshEntity(line);
        }
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), MYFSounds.rosalyneSwing.get(), SoundSource.PLAYERS, 1.0f, 1.2f);
        Shots.particles(p.level(), ParticleTypes.CHERRY_LEAVES, eye, 20, 0.8, 0.05);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent e) {
        BELL.clear();
    }
}
