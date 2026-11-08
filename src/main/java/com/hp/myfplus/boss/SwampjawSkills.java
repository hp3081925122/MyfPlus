package com.hp.myfplus.boss;

import java.util.ArrayList;
import java.util.List;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Shots;
import com.hp.myfplus.core.SkillRunner;
import com.hp.myfplus.entity.EnhancedMineEntity;
import com.hp.myfplus.entity.boss.EnhancedSwampjawEntity;

import lykrast.meetyourfight.entity.SwampjawEntity;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class SwampjawSkills {
    public static final String REBITE = "rebite", TAIL = "tail";
    private static final double MINE_GRAVITY = 0.04;

    private SwampjawSkills() {
    }

    public static void register(EnhancedSwampjawEntity boss, SkillRunner runner) {
        runner.add(new Rebite(boss)).add(new Tail(boss)).add(new Spit(boss)).add(new Vortex(boss)).add(new Rain(boss));
    }

    private static void mine(EnhancedSwampjawEntity boss, Vec3 pos, Vec3 motion, double mult) {
        boss.level().addFreshEntity(EnhancedMineEntity.create(boss, pos, motion, boss.dmg(mult), 2.6f, 200));
    }

    /** 连环回咬：俯冲命中后掉头，10 tick 预警后再次俯冲。 */
    private static final class Rebite extends BossSkill {
        private final EnhancedSwampjawEntity boss;

        Rebite(EnhancedSwampjawEntity boss) {
            super(REBITE, 0, false);
            this.boss = boss;
        }

        @Override
        public void start(LivingEntity target) {
            boss.behavior(EnhancedSwampjawEntity.SKILL);
            boss.brake();
            boss.playSound(MYFSounds.swampjawCharge.get(), 6.0f, 1.15f);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (boss.behavior() != EnhancedSwampjawEntity.SKILL) return true;
            boss.steerTo(boss.position().add(Shots.toward(target, boss).scale(2)).add(0, 0.6, 0), 0.4);
            if (t % 2 == 0) Shots.particles(boss.level(), ParticleTypes.BUBBLE, boss.mouth(), 6, 0.4, 0.05);
            if (t < 10) return false;
            boss.behavior(EnhancedSwampjawEntity.SWOOP);
            boss.setAnimation(SwampjawEntity.ANIM_SWOOP);
            boss.attackDelay(Math.max(boss.attackDelay(), 60));
            return true;
        }
    }

    /** 甩尾横扫：玩家贴近时原地甩尾，1.0 倍攻击力，击退 0.6。 */
    private static final class Tail extends BossSkill {
        private final EnhancedSwampjawEntity boss;

        Tail(EnhancedSwampjawEntity boss) {
            super(TAIL, 0, false);
            this.boss = boss;
        }

        @Override
        public void start(LivingEntity target) {
            boss.behavior(EnhancedSwampjawEntity.SKILL);
            boss.brake();
            boss.setAnimation(SwampjawEntity.ANIM_SWIPE);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (boss.behavior() != EnhancedSwampjawEntity.SKILL) return true;
            if (t == 10) {
                boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 4.0f, 0.8f);
                Shots.particles(boss.level(), ParticleTypes.SWEEP_ATTACK, boss.position().add(0, 0.8, 0), 6, 1.6, 0);
                Shots.particles(boss.level(), ParticleTypes.SPLASH, boss.position().add(0, 0.8, 0), 30, 2.0, 0.3);
                for (LivingEntity v : boss.level().getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(2.0, 1.0, 2.0), e -> Shots.foe(boss, e))) {
                    if (v.hurt(boss.damageSources().mobAttack(boss), boss.dmg(1.0))) {
                        Vec3 d = Shots.flat(v.position().subtract(boss.position()));
                        v.knockback(0.6, -d.x, -d.z);
                    }
                }
            }
            if (t < 18) return false;
            boss.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
            return true;
        }
    }

    /** 泥沼喷吐：悬停蓄力，向玩家当前位置与预判位置各抛出一颗水雷。 */
    private static final class Spit extends BossSkill {
        private final EnhancedSwampjawEntity boss;

        Spit(EnhancedSwampjawEntity boss) {
            super("spit", 3, false);
            this.boss = boss;
        }

        @Override
        public void start(LivingEntity target) {
            boss.behavior(EnhancedSwampjawEntity.SKILL);
            boss.playSound(MYFSounds.swampjawBomb.get(), 4.0f, 0.8f);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (boss.behavior() != EnhancedSwampjawEntity.SKILL) return true;
            Vec3 hover = target.position().add(Shots.toward(target, boss).scale(7)).add(0, 5, 0);
            boss.steerTo(hover, t < 15 ? 0.6 : 0.2);
            if (t < 15 && t % 2 == 0) Shots.particles(boss.level(), ParticleTypes.SPLASH, boss.mouth(), 10, 0.3, 0.1);
            if (t == 15) {
                Vec3 from = boss.mouth();
                Vec3 now = Shots.feet(target);
                Vec3 lead = now.add(target.getDeltaMovement().multiply(15, 0, 15));
                mine(boss, from, Shots.lob(from, now, 18, MINE_GRAVITY), 0.9);
                mine(boss, from, Shots.lob(from, lead, 22, MINE_GRAVITY), 0.9);
                boss.playSound(SoundEvents.DROWNED_SHOOT, 3.0f, 0.7f);
            }
            return t >= 25;
        }
    }

    /** 泥沼漩涡（二阶段）：绕玩家上空一圈投下 8 颗水雷（留一个缺口），随后俯冲。 */
    private static final class Vortex extends BossSkill {
        private static final int LAP = 48, COUNT = 8;
        private final EnhancedSwampjawEntity boss;
        private Vec3 center = Vec3.ZERO;
        private float angle0;
        private int gap;

        Vortex(EnhancedSwampjawEntity boss) {
            super("vortex", 2, true);
            this.boss = boss;
        }

        @Override
        public void start(LivingEntity target) {
            boss.behavior(EnhancedSwampjawEntity.SKILL);
            center = Shots.feet(target);
            angle0 = (float) Mth.atan2(boss.getZ() - center.z, boss.getX() - center.x);
            gap = boss.getRandom().nextInt(COUNT);
            boss.playSound(MYFSounds.swampjawCharge.get(), 5.0f, 0.8f);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (boss.behavior() != EnhancedSwampjawEntity.SKILL) return true;
            float a = angle0 + Mth.TWO_PI * Math.min(t, LAP) / LAP;
            boss.steerTo(center.add(Shots.dir(a).scale(5)).add(0, 7, 0), 1.3);
            int step = LAP / COUNT;
            if (t > 4 && t <= LAP + 4 && (t - 4) % step == 0) {
                int i = (t - 4) / step - 1;
                if (i != gap) mine(boss, boss.position(), new Vec3(0, -0.1, 0), 0.8);
                else Shots.particles(boss.level(), ParticleTypes.BUBBLE_POP, boss.position(), 10, 0.5, 0.05);
            }
            if (t < LAP + 10) return false;
            boss.behavior(EnhancedSwampjawEntity.SWOOP);
            boss.setAnimation(SwampjawEntity.ANIM_SWOOP);
            boss.playSound(MYFSounds.swampjawCharge.get(), 6.0f, 1.0f);
            boss.attackDelay(Math.max(boss.attackDelay(), 60));
            return true;
        }
    }

    /** 腐水暴雨（二阶段）：两波，每波 3 个落点先冒水花标记，15 tick 后水雷从高处落下。 */
    private static final class Rain extends BossSkill {
        private final EnhancedSwampjawEntity boss;
        private final List<Vec3> marks = new ArrayList<>();

        Rain(EnhancedSwampjawEntity boss) {
            super("rain", 2, true);
            this.boss = boss;
        }

        @Override
        public void start(LivingEntity target) {
            boss.behavior(EnhancedSwampjawEntity.SKILL);
            marks.clear();
            boss.playSound(MYFSounds.swampjawIdle.get(), 5.0f, 0.6f);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (boss.behavior() != EnhancedSwampjawEntity.SKILL) return true;
            boss.steerTo(target.position().add(0, 9, 0), 0.8);
            int w = t % 30;
            if (t < 60 && w == 0) {
                marks.clear();
                marks.add(Shots.feet(target));
                for (int i = 0; i < 2; i++) {
                    double x = target.getX() + (boss.getRandom().nextDouble() - 0.5) * 6;
                    double z = target.getZ() + (boss.getRandom().nextDouble() - 0.5) * 6;
                    marks.add(new Vec3(x, Shots.groundY(boss.level(), x, target.getY() + 1, z) + 0.1, z));
                }
            }
            if (t < 60 && w < 15 && w % 3 == 0) {
                for (Vec3 m : marks) Shots.particles(boss.level(), ParticleTypes.SPLASH, m, 12, 0.5, 0.1);
            }
            if (t < 60 && w == 15) {
                for (Vec3 m : marks) mine(boss, m.add(0, 10, 0), new Vec3(0, -0.3, 0), 0.8);
                boss.playSound(MYFSounds.swampjawBomb.get(), 4.0f, 1.2f);
            }
            return t >= 66;
        }
    }
}
