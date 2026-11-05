package com.hp.myfplus.boss.swampjaw;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.entity.BoneHookEntity;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HookSkill extends BossSkill {
    private static final int WINDUP = 12;
    private final SwampjawBrain b;
    @Nullable
    private BoneHookEntity hook;
    private int stage;
    private int st;

    public HookSkill(SwampjawBrain b) {
        super("hook", 140);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist >= 6 && dist <= 16 && b.boss.hasLineOfSight(target);
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    public void start(LivingEntity target) {
        stage = 0;
        st = 0;
        hook = null;
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        switch (stage) {
            case 0 -> {
                b.flight.brake();
                if (target == null) return true;
                b.flight.face(target.getEyePosition());
                b.setSkill(SwampjawBrain.SKILL_SPIT);
                if (t == 1) FxEntity.at(fish.level(), FxType.SJ_HOOK_GLINT, SwampjawBrain.mouth(fish)).life(WINDUP).spawn();
                if (t >= WINDUP) {
                    b.setSkill(0);
                    Vec3 m = SwampjawBrain.mouth(fish);
                    Vec3 aim = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ()).subtract(m).normalize();
                    BoneHookEntity h = new BoneHookEntity(MyfEntities.BONE_HOOK.get(), fish.level());
                    h.setPos(m.x, m.y, m.z);
                    h.setOwner(fish);
                    h.damage = Combat.damage(fish, b.cfg, 0.4);
                    h.setDeltaMovement(aim.scale(0.9));
                    fish.level().addFreshEntity(h);
                    hook = h;
                    fish.playSound(SoundEvents.FISHING_BOBBER_THROW, 2.0f, 0.5f);
                    stage = 1;
                    st = 0;
                }
            }
            case 1 -> {
                b.flight.brake();
                if (hook == null || hook.isRemoved() || st > 40) return true;
                if (hook.blocked) {
                    snap();
                    stage = 3;
                    st = 0;
                    fish.setAnimation(SwampjawEntity.ANIM_STUN);
                    return false;
                }
                if (hook.missed) {
                    snap();
                    return true;
                }
                if (hook.hooked != null) {
                    stage = 2;
                    st = 0;
                    fish.playSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 2.0f, 0.6f);
                }
            }
            case 2 -> {
                LivingEntity v = hook != null ? hook.hooked : null;
                if (v == null || !v.isAlive() || hook.isRemoved()) return true;
                if (hook.jumps >= 2 || !fish.hasLineOfSight(v)) {
                    snap();
                    return true;
                }
                Vec3 m = SwampjawBrain.mouth(fish);
                double dx = m.x - v.getX();
                double dz = m.z - v.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0.1) Combat.setHorizontalVelocity(v, dx / len * 0.5, dz / len * 0.5);
                b.flight.steer(new Vec3(fish.getX(), v.getY() + 1.2, fish.getZ()), 0.3);
                b.flight.face(v.getEyePosition());
                if (fish.distanceTo(v) < 3.0 || st >= 30) {
                    if (fish.distanceTo(v) < 4.5) Combat.hit(fish, b.cfg, v, 0.8, 0.4);
                    fish.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 1.5f, 0.7f);
                    hook.discard();
                    return true;
                }
            }
            case 3 -> {
                b.flight.brake();
                if (st >= 20) return true;
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    private void snap() {
        if (hook == null) return;
        SwampjawEntity fish = b.boss;
        Vec3 mid = hook.position().add(SwampjawBrain.mouth(fish)).scale(0.5);
        FxEntity.at(fish.level(), FxType.SJ_VINE_SNAP, mid).spawn();
        fish.playSound(SoundEvents.VINE_BREAK, 2.0f, 0.8f);
        hook.discard();
    }

    @Override
    public void stop() {
        if (hook != null && !hook.isRemoved()) hook.discard();
        hook = null;
        b.setSkill(0);
        b.boss.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
        b.flight.face(null);
    }
}
