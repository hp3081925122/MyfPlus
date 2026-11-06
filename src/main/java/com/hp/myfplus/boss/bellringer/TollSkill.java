package com.hp.myfplus.boss.bellringer;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class TollSkill extends BeatSkill {
    private Vec3 anchor = Vec3.ZERO;
    private float[] gaps = new float[0];

    public TollSkill(BellringerBrain b) {
        super(b, "toll", 80);
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist <= 16;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 4;
    }

    @Override
    protected void begin(LivingEntity target) {
        anchor = b.boss.position();
        gaps = new float[0];
    }

    @Override
    protected boolean onBeat(int beat, @Nullable LivingEntity target) {
        if (beat > 3) return true;
        Vec3 c = b.groundAt(b.boss.position());
        b.ding();
        Fx.shake(b.boss.level(), c, 0.15f);
        FxEntity.at(b.boss.level(), FxType.BR_IMPACT, c).size(3f).spawn();
        boolean tall = b.phase2 && beat == 2;
        b.wave(c, 0.8f, 18f, 0.35f, tall, tall ? gaps : new float[0], tall ? 0.8 : 0.6);
        if (b.phase2 && beat == 1) {
            float base = target != null ? Combat.yawTo(c, target.position()) : b.boss.getRandom().nextFloat() * 360;
            gaps = new float[]{base + 60, base + 180, base + 300};
            for (float g : gaps) {
                Vec3 d = Combat.dirFromYaw(g);
                for (float r : new float[]{2.5f, 4f, 5.5f}) {
                    Vec3 p = b.groundAt(c.add(d.scale(r)));
                    FxEntity.at(b.boss.level(), FxType.BR_GAP_MARK, p).life(b.interval() * 2).spawn();
                }
            }
        }
        return false;
    }

    @Override
    protected boolean update(int t, @Nullable LivingEntity target) {
        b.hover(anchor, 0.6);
        return false;
    }
}
