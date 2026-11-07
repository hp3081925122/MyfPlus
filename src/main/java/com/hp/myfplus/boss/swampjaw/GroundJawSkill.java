package com.hp.myfplus.boss.swampjaw;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.entity.FossilJawEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** 地颌：玩家脚下前后两排獠牙印记，20 tick 后獠牙破土并向中线咬合。向两侧跑最容易躲开。P2 追加一次转 90° 的咬合。 */
public class GroundJawSkill extends BossSkill {
    private static final int[] CAST_AT = {0, 16};
    private final SwampjawBrain b;
    private final FossilJawEntity[] jaws = new FossilJawEntity[CAST_AT.length];
    private int casts;
    private int st;

    public GroundJawSkill(SwampjawBrain b) {
        super("ground_jaw", 100);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist <= 22 && b.grounded(target);
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    public void start(LivingEntity target) {
        casts = b.phase2 ? 2 : 1;
        st = 0;
        jaws[0] = null;
        jaws[1] = null;
        b.setSkill(SwampjawBrain.SKILL_JAW);
        b.boss.playSound(SoundEvents.RAVAGER_ROAR, 1.5f, 1.6f);
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        Vec3 anchor = target != null ? target.position() : fish.position();
        b.flight.steer(new Vec3(fish.getX(), anchor.y + 6, fish.getZ()), 0.4);
        b.flight.face(anchor);
        int end = 0;
        for (int i = 0; i < casts; i++) {
            int at = CAST_AT[i];
            if (st == at + 1 && target != null) cast(i, target, Combat.yawTo(fish.position(), target.position()) + (i == 1 ? 90f : 0f));
            FossilJawEntity j = jaws[i];
            if (j != null) {
                int age = st - at - 1;
                if (age == FossilJawEntity.ROW_WARN) {
                    fish.level().playSound(null, j.getX(), j.getY(), j.getZ(), SoundEvents.BONE_BLOCK_BREAK, SoundSource.HOSTILE, 2.0f, 0.5f);
                    Fx.rockBurst(fish.level(), j.position(), 2.5f, 8);
                }
                if (age == FossilJawEntity.ROW_CLAMP) clamp(j);
            }
            end = at + 1 + FossilJawEntity.ROW_CLAMP + 10;
        }
        return st >= end;
    }

    private void cast(int i, LivingEntity target, float yaw) {
        SwampjawEntity fish = b.boss;
        Vec3 c = b.groundAt(target.getX(), target.getZ(), target.getY() + 0.5);
        FossilJawEntity j = new FossilJawEntity(MyfEntities.FOSSIL_JAW.get(), fish.level());
        j.setPos(c.x, c.y, c.z);
        j.setOwner(fish);
        j.mode = FossilJawEntity.ROWS;
        j.yaw = yaw;
        j.life = FossilJawEntity.ROW_LIFE;
        fish.level().addFreshEntity(j);
        jaws[i] = j;
        fish.level().playSound(null, c.x, c.y, c.z, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 2.0f, 0.4f);
    }

    private void clamp(FossilJawEntity j) {
        SwampjawEntity fish = b.boss;
        Vec3 c = j.position();
        for (LivingEntity v : Combat.foes(fish, new AABB(c, c).inflate(6, 3, 6),
                e -> FossilJawEntity.inRows(c, j.yaw, e.position(), 0.4, 0.8) && Combat.inGroundBand(fish.level(), e, c.y, 3))) {
            if (Combat.hit(fish, b.cfg, v, 1.1, 0)) Combat.push(v, new Vec3(0, 0.35, 0));
        }
        Fx.shake(fish.level(), c, 0.6f);
        Fx.rockBurst(fish.level(), c, 1.4f, 10);
        fish.level().playSound(null, c.x, c.y, c.z, SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.HOSTILE, 2.5f, 0.5f);
    }

    @Override
    public void stop() {
        jaws[0] = null;
        jaws[1] = null;
        b.flight.face(null);
    }
}
