package com.hp.myfplus.core;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class SkillGoal extends Goal {
    private final BossBrain<?> brain;
    public final List<BossSkill> skills = new ArrayList<>();
    @Nullable
    private BossSkill current;
    @Nullable
    private BossSkill last;
    @Nullable
    private BossSkill forced;
    private long nextReady;
    private int t;
    private boolean finished;

    public SkillGoal(BossBrain<?> brain) {
        this.brain = brain;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    public <S extends BossSkill> S add(S skill) {
        skills.add(skill);
        return skill;
    }

    public void force(BossSkill skill) {
        forced = skill;
    }

    public boolean forceByName(String name) {
        for (BossSkill s : skills) {
            if (s.name.equals(name)) {
                forced = s;
                return true;
            }
        }
        return false;
    }

    public boolean isRunning(BossSkill skill) {
        return current == skill;
    }

    public void delay(int ticks) {
        nextReady = Math.max(nextReady, brain.now() + ticks);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (!brain.boss.isAlive()) return false;
        LivingEntity target = brain.target();
        if (forced != null) {
            if (target == null) return false;
            current = forced;
            forced = null;
            return true;
        }
        if (target == null || brain.now() < nextReady) return false;
        current = pick(target);
        return current != null;
    }

    @Nullable
    private BossSkill pick(LivingEntity target) {
        double dist = brain.boss.distanceTo(target);
        long now = brain.now();
        double cdMul = brain.cfg.cooldown();
        int total = 0;
        List<BossSkill> pool = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        for (BossSkill s : skills) {
            if (s == last || s.cooldown < 0) continue;
            if (now < s.lastUsed + (long) (s.cooldown * cdMul * s.cooldownScale())) continue;
            if (!s.canUse(target, dist)) continue;
            int w = s.weight(target, dist);
            if (w <= 0) continue;
            pool.add(s);
            weights.add(w);
            total += w;
        }
        if (total <= 0) return null;
        int r = brain.boss.getRandom().nextInt(total);
        for (int i = 0; i < pool.size(); i++) {
            r -= weights.get(i);
            if (r < 0) return pool.get(i);
        }
        return pool.get(pool.size() - 1);
    }

    @Override
    public void start() {
        t = 0;
        finished = false;
        LivingEntity target = brain.target();
        if (current != null && target != null) current.start(target);
        else finished = true;
    }

    @Override
    public void tick() {
        if (current == null) {
            finished = true;
            return;
        }
        t++;
        if (current.tick(t, brain.target())) finished = true;
    }

    @Override
    public boolean canContinueToUse() {
        return !finished && current != null && brain.boss.isAlive();
    }

    @Override
    public void stop() {
        if (current != null) {
            current.stop();
            current.lastUsed = brain.now();
            last = current;
        }
        current = null;
        brain.setSkill(0);
        nextReady = Math.max(nextReady, brain.now() + (long) (brain.globalGap() * brain.cfg.cooldown()));
    }
}
