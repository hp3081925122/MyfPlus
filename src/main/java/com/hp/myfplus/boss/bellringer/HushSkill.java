package com.hp.myfplus.boss.bellringer;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.BellringerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HushSkill extends BeatSkill {
    private static final int LURK = 100;
    private static final float CONE = 5f;
    private int stage;
    private int st;
    private double angle;
    private float blastYaw;

    public HushSkill(BellringerBrain b) {
        super(b, "hush", 360);
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist <= 20;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return b.phase2 ? 3 : 2;
    }

    @Override
    protected void begin(LivingEntity target) {
        stage = 0;
        st = 0;
        angle = Math.atan2(b.boss.getZ() - target.getZ(), b.boss.getX() - target.getX());
    }

    @Override
    protected boolean onBeat(int beat, @Nullable LivingEntity target) {
        if (stage == 0) {
            b.boss.playSound(SoundEvents.BELL_RESONATE, 2.0f, 0.5f);
            b.boss.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 2.0f, 0.6f);
            FxEntity.at(b.boss.level(), FxType.BR_ECHO_BREAK, b.boss.position()).size(1.6f).spawn();
            b.hidden = true;
            b.revealRequested = false;
            b.beatPaused = true;
            b.setSkill(BellringerBrain.SKILL_HIDDEN);
            stage = 1;
            st = 0;
        }
        return false;
    }

    @Override
    protected boolean update(int t, @Nullable LivingEntity target) {
        BellringerEntity boss = b.boss;
        st++;
        switch (stage) {
            case 1 -> {
                if (target == null) return true;
                if (b.revealRequested) {
                    reveal();
                    boss.playSound(SoundEvents.SHIELD_BREAK, 2.0f, 0.6f);
                    Fx.shake(boss.level(), boss.position(), 0.3f);
                    stage = 3;
                    st = 0;
                    return false;
                }
                angle += 0.045;
                double r = 8 + 2 * Math.sin(st * 0.07);
                Vec3 p = target.position().add(Math.cos(angle) * r, 1.2, Math.sin(angle) * r);
                b.hover(p, 0.6);
                if (st % 20 == 10) FxEntity.at(boss.level(), FxType.BR_EYES, boss.getEyePosition()).spawn();
                if (st >= LURK) {
                    Vec3 back = Combat.dirFromYaw(target.getYRot()).scale(-3);
                    Vec3 at = target.position().add(back.x, 0.3, back.z);
                    boss.teleportTo(at.x, at.y, at.z);
                    reveal();
                    blastYaw = Combat.yawTo(at, target.position());
                    boss.setYRot(blastYaw);
                    boss.yBodyRot = blastYaw;
                    boss.yHeadRot = blastYaw;
                    b.forceBeat();
                    Vec3 g = b.groundAt(at);
                    FxEntity.at(boss.level(), FxType.WARN_SECTOR, g).yaw(blastYaw).size(CONE).line(CONE, 90f).life(10).palette(1).spawn();
                    boss.playSound(SoundEvents.BELL_BLOCK, 2.5f, 0.7f);
                    stage = 2;
                    st = 0;
                }
            }
            case 2 -> {
                b.hover(boss.position(), 0.1);
                if (st == 10) {
                    b.ding();
                    Vec3 o = boss.position();
                    Vec3 g = b.groundAt(o);
                    FxEntity.at(boss.level(), FxType.BR_IMPACT, g.add(Combat.dirFromYaw(blastYaw).scale(2))).size(CONE * 1.6f).spawn();
                    Fx.rockLine(boss.level(), g, blastYaw, CONE, 3f, 0.9f);
                    Fx.shake(boss.level(), o, 0.7f);
                    for (LivingEntity v : Combat.foes(boss, boss.getBoundingBox().inflate(CONE, 2, CONE))) {
                        if (Combat.horizontalDist(v.position(), o) > CONE) continue;
                        if (Math.abs(Mth.wrapDegrees(Combat.yawTo(o, v.position()) - blastYaw)) > 45) continue;
                        Combat.hit(boss, b.cfg, v, 1.0, 0.8);
                    }
                }
                if (st >= 18) return true;
            }
            case 3 -> {
                b.hover(boss.position(), 0.05);
                if (st >= 30) return true;
            }
            default -> {
            }
        }
        return false;
    }

    private void reveal() {
        b.hidden = false;
        b.revealRequested = false;
        b.beatPaused = false;
        b.setSkill(0);
        FxEntity.at(b.boss.level(), FxType.BR_ECHO_BREAK, b.boss.position()).size(1.6f).spawn();
    }

    @Override
    public void stop() {
        if (b.hidden) reveal();
        b.beatPaused = false;
    }
}
