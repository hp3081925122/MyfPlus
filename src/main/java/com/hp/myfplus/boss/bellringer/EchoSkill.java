package com.hp.myfplus.boss.bellringer;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.entity.EchoMarkEntity;
import com.hp.myfplus.registry.MyfEntities;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class EchoSkill extends BeatSkill {
    public EchoSkill(BellringerBrain b) {
        super(b, "echo", 140);
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist <= 24;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    protected boolean onBeat(int beat, @Nullable LivingEntity target) {
        if (beat > 4 || target == null) return true;
        b.ding();
        place(target.position());
        if (b.phase2) {
            Vec3 dm = target.getDeltaMovement();
            Vec3 h = new Vec3(dm.x, 0, dm.z);
            Vec3 ahead = h.lengthSqr() > 1.0E-4 ? h.normalize().scale(3) : Combat.dirFromYaw(target.getYRot()).scale(3);
            place(target.position().add(ahead));
        }
        return false;
    }

    private void place(Vec3 at) {
        EchoMarkEntity mark = new EchoMarkEntity(MyfEntities.ECHO_MARK.get(), b.boss.level());
        Vec3 p = b.groundAt(at);
        mark.setPos(p.x, p.y, p.z);
        mark.setOwner(b.boss);
        mark.setup(b.interval());
        mark.damage = Combat.damage(b.boss, b.cfg, 0.8);
        b.boss.level().addFreshEntity(mark);
        b.boss.level().playSound(null, p.x, p.y, p.z, SoundEvents.AMETHYST_BLOCK_CHIME, b.boss.getSoundSource(), 1.5f, 0.6f);
    }
}
