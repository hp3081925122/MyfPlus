package com.hp.myfplus.boss;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Shots;
import com.hp.myfplus.core.SkillRunner;
import com.hp.myfplus.entity.boss.EnhancedFortunaEntity;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import lykrast.meetyourfight.entity.FortunaBombEntity;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class FortunaSkills {
    private FortunaSkills() {
    }

    public static void register(EnhancedFortunaEntity boss, SkillRunner runner) {
        runner.add(new Pincer(boss)).add(new ChipRain(boss)).add(new RaiseDash(boss)).add(new Flush(boss)).add(new AllIn(boss));
    }

    private static void bomb(EnhancedFortunaEntity boss, Vec3 from, Vec3 to, int fuse, int move) {
        FortunaBombEntity b = new FortunaBombEntity(boss.level(), from.x, from.y, from.z, boss);
        b.setup(fuse, move, to.x, to.y, to.z);
        boss.level().addFreshEntity(b);
    }

    private static Vec3 bombSpot(LivingEntity target, Vec3 offset) {
        Vec3 p = target.position().add(offset);
        double y = Shots.groundY(target.level(), p.x, target.getY() + 1, p.z) + 0.5;
        if (target.onGround()) y += 0.75;
        return new Vec3(p.x, y, p.z);
    }

    private abstract static class Stationary extends BossSkill {
        protected final EnhancedFortunaEntity boss;
        private Vec3 hold = Vec3.ZERO;

        Stationary(EnhancedFortunaEntity boss, String id, int weight, boolean p2) {
            super(id, weight, p2);
            this.boss = boss;
        }

        @Override
        public void start(LivingEntity target) {
            hold = boss.position();
            boss.playSound(MYFSounds.dameFortunaAttack.get(), 2.0f, boss.getVoicePitch());
        }

        @Override
        public final boolean tick(LivingEntity target) {
            if (!boss.isAttackPhase()) return true;
            boss.getMoveControl().setWantedPosition(hold.x, target.getY() + 1, hold.z, 1);
            boss.getLookControl().setLookAt(target);
            return step(target);
        }

        protected abstract boolean step(LivingEntity target);
    }

    /** 夹击骰：先向玩家左右两侧各掷一颗骰子炸弹，再向玩家脚下掷一颗。 */
    private static final class Pincer extends Stationary {
        Pincer(EnhancedFortunaEntity boss) {
            super(boss, "pincer", 3, false);
        }

        @Override
        protected boolean step(LivingEntity target) {
            if (t == 0 || t == 22) boss.setAnimation(DameFortunaEntity.ANIM_DICE_WINDUP);
            if (t == 10) {
                boss.setAnimation(DameFortunaEntity.ANIM_DICE_LAUNCH);
                Vec3 f = Shots.toward(boss, target);
                Vec3 side = new Vec3(-f.z, 0, f.x).scale(3);
                Vec3 from = boss.position().add(0, 2, 0);
                bomb(boss, from, bombSpot(target, side), 25, 15);
                bomb(boss, from, bombSpot(target, side.reverse()), 25, 15);
                boss.playSound(MYFSounds.dameFortunaShoot.get(), 2.0f, 1.0f);
            }
            if (t == 32) {
                boss.setAnimation(DameFortunaEntity.ANIM_DICE_LAUNCH);
                bomb(boss, boss.position().add(0, 2, 0), bombSpot(target, Vec3.ZERO), 25, 15);
                boss.playSound(MYFSounds.dameFortunaShoot.get(), 2.0f, 1.1f);
            }
            return t >= 50;
        }
    }

    /** 筹码雨：拍手后在玩家头顶一圈生成 10 枚追踪筹码，分两组射下。 */
    private static final class ChipRain extends Stationary {
        ChipRain(EnhancedFortunaEntity boss) {
            super(boss, "chip_rain", 3, false);
        }

        @Override
        protected boolean step(LivingEntity target) {
            if (t == 0) boss.setAnimation(DameFortunaEntity.ANIM_CHIPS_WINDUP);
            if (t == 10) {
                boss.setAnimation(DameFortunaEntity.ANIM_CLAP);
                boss.playSound(MYFSounds.dameFortunaClap.get(), 2.0f, 1.0f);
                boss.playSound(MYFSounds.dameFortunaChipsStart.get(), 2.0f, 1.0f);
                Vec3 from = boss.position().add(0, 1, 0);
                Vec3 top = target.position().add(0, 6, 0);
                float a0 = boss.getRandom().nextFloat() * Mth.TWO_PI;
                for (int i = 0; i < 10; i++) {
                    Vec3 hold = top.add(Shots.dir(a0 + i * Mth.TWO_PI / 10).scale(2.5));
                    Shots.fireChip(boss, 0.6, target, from, hold, i % 2 == 0 ? 30 : 45, 15, 1.0, 0);
                }
            }
            if (t == 14) boss.setAnimation(DameFortunaEntity.ANIM_IDLE);
            return t >= 60;
        }
    }

    /** 加注冲刺：两次高速穿过玩家位置，沿途每 4 tick 留下一颗短引信骰子炸弹。 */
    private static final class RaiseDash extends BossSkill {
        private static final int PASS = 18;
        private final EnhancedFortunaEntity boss;
        private Vec3 dest = Vec3.ZERO;

        RaiseDash(EnhancedFortunaEntity boss) {
            super("raise_dash", 2, false);
            this.boss = boss;
        }

        @Override
        public boolean usable(LivingEntity target) {
            return boss.distanceToSqr(target) < 24 * 24;
        }

        @Override
        public void start(LivingEntity target) {
            boss.setAnimation(DameFortunaEntity.ANIM_SPIN);
            boss.playSound(MYFSounds.dameFortunaSpinStart.get(), 2, 1);
            boss.playSound(MYFSounds.dameFortunaAttack.get(), 2.0f, boss.getVoicePitch());
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (!boss.isAttackPhase()) return true;
            int p = t % PASS;
            if (t < PASS * 2 && p == 0) {
                Vec3 d = Shots.toward(boss, target);
                dest = target.position().add(d.scale(5)).add(0, 1, 0);
            }
            if (t < PASS * 2) {
                boss.getMoveControl().setWantedPosition(dest.x, dest.y, dest.z, 3.0);
                if (p >= 4 && p % 4 == 0) {
                    Vec3 at = boss.position();
                    bomb(boss, at, new Vec3(at.x, Shots.groundY(boss.level(), at.x, at.y + 1, at.z) + 0.5, at.z), 20, 6);
                }
                return false;
            }
            if (t == PASS * 2) {
                boss.setAnimation(DameFortunaEntity.ANIM_SPIN_POSE);
                boss.playSound(MYFSounds.dameFortunaSpinStop.get(), 2, 1);
            }
            boss.getMoveControl().setWantedPosition(boss.getX(), target.getY() + 1, boss.getZ(), 1);
            return t >= PASS * 2 + 20;
        }
    }

    /** 同花顺（二阶段）：身后展开 5 列筹码，每列 4 枚，从左到右依次追踪射出。 */
    private static final class Flush extends Stationary {
        Flush(EnhancedFortunaEntity boss) {
            super(boss, "royal_flush", 3, true);
        }

        @Override
        protected boolean step(LivingEntity target) {
            if (t == 0) {
                boss.setAnimation(DameFortunaEntity.ANIM_CHIPS_WINDUP);
                Vec3 f = Shots.toward(boss, target);
                Vec3 side = new Vec3(-f.z, 0, f.x);
                Vec3 base = boss.position().subtract(f.scale(1.5));
                Vec3 from = boss.position().add(0, 1, 0);
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 4; j++) {
                        Vec3 hold = base.add(side.scale((i - 2) * 1.2)).add(0, 1.2 + j * 0.6, 0);
                        Shots.fireChip(boss, 0.6, target, from, hold, 30 + i * 8 + j * 2, 15, 1.1, 0);
                    }
                }
                boss.playSound(MYFSounds.dameFortunaChipsStart.get(), 2.0f, 0.9f);
            }
            if (t == 25) {
                boss.setAnimation(DameFortunaEntity.ANIM_CLAP);
                boss.playSound(MYFSounds.dameFortunaClap.get(), 2.0f, 1.2f);
            }
            if (t == 29) boss.setAnimation(DameFortunaEntity.ANIM_CHIPS_LAUNCH);
            return t >= 75;
        }
    }

    /** 梭哈（二阶段）：玩家周围一圈 6 颗骰子炸弹同时引爆，筹码线交织成网。 */
    private static final class AllIn extends Stationary {
        AllIn(EnhancedFortunaEntity boss) {
            super(boss, "all_in", 2, true);
        }

        @Override
        protected boolean step(LivingEntity target) {
            if (t == 0) boss.setAnimation(DameFortunaEntity.ANIM_DICE_WINDUP);
            if (t == 15) {
                boss.setAnimation(DameFortunaEntity.ANIM_DICE_LAUNCH);
                Vec3 from = boss.position().add(0, 2, 0);
                float a0 = boss.getRandom().nextFloat() * Mth.TWO_PI;
                for (int i = 0; i < 6; i++) bomb(boss, from, bombSpot(target, Shots.dir(a0 + i * Mth.TWO_PI / 6).scale(4)), 40, 20);
                boss.playSound(MYFSounds.dameFortunaShoot.get(), 2.5f, 0.8f);
                boss.playSound(MYFSounds.dameFortunaClap.get(), 2.0f, 0.8f);
            }
            return t >= 60;
        }
    }
}
