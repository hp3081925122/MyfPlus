package com.hp.myfplus.boss.swampjaw;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** P2 转换「古颌苏醒」：50% 血时升空咆哮，期间免伤；爆发时整圈方块浪、冲击波、第二幕横幅与沼泽雾。 */
public class AwakenSkill extends BossSkill {
    private static final int BURST = 24;
    private final SwampjawBrain b;
    private int st;

    public AwakenSkill(SwampjawBrain b) {
        super("awaken", -1);
        this.b = b;
    }

    @Override
    public void start(LivingEntity target) {
        st = 0;
        b.guarded = true;
        b.setSkill(SwampjawBrain.SKILL_JAW);
        b.boss.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 2.0f, 0.6f);
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        Vec3 c = b.center != null ? b.center : fish.position();
        b.flight.steer(new Vec3(c.x, c.y + 8, c.z), 0.6);
        b.flight.face(target != null ? target.position() : null);
        if (st % 6 == 0 && st < BURST) Fx.shake(fish.level(), fish.position(), 0.25f);
        if (st == BURST) burst();
        return st >= BURST + 16;
    }

    private void burst() {
        SwampjawEntity fish = b.boss;
        b.phase2 = true;
        b.setPhase(2);
        Fx.banner(fish.level(), fish.position(), Fx.BOSS_SWAMPJAW, 2);
        FxEntity.at(fish.level(), FxType.SJ_ROAR, fish.position().add(0, 0.6, 0)).spawn();
        Fx.shake(fish.level(), fish.position(), 1.1f);
        Vec3 g = b.groundAt(fish.getX(), fish.getZ(), fish.getY());
        Fx.rockRing(fish.level(), g, 10f, 0.5f);
        Fx.rockBurst(fish.level(), g, 3f, 18);
        fish.playSound(SoundEvents.RAVAGER_ROAR, 3.0f, 0.5f);
        fish.playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 0.5f);
    }

    @Override
    public void stop() {
        b.guarded = false;
        b.flight.face(null);
    }
}
