package com.hp.myfplus.boss.rosalyne;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class StanceSkill extends RosaSkill {
    private static final int WINDUP = 6;
    private final Set<Integer> hit = new HashSet<>();
    @Nullable
    private FxEntity sigil;
    private int window;

    public StanceSkill(RosalyneBrain b) {
        super(b, "stance", 240);
    }

    @Override
    protected boolean usable(LivingEntity target, double dist) {
        return dist <= 8;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return b.act == 1 ? 1 : 2;
    }

    @Override
    protected void begin(LivingEntity target) {
        hit.clear();
        sigil = null;
        b.counterMelee = null;
        b.counterRanged = null;
        int w = MyfConfig.PARRY_WINDOW.get();
        window = b.act >= 3 ? w * 3 / 4 : w;
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        RosalyneEntity boss = b.boss;
        Vec3 here = boss.position();
        switch (stage) {
            case 0 -> {
                b.hover(here.x, here.y, here.z, 0.05);
                if (target != null) b.face(Combat.yawTo(here, target.position()));
                if (st == 1) boss.setAnimation(RosalyneEntity.ANIM_ARM_IN_UP);
                if (st >= WINDUP) {
                    b.parrying = true;
                    sigil = FxEntity.at(boss.level(), FxType.RS_SIGIL, here).follow(boss, 0, 1.2, 0).life(window).spawn();
                    boss.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2.0f, 0.7f);
                    next();
                }
            }
            case 1 -> {
                b.hover(here.x, here.y, here.z, 0.05);
                if (target != null) b.face(Combat.yawTo(here, target.position()));
                if (b.counterMelee != null) {
                    endParry();
                    boss.playSound(SoundEvents.ANVIL_PLACE, 1.5f, 1.8f);
                    stage = 2;
                    st = 0;
                }
                else if (b.counterRanged != null) {
                    endParry();
                    boss.playSound(SoundEvents.ANVIL_PLACE, 1.5f, 1.8f);
                    boss.setAnimation(RosalyneEntity.ANIM_ARM_OUT_UP);
                    b.swingSound();
                    stage = 4;
                    st = 0;
                }
                else if (st >= window) {
                    endParry();
                    FxEntity.at(boss.level(), FxType.RS_SIGIL_BREAK, here.add(0, 1.2, 0)).spawn();
                    boss.playSound(SoundEvents.GLASS_BREAK, 2.0f, 0.7f);
                    boss.setAnimation(RosalyneEntity.ANIM_NEUTRAL);
                    b.vulnerable = 30;
                    stage = 3;
                    st = 0;
                }
            }
            case 2 -> {
                LivingEntity foe = b.counterMelee;
                if (foe == null || !foe.isAlive()) return true;
                if (st == 6) {
                    FxEntity.at(boss.level(), FxType.RS_GHOST, boss.position()).yaw(boss.yBodyRot).count(boss.getId()).spawn();
                    Vec3 back = Combat.dirFromYaw(foe.getYRot()).scale(-2.2);
                    b.blink(new Vec3(foe.getX() + back.x, foe.getY() + 0.1, foe.getZ() + back.z));
                    b.face(Combat.yawTo(boss.position(), foe.position()));
                    boss.setAnimation(RosalyneEntity.ANIM_ARM_OUT_DN);
                }
                if (st > 6) b.hover(boss.getX(), boss.getY(), boss.getZ(), 0.05);
                if (st == 10) {
                    float yaw = Combat.yawTo(boss.position(), foe.position());
                    b.face(yaw);
                    boss.setAnimation(RosalyneEntity.ANIM_ARM_IN_UP);
                    Fx.arc(boss.level(), boss.position(), yaw, 3.2f, 200f, 1.1f, false);
                    b.swingSound();
                    b.hitSector(boss.position(), yaw, 3.5f, 100f, 1.2, 0.6, hit);
                    Fx.shake(boss.level(), boss.position(), 0.5f);
                }
                if (st >= 20) return true;
            }
            case 3 -> {
                b.hover(here.x, here.y - 0.02, here.z, 0.05);
                if (st >= 30) return true;
            }
            case 4 -> {
                b.hover(here.x, here.y, here.z, 0.05);
                if (st >= 10) return true;
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    private void endParry() {
        b.parrying = false;
        if (sigil != null) sigil.discard();
        sigil = null;
    }

    @Override
    public void stop() {
        super.stop();
        endParry();
    }
}
