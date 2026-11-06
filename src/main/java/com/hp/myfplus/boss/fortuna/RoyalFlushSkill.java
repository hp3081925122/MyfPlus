package com.hp.myfplus.boss.fortuna;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RoyalFlushSkill extends FortunaSkill {
    private static final float RADIUS = 7f;
    private static final int FIRST = 12;
    private static final int GAP = 8;
    private static final int FALL = 12;
    private static final int STAR = FIRST + GAP * 4 + FALL + 4;
    private static final int BLOW = STAR + 26;
    private Vec3 center = Vec3.ZERO;
    private final List<Vec3> points = new ArrayList<>();
    private final List<FxEntity> cards = new ArrayList<>();

    public RoyalFlushSkill(FortunaBrain b) {
        super(b, "royal_flush", 500, false);
    }

    @Override
    protected boolean usable(LivingEntity target, double dist) {
        return b.act() >= 3 && dist <= 20;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 4;
    }

    @Override
    public void start(LivingEntity target) {
        center = b.groundAt(target.position());
        points.clear();
        cards.clear();
        double base = b.boss.getRandom().nextDouble() * Math.PI * 2;
        for (int i = 0; i < 5; i++) {
            double a = base + i * Math.PI * 2 / 5;
            points.add(b.groundAt(center.add(Math.cos(a) * RADIUS, 0, Math.sin(a) * RADIUS)));
        }
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        DameFortunaEntity boss = b.boss;
        b.hover(center.x, center.y + 7, center.z, 0.6);
        if (t == 1) {
            boss.setAnimation(DameFortunaEntity.ANIM_SNAP_PRE);
            boss.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0f, 1.3f);
        }
        if (t == 8) {
            boss.setAnimation(DameFortunaEntity.ANIM_SNAP_POST);
            boss.playSound(SoundEvents.NOTE_BLOCK_SNARE.get(), 2.0f, 1.4f);
        }
        for (int i = 0; i < 5; i++) {
            int warn = FIRST + i * GAP;
            if (t == warn) FxEntity.at(boss.level(), FxType.WARN_CIRCLE, points.get(i)).size(1.8f).life(FALL).palette(2).spawn();
            if (t == warn) cards.add(FxEntity.at(boss.level(), FxType.FT_CARD, points.get(i)).count(i).life(BLOW - warn + 6).spawn());
            if (t == warn + 8) land(i);
        }
        if (t == STAR) {
            boss.setAnimation(DameFortunaEntity.ANIM_SPIN_POSE);
            boss.playSound(SoundEvents.BELL_RESONATE, 2.5f, 1.2f);
            for (int i = 0; i < 5; i++) {
                Vec3 a = points.get(i), c = points.get((i + 2) % 5);
                float yaw = Combat.yawTo(a, c);
                FxEntity.at(boss.level(), FxType.WARN_LINE, a).yaw(yaw).line((float) Combat.horizontalDist(a, c), 1.6f).life(BLOW - STAR).palette(2).spawn();
            }
        }
        if (t == BLOW) blow();
        return t >= BLOW + 14;
    }

    private void land(int i) {
        DameFortunaEntity boss = b.boss;
        Vec3 p = points.get(i);
        boss.level().playSound(null, p.x, p.y, p.z, SoundEvents.ANVIL_LAND, boss.getSoundSource(), 1.2f, 1.3f + i * 0.1f);
        Fx.rockBurst(boss.level(), p, 1.2f, 6);
        Fx.shake(boss.level(), p, 0.35f);
        AABB box = new AABB(p, p).inflate(1.8, 2, 1.8);
        for (LivingEntity v : Combat.foes(boss, box, e -> Combat.horizontalDist(e.position(), p) <= 1.8)) Combat.hit(boss, b.cfg, v, 1.0, 0.5);
    }

    private void blow() {
        DameFortunaEntity boss = b.boss;
        boss.setAnimation(DameFortunaEntity.ANIM_CHIPS_LAUNCH);
        boss.playSound(SoundEvents.GENERIC_EXPLODE, 3.0f, 0.7f);
        boss.playSound(SoundEvents.PLAYER_LEVELUP, 2.0f, 0.5f);
        Fx.shake(boss.level(), center, 1.3f);
        for (int i = 0; i < 5; i++) {
            Vec3 a = points.get(i), c = points.get((i + 2) % 5);
            Fx.rockLine(boss.level(), a, Combat.yawTo(a, c), (float) Combat.horizontalDist(a, c), 1.8f, 1.2f);
            FxEntity.at(boss.level(), FxType.FT_CARD_BURST, a.add(0, 1.5, 0)).spawn();
        }
        for (FxEntity c : cards) c.discard();
        AABB box = new AABB(center, center).inflate(RADIUS + 1, 3, RADIUS + 1);
        for (LivingEntity v : Combat.foes(boss, box)) {
            if (nearEdge(v.position())) Combat.hit(boss, b.cfg, v, 1.4, 0.6);
        }
    }

    private boolean nearEdge(Vec3 p) {
        for (int i = 0; i < 5; i++) {
            Vec3 a = points.get(i), c = points.get((i + 2) % 5);
            double dx = c.x - a.x, dz = c.z - a.z;
            double len2 = dx * dx + dz * dz;
            double k = Math.max(0, Math.min(1, ((p.x - a.x) * dx + (p.z - a.z) * dz) / len2));
            double qx = a.x + dx * k - p.x, qz = a.z + dz * k - p.z;
            if (qx * qx + qz * qz <= 1.0) return true;
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        for (FxEntity c : cards) if (!c.isRemoved()) c.discard();
        cards.clear();
    }
}
