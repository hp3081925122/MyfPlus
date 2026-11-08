package com.hp.myfplus.boss.swampjaw;

import javax.annotation.Nullable;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.fx.Fx;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.phys.Vec3;

/** 强化沼牙「远古化石猎食者」：把地面当水，潜地游动、破土跃袭、用化石巨颌从地下咬合。 */
public class SwampjawBrain extends BossBrain<SwampjawEntity> {
    public static final int SKILL_JAW = 1;
    /** 客户端在此技能 ID 下不渲染 Boss（见 ClientEvents），敲钟人的隐身也复用它。 */
    public static final int SKILL_SUBMERGED = 2;

    public final SwampjawFlight flight;
    public final BreachSkill breach;
    public final GroundJawSkill groundJaw;
    public final BoneRainSkill boneRain;
    public final FinStormSkill fins;
    public final TailSkill tail;
    public final AwakenSkill awaken;
    public final WorldMawSkill worldMaw;
    @Nullable
    public Vec3 center;
    public boolean phase2;
    public boolean guarded;
    private boolean submerged;
    private boolean introDone;
    private boolean transitionQueued;

    public SwampjawBrain(SwampjawEntity fish) {
        super(fish, MyfConfig.SWAMPJAW);
        flight = new SwampjawFlight(fish);
        breach = director.add(new BreachSkill(this));
        groundJaw = director.add(new GroundJawSkill(this));
        boneRain = director.add(new BoneRainSkill(this));
        fins = director.add(new FinStormSkill(this));
        tail = director.add(new TailSkill(this));
        awaken = director.add(new AwakenSkill(this));
        worldMaw = director.add(new WorldMawSkill(this));
    }

    public void install(GoalSelector goals) {
        goals.removeAllGoals(g -> true);
        goals.addGoal(1, director);
        goals.addGoal(4, new SwampjawOrbitGoal(this));
    }

    @Override
    public int globalGap() {
        int gap = 26 + boss.getRandom().nextInt(19);
        return phase2 ? (int) (gap * 0.75) : gap;
    }

    @Override
    public void serverTick() {
        if (center == null) center = boss.position();
        int phase = phase2 ? 2 : 1;
        if (((MyfBossHost) boss).myfplus$phase() != phase) setPhase(phase);
        if (!introDone && target() != null) {
            introDone = true;
            Fx.banner(boss.level(), boss.position(), Fx.BOSS_SWAMPJAW, 1);
        }
    }

    @Override
    public boolean firstPhase() {
        return !phase2 && !transitionQueued;
    }

    @Override
    protected void enterSecondPhase() {
        transitionQueued = true;
        director.force(awaken);
    }

    @Override
    public boolean blocksAttack(DamageSource source) {
        return (submerged || guarded) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    public boolean submerged() {
        return submerged;
    }

    public void dive() {
        submerged = true;
        setSkill(SKILL_SUBMERGED);
    }

    public void surface() {
        if (submerged) setSkill(0);
        submerged = false;
    }

    /** 目标脚下或附近 2.5 格内有地面时才放地面技能。 */
    public boolean grounded(LivingEntity target) {
        return Math.abs(target.getY() - ground(target.getX(), target.getZ(), target.getY() + 0.5)) < 2.5;
    }

    public Vec3 groundAt(double x, double z, double refY) {
        return new Vec3(x, ground(x, z, refY), z);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putBoolean("phase2", phase2 || transitionQueued);
        tag.putBoolean("intro", introDone);
        if (center != null) {
            tag.putDouble("cx", center.x);
            tag.putDouble("cy", center.y);
            tag.putDouble("cz", center.z);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        phase2 = tag.getBoolean("phase2");
        introDone = tag.getBoolean("intro");
        transitionQueued = phase2;
        if (tag.contains("cx")) center = new Vec3(tag.getDouble("cx"), tag.getDouble("cy"), tag.getDouble("cz"));
        surface();
    }

    public static Vec3 mouth(Entity fish) {
        Vec3 f = Combat.dirFromYaw(fish.getYRot());
        return fish.position().add(f.x * 1.7, 0.7, f.z * 1.7);
    }

    public static Vec3 mouth(Entity fish, float partial) {
        Vec3 f = Combat.dirFromYaw(Mth.rotLerp(partial, fish.yRotO, fish.getYRot()));
        return fish.getPosition(partial).add(f.x * 1.7, 0.7, f.z * 1.7);
    }

    public double ground(double x, double z, double refY) {
        return GroundProbe.under(boss.level(), x, z, refY);
    }
}
