package com.hp.myfplus.boss.rosalyne;

import javax.annotation.Nullable;

import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.RosalyneEntity;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class CorollaSkill extends RosaSkill {
    private static final int DURATION = 80;
    private Vec3 at = Vec3.ZERO;

    public CorollaSkill(RosalyneBrain b) {
        super(b, "corolla", -1);
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
        if (t == 1) {
            boss.setAnimation(RosalyneEntity.ANIM_SUMMONING);
            boss.playSound(MYFSounds.rosalyneCrack.get(), 2.0f, 1.2f);
            boss.playSound(SoundEvents.BEACON_POWER_SELECT, 2.0f, 0.6f);
            Fx.rockRing(boss.level(), b.groundAt(at), 6f, 0.4f);
        }
        if (t >= 20 && t < 20 + 8 * 6 && (t - 20) % 6 == 0) {
            int slot = (t - 20) / 6;
            b.addPetal(slot);
            boss.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.5f, 0.8f + slot * 0.1f);
        }
        if (t == DURATION) {
            FxEntity.at(boss.level(), FxType.RS_PETAL_BURST, at.add(0, 1.2, 0)).size(2.2f).spawn();
            Fx.shake(boss.level(), at, 0.6f);
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        b.invulnerable = false;
        if (b.act == 1) b.director.force(b.arena);
    }
}
