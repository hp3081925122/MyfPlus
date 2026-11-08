package com.hp.myfplus.boss.rosalyne;

import javax.annotation.Nullable;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.RosalyneEntity;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ArenaSkill extends RosaSkill {
    private static final int DURATION = 60;
    private Vec3 at = Vec3.ZERO;

    public ArenaSkill(RosalyneBrain b) {
        super(b, "arena", -1);
    }

    @Override
    protected void begin(LivingEntity target) {
        b.invulnerable = true;
        at = b.boss.position();
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        RosalyneEntity boss = b.boss;
        b.hover(at.x, at.y, at.z, 0.2);
        if (t == 1) boss.setAnimation(RosalyneEntity.ANIM_MADDENING);
        if (t % 20 == 10) boss.playSound(MYFSounds.rosalyneCrack.get(), 2.0f, 1.0f);
        if (t == 40) {
            Vec3 mid = target != null ? at.add(target.position()).scale(0.5) : at;
            Vec3 c = b.groundAt(mid);
            float r = MyfConfig.DUEL_RADIUS.get();
            b.spawnRing(c, r);
            Fx.rockRing(boss.level(), c, r + 1, 0.6f);
            Fx.shake(boss.level(), c, 1.0f);
            boss.playSound(SoundEvents.ROOTED_DIRT_BREAK, 3.0f, 0.5f);
            Fx.banner(boss.level(), c, Fx.BOSS_ROSALYNE, 2);
        }
        if (t == DURATION) {
            b.act = 2;
            b.invulnerable = false;
            b.setPhase(2);
            boss.setPhase(RosalyneEntity.PHASE_3);
            FxEntity.at(boss.level(), FxType.RS_PETAL_BURST, at.add(0, 1.2, 0)).size(3f).spawn();
            Fx.rockBurst(boss.level(), b.groundAt(at), 2f, 14);
            Fx.shake(boss.level(), at, 0.8f);
            boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 0.8f);
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        b.invulnerable = false;
        if (b.act >= 2) return;
        if (b.ring == null) b.spawnRing(b.groundAt(at), MyfConfig.DUEL_RADIUS.get());
        b.act = 2;
        b.setPhase(2);
        b.boss.setPhase(RosalyneEntity.PHASE_3);
    }
}
