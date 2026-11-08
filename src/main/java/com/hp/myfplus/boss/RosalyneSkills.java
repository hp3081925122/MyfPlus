package com.hp.myfplus.boss;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Shots;
import com.hp.myfplus.core.SkillRunner;
import com.hp.myfplus.entity.boss.EnhancedRosalyneEntity;

import lykrast.meetyourfight.entity.ProjectileLineEntity;
import lykrast.meetyourfight.entity.RosalyneEntity;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import lykrast.meetyourfight.registry.MYFEntities;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public final class RosalyneSkills {
    public static final String FAN = "rose_fan";
    private static final int V = ProjectileLineEntity.VAR_ROSALYNE;

    private RosalyneSkills() {
    }

    public static void register(EnhancedRosalyneEntity boss, SkillRunner runner) {
        runner.add(new Fan(boss)).add(new Blink(boss)).add(new SunsetRing(boss)).add(new Corolla(boss));
    }

    private static void hold(EnhancedRosalyneEntity boss, Vec3 at) {
        boss.getMoveControl().setWantedPosition(at.x, at.y, at.z, 2);
    }

    /** 玫瑰扇刃：向玩家方向扇形生成 5 道玫瑰幽灵线，15 tick 后射出。 */
    private static final class Fan extends BossSkill {
        private final EnhancedRosalyneEntity boss;
        private Vec3 at = Vec3.ZERO;

        Fan(EnhancedRosalyneEntity boss) {
            super(FAN, 3, false);
            this.boss = boss;
        }

        @Override
        public void start(LivingEntity target) {
            at = boss.position();
            boss.setAnimation(RosalyneEntity.ANIM_ARM_OUT_UP);
            boss.playSound(MYFSounds.rosalyneSwingPrepare.get(), 1.5f, 1.0f);
            Vec3 eye = boss.position().add(0, 1.2, 0);
            Vec3 aim = target.getEyePosition().subtract(eye).normalize();
            for (int i = -2; i <= 2; i++) {
                Vec3 d = aim.yRot(i * 15f * Mth.DEG_TO_RAD);
                Shots.fireLine(boss, V, 0.5, eye.add(d.scale(1.2)), d, 15);
            }
            Shots.particles(boss.level(), ParticleTypes.CHERRY_LEAVES, eye, 25, 1.0, 0.05);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (!boss.attackPhase()) return true;
            hold(boss, at);
            boss.getLookControl().setLookAt(target);
            if (t == 15) {
                boss.setAnimation(RosalyneEntity.ANIM_ARM_IN_UP);
                boss.playSound(MYFSounds.rosalyneSwing.get(), 1.5f, 1.2f);
            }
            return t >= 25;
        }
    }

    /** 瞬步斩：化作花瓣消失，10 tick 后出现在玩家背后挥砍。 */
    private static final class Blink extends BossSkill {
        private final EnhancedRosalyneEntity boss;
        private Vec3 at = Vec3.ZERO;

        Blink(EnhancedRosalyneEntity boss) {
            super("blink", 2, false);
            this.boss = boss;
        }

        @Override
        public boolean usable(LivingEntity target) {
            return boss.distanceToSqr(target) < 20 * 20;
        }

        @Override
        public void start(LivingEntity target) {
            at = boss.position();
            boss.setInvisible(true);
            boss.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.5f, 1.4f);
            Shots.particles(boss.level(), ParticleTypes.CHERRY_LEAVES, boss.position().add(0, 1, 0), 40, 0.6, 0.05);
            Shots.particles(boss.level(), ParticleTypes.PORTAL, boss.position().add(0, 1, 0), 30, 0.5, 0.3);
        }

        private Vec3 spot(LivingEntity target) {
            Vec3 back = Vec3.directionFromRotation(0, target.getYRot()).reverse();
            Vec3 side = new Vec3(-back.z, 0, back.x);
            for (Vec3 d : new Vec3[] {back, side, side.reverse(), back.reverse()}) {
                Vec3 p = target.position().add(d.scale(2)).add(0, 0.2, 0);
                AABB box = boss.getBoundingBox().move(p.subtract(boss.position()));
                if (boss.level().noCollision(boss, box)) return p;
            }
            return target.position().add(0, 2.5, 0);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (!boss.attackPhase()) return true;
            hold(boss, at);
            if (t == 10) {
                at = spot(target);
                boss.moveTo(at.x, at.y, at.z, boss.getYRot(), boss.getXRot());
                boss.setInvisible(false);
                boss.setAnimation(RosalyneEntity.ANIM_ARM_OUT_DN);
                boss.playSound(MYFSounds.rosalyneSwingPrepare.get(), 1.5f, 1.3f);
                Shots.particles(boss.level(), ParticleTypes.CHERRY_LEAVES, at.add(0, 1, 0), 40, 0.6, 0.05);
            }
            if (t > 10) boss.getLookControl().setLookAt(target);
            if (t == 16) boss.swing();
            return t >= 26;
        }

        @Override
        public void stop() {
            boss.setInvisible(false);
        }
    }

    /** 落日环刃（二阶段）：跃起悬停，玩家周围一圈 16 道玫瑰幽灵线分两拨射向圆心，随后接本体垂直下劈。 */
    private static final class SunsetRing extends BossSkill {
        private static final int COUNT = 16;
        private final EnhancedRosalyneEntity boss;
        private Vec3 at = Vec3.ZERO;

        SunsetRing(EnhancedRosalyneEntity boss) {
            super("sunset_ring", 3, true);
            this.boss = boss;
        }

        @Override
        public void start(LivingEntity target) {
            at = target.position().add(Shots.toward(target, boss).scale(3)).add(0, 4, 0);
            boss.setAnimation(RosalyneEntity.ANIM_PREPARE_CRASH);
            boss.playSound(MYFSounds.rosalyneCrack.get(), 1.5f, 1.2f);
            Vec3 c = Shots.feet(target).add(0, 0.6, 0);
            float a0 = boss.getRandom().nextFloat() * Mth.TWO_PI;
            for (int k = 0; k < COUNT; k++) {
                Vec3 d = Shots.dir(a0 + k * Mth.TWO_PI / COUNT);
                Shots.fireLine(boss, V, 0.5, c.add(d.scale(7)), d.reverse(), k % 2 == 0 ? 20 : 35);
            }
            Shots.particles(boss.level(), ParticleTypes.FLAME, c, 40, 3.5, 0.02);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (!boss.attackPhase()) return true;
            hold(boss, at);
            boss.getLookControl().setLookAt(target);
            if (t == 20 || t == 35) boss.playSound(MYFSounds.roseSpiritShoot.get(), 1.5f, 1.0f);
            if (t >= 40) {
                boss.crashNext();
                return true;
            }
            return false;
        }
    }

    /** 花冠再临（二阶段）：召唤 2 只玫瑰灵协助作战，场上最多 2 只。 */
    private static final class Corolla extends BossSkill {
        private final EnhancedRosalyneEntity boss;
        private Vec3 at = Vec3.ZERO;

        Corolla(EnhancedRosalyneEntity boss) {
            super("corolla", 2, true);
            this.boss = boss;
        }

        @Override
        public boolean usable(LivingEntity target) {
            return boss.corollaCooldown <= 0
                    && boss.level().getEntitiesOfClass(RoseSpiritEntity.class, boss.getBoundingBox().inflate(32), s -> s.isAlive() && s.getOwner() == boss).size() < 2;
        }

        @Override
        public void start(LivingEntity target) {
            at = boss.position();
            boss.setAnimation(RosalyneEntity.ANIM_SUMMONING);
            boss.playSound(MYFSounds.roseSpiritWarn.get(), 2.0f, 0.9f);
        }

        @Override
        public boolean tick(LivingEntity target) {
            if (!boss.attackPhase()) return true;
            hold(boss, at);
            if (t % 4 == 0) Shots.particles(boss.level(), ParticleTypes.CHERRY_LEAVES, boss.position().add(0, 1, 0), 10, 1.5, 0.05);
            if (t == 20 && boss.level() instanceof ServerLevel level) {
                for (int i = 0; i < 2; i++) {
                    RoseSpiritEntity spirit = MYFEntities.ROSE_SPIRIT.get().create(level);
                    if (spirit == null) continue;
                    spirit.moveTo(boss.getX() + (i == 0 ? -2 : 2), boss.getY(), boss.getZ());
                    spirit.setOwner(boss);
                    spirit.setTarget(target);
                    spirit.attackCooldown = 40 + 30 * i;
                    ForgeEventFactory.onFinalizeSpawn(spirit, level, level.getCurrentDifficultyAt(boss.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                    level.addFreshEntity(spirit);
                }
                boss.corollaCooldown = 600;
                boss.playSound(MYFSounds.roseSpiritShoot.get(), 2.0f, 0.8f);
            }
            return t >= 40;
        }
    }
}
