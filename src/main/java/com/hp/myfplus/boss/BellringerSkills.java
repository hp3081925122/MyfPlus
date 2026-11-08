package com.hp.myfplus.boss;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Shots;
import com.hp.myfplus.core.SkillRunner;
import com.hp.myfplus.entity.boss.EnhancedBellringerEntity;

import lykrast.meetyourfight.entity.ProjectileLineEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class BellringerSkills {
    private static final int V = ProjectileLineEntity.VAR_BELLRINGER;
    private static final Vec3 DOWN = new Vec3(0, -1, 0);
    private static final Vec3[] CARDINAL = {new Vec3(1, 0, 0), new Vec3(0, 0, 1), new Vec3(-1, 0, 0), new Vec3(0, 0, -1)};

    private BellringerSkills() {
    }

    public static void register(EnhancedBellringerEntity boss, SkillRunner runner) {
        runner.add(new Cross(boss)).add(new Ring(boss)).add(new Echo(boss)).add(new Pendulum(boss)).add(new Carillon(boss));
    }

    private static Vec3 cell(LivingEntity target) {
        BlockPos p = target.blockPosition();
        return new Vec3(p.getX() + 0.5, Shots.feet(target).y, p.getZ() + 0.5);
    }

    /** 十字钟鸣：四个正方向各一排 5 道幽灵线，同时射向玩家。 */
    private static final class Cross extends BossSkill {
        private final EnhancedBellringerEntity boss;

        Cross(EnhancedBellringerEntity boss) {
            super("cross", 3, false);
            this.boss = boss;
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (t == 0) {
                boss.ding();
                Vec3 c = cell(target);
                for (Vec3 d : CARDINAL) {
                    Vec3 side = new Vec3(-d.z, 0, d.x);
                    for (int i = -2; i <= 2; i++) Shots.fireLine(boss, V, 0.8, c.subtract(d.scale(7)).add(side.scale(i)), d, 20);
                }
            }
            return t >= 30;
        }
    }

    /** 钟环：半径 6 的圆上 12 道幽灵线顺时针依次射向圆心，留一个缺口。 */
    private static final class Ring extends BossSkill {
        private static final int COUNT = 12;
        private final EnhancedBellringerEntity boss;

        Ring(EnhancedBellringerEntity boss) {
            super("ring", 3, false);
            this.boss = boss;
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (t == 0) {
                boss.ding();
                boss.playSound(SoundEvents.BELL_RESONATE, 1.5f, 1.4f);
                Vec3 c = cell(target);
                int gap = boss.getRandom().nextInt(COUNT);
                float a0 = boss.getRandom().nextFloat() * Mth.TWO_PI;
                for (int k = 0; k < COUNT; k++) {
                    if (k == gap) continue;
                    Vec3 d = Shots.dir(a0 + k * Mth.TWO_PI / COUNT);
                    Shots.fireLine(boss, V, 0.8, c.add(d.scale(6)), d.reverse(), 20 + k * 2);
                }
            }
            return t >= 50;
        }
    }

    /** 回响追钟：连敲三下，每下在玩家当时位置落下十字形天降线。 */
    private static final class Echo extends BossSkill {
        private final EnhancedBellringerEntity boss;

        Echo(EnhancedBellringerEntity boss) {
            super("echo", 2, false);
            this.boss = boss;
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (t % 8 == 0 && t <= 16) {
                boss.ding();
                Vec3 c = cell(target).add(0, 7, 0);
                Shots.fireLine(boss, V, 1.0, c, DOWN, 12);
                for (Vec3 d : CARDINAL) Shots.fireLine(boss, V, 1.0, c.add(d), DOWN, 12);
            }
            return t >= 36;
        }
    }

    /** 钟摆（二阶段）：一面 11 道宽的幽灵线墙从玩家一侧扫过，再反向扫回，每遍留一个缺口。 */
    private static final class Pendulum extends BossSkill {
        private final EnhancedBellringerEntity boss;
        private Vec3 forward = Vec3.ZERO;

        Pendulum(EnhancedBellringerEntity boss) {
            super("pendulum", 3, true);
            this.boss = boss;
        }

        @Override
        public void start(LivingEntity target) {
            forward = Shots.toward(boss, target);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (t == 0 || t == 25) {
                boss.ding();
                Vec3 c = cell(target);
                Vec3 side = new Vec3(-forward.z, 0, forward.x).scale(t == 0 ? 1 : -1);
                int gap = boss.getRandom().nextInt(9) - 4;
                for (int i = -5; i <= 5; i++) {
                    if (i == gap) continue;
                    Shots.fireLine(boss, V, 0.9, c.add(forward.scale(i)).subtract(side.scale(7)), side, 20);
                }
            }
            return t >= 50;
        }
    }

    /** 万钟齐鸣（二阶段）：玩家周围 7×7 棋盘格天降线，先落一种颜色的格子，20 tick 后落另一种。 */
    private static final class Carillon extends BossSkill {
        private final EnhancedBellringerEntity boss;
        private Vec3 center = Vec3.ZERO;

        Carillon(EnhancedBellringerEntity boss) {
            super("carillon", 2, true);
            this.boss = boss;
        }

        @Override
        public void start(LivingEntity target) {
            center = cell(target);
            boss.playSound(SoundEvents.BELL_RESONATE, 3f, 0.8f);
            Shots.particles(boss.level(), ParticleTypes.SOUL, boss.position().add(0, 1, 0), 30, 1.0, 0.05);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (t == 0 || t == 20) {
                boss.ding();
                int parity = t == 0 ? 0 : 1;
                for (int x = -3; x <= 3; x++) {
                    for (int z = -3; z <= 3; z++) {
                        if (((x + z) & 1) != parity) continue;
                        Shots.fireLine(boss, V, 1.0, center.add(x, 7, z), DOWN, 20);
                    }
                }
            }
            return t >= 45;
        }
    }
}
