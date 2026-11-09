package com.hp.myfplus.core;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.registry.MyfEffects;
import com.mojang.logging.LogUtils;

import lykrast.meetyourfight.config.MYFConfigValues;
import lykrast.meetyourfight.entity.BellringerEntity;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import lykrast.meetyourfight.entity.ProjectileBossAbstract;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Myfplus.MOD_ID)
public final class CommonEvents {
    private static final String INIT = "myfplus_init";

    private CommonEvents() {
    }

    private static boolean stunned(Entity e) {
        return e instanceof LivingEntity l && l.hasEffect(MyfEffects.STUNNED.get());
    }

    @SubscribeEvent
    public static void onStunnedAttack(LivingAttackEvent e) {
        if (stunned(e.getSource().getEntity()) && e.getSource().getDirectEntity() == e.getSource().getEntity()) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onStunnedSwing(AttackEntityEvent e) {
        if (stunned(e.getEntity())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onStunnedUseItem(PlayerInteractEvent.RightClickItem e) {
        if (stunned(e.getEntity())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onStunnedUseBlock(PlayerInteractEvent.RightClickBlock e) {
        if (stunned(e.getEntity())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onStunnedHitBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (stunned(e.getEntity())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onStunnedUseEntity(PlayerInteractEvent.EntityInteract e) {
        if (stunned(e.getEntity())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onStunnedJump(LivingEvent.LivingJumpEvent e) {
        if (!stunned(e.getEntity())) return;
        Vec3 dm = e.getEntity().getDeltaMovement();
        e.getEntity().setDeltaMovement(dm.x, Math.min(0, dm.y), dm.z);
    }

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent e) {
        if (e.getProjectile().level().isClientSide || !(e.getProjectile() instanceof ProjectileBossAbstract proj)) return;
        if (!(e.getRayTraceResult() instanceof EntityHitResult hit) || !(proj.getOwner() instanceof LivingEntity owner)) return;
        if (owner instanceof Player && !(hit.getEntity() instanceof LivingEntity v && Shots.foe(owner, v)))
            e.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
        else if (owner instanceof EnhancedBoss && hit.getEntity() instanceof EnhancedBoss)
            e.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onHurt(LivingHurtEvent e) {
        if (!(e.getSource().getDirectEntity() instanceof ProjectileBossAbstract proj)) return;
        Entity owner = proj.getOwner();
        CompoundTag d = proj.getPersistentData();
        if (owner instanceof EnhancedBoss boss && owner instanceof LivingEntity le) {
            double mult = d.contains(Shots.MULT) ? d.getDouble(Shots.MULT) : 1.0;
            e.setAmount((float) (le.getAttributeValue(Attributes.ATTACK_DAMAGE) * mult * boss.myfplus$cfg().damage()));
        }
        else if (owner instanceof Player p) {
            float amount = d.contains(Shots.FLAT) ? d.getFloat(Shots.FLAT)
                    : (float) (MyfConfig.DIE_BASE.get() + MyfConfig.DIE_SCALE.get() * p.getAttributeValue(Attributes.ATTACK_DAMAGE));
            e.setAmount(amount);
            int stun = d.getInt(Shots.STUN);
            LivingEntity v = e.getEntity();
            if (stun > 0 && !v.getType().is(Tags.EntityTypes.BOSSES))
                v.addEffect(new MobEffectInstance(MyfEffects.STUNNED.get(), stun, 0, false, true, true), p);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent e) {
        LivingEntity v = e.getEntity();
        if (!(v instanceof EnhancedBoss boss) || !boss.myfplus$lethalPhase2() || boss.myfplus$phase2() || !boss.myfplus$cfg().phase2()) return;
        if (e.getAmount() < v.getHealth() || e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        e.setAmount(0);
        v.setHealth(v.getMaxHealth());
        LogUtils.getLogger().debug("Enhanced boss {} enters phase 2", v.getType().getDescriptionId());
        boss.myfplus$enterPhase2();
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof Mob mob) || !(mob instanceof EnhancedBoss boss)) return;
        if (mob.getPersistentData().getBoolean(INIT)) return;
        mob.getPersistentData().putBoolean(INIT, true);
        AttributeInstance atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            if (mob instanceof BellringerEntity) atk.setBaseValue(BellringerEntity.DMG * MYFConfigValues.BELLRINGER_DMG_MULT);
            else if (mob instanceof DameFortunaEntity) atk.setBaseValue(DameFortunaEntity.DMG * MYFConfigValues.FORTUNA_DMG_MULT);
            if (boss.myfplus$cfg().attack() != 1.0)
                atk.addPermanentModifier(new AttributeModifier("myfplus config attack", boss.myfplus$cfg().attack() - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        AttributeInstance hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null && boss.myfplus$cfg().health() != 1.0) {
            hp.addPermanentModifier(new AttributeModifier("myfplus config health", boss.myfplus$cfg().health() - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            mob.setHealth(mob.getMaxHealth());
        }
    }
}
