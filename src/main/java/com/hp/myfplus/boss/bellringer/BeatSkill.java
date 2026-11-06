package com.hp.myfplus.boss.bellringer;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;

import net.minecraft.world.entity.LivingEntity;

public abstract class BeatSkill extends BossSkill {
    protected final BellringerBrain b;
    protected int beat;
    protected int sinceBeat;

    protected BeatSkill(BellringerBrain b, String name, int cooldown) {
        super(name, cooldown);
        this.b = b;
    }

    @Override
    public final void start(LivingEntity target) {
        beat = 0;
        sinceBeat = 0;
        begin(target);
    }

    protected void begin(LivingEntity target) {
    }

    @Override
    public final boolean tick(int t, @Nullable LivingEntity target) {
        sinceBeat++;
        if (b.beatThisTick) {
            beat++;
            sinceBeat = 0;
            if (onBeat(beat, target)) return true;
        }
        return update(t, target) || t > 600;
    }

    protected abstract boolean onBeat(int beat, @Nullable LivingEntity target);

    protected boolean update(int t, @Nullable LivingEntity target) {
        return false;
    }
}
