package com.hp.myfplus.core;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.logging.LogUtils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class SkillRunner extends Goal {
    private final Mob boss;
    private final Runnable onEnd;
    private final List<BossSkill> skills = new ArrayList<>();
    @Nullable
    private BossSkill pending;
    @Nullable
    private BossSkill active;
    private boolean done;

    public SkillRunner(Mob boss, Runnable onEnd) {
        this.boss = boss;
        this.onEnd = onEnd;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public SkillRunner add(BossSkill skill) {
        skills.add(skill);
        return this;
    }

    public boolean busy() {
        return pending != null || active != null;
    }

    @Nullable
    public BossSkill active() {
        return active;
    }

    public boolean pick(LivingEntity target, boolean phase2) {
        int total = 0;
        List<BossSkill> ok = new ArrayList<>();
        for (BossSkill s : skills) {
            if (s.weight <= 0 || (s.phase2Only && !phase2) || !s.usable(target)) continue;
            ok.add(s);
            total += s.weight;
        }
        if (total <= 0) return false;
        int r = boss.getRandom().nextInt(total);
        for (BossSkill s : ok) {
            r -= s.weight;
            if (r < 0) {
                pending = s;
                return true;
            }
        }
        return false;
    }

    public boolean queue(String id) {
        for (BossSkill s : skills) {
            if (s.id.equals(id)) {
                pending = s;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canUse() {
        LivingEntity t = boss.getTarget();
        if (pending != null && (t == null || !t.isAlive())) pending = null;
        return pending != null;
    }

    @Override
    public void start() {
        active = pending;
        pending = null;
        done = false;
        active.t = 0;
        LogUtils.getLogger().debug("Enhanced boss {} starts skill {}", boss.getType().getDescriptionId(), active.id);
        active.start(boss.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity t = boss.getTarget();
        if (active == null || t == null || !t.isAlive()) {
            done = true;
            return;
        }
        done = active.tick(t);
        active.t++;
    }

    @Override
    public boolean canContinueToUse() {
        return active != null && !done && boss.isAlive();
    }

    @Override
    public void stop() {
        if (active != null) active.stop();
        active = null;
        done = false;
        onEnd.run();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
