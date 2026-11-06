package com.hp.myfplus.boss.swampjaw;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.entity.MudPitEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SwampjawBrain extends BossBrain<SwampjawEntity> {
    public static final int SKILL_SPIT = 1;
    public static final int SKILL_SUBMERGED = 2;
    public static final int MAX_PITS = 6;

    public final SwampjawFlight flight;
    public final DashSkill dash;
    public final HookSkill hook;
    public final MudSkill mud;
    public final LurkSkill lurk;
    public final BubbleSkill bubble;
    public final MireSkill mire;
    public final MaelstromSkill maelstrom;
    public final List<MudPitEntity> pits = new ArrayList<>();
    private boolean introDone;
    @Nullable
    public Vec3 center;
    public boolean phase2;
    private boolean transitionQueued;
    public boolean dashing;
    public boolean stunRequested;
    public boolean stunned;
    public boolean submerged;

    public SwampjawBrain(SwampjawEntity fish) {
        super(fish, MyfConfig.SWAMPJAW);
        flight = new SwampjawFlight(fish);
        dash = director.add(new DashSkill(this));
        hook = director.add(new HookSkill(this));
        mud = director.add(new MudSkill(this));
        lurk = director.add(new LurkSkill(this));
        bubble = director.add(new BubbleSkill(this));
        mire = director.add(new MireSkill(this));
        maelstrom = director.add(new MaelstromSkill(this));
    }

    public void install(GoalSelector goals) {
        goals.removeAllGoals(g -> true);
        goals.addGoal(1, director);
        goals.addGoal(4, new SwampjawOrbitGoal(this));
    }

    @Override
    public int globalGap() {
        int gap = 30 + boss.getRandom().nextInt(21);
        return phase2 ? (int) (gap * 0.75) : gap;
    }

    @Override
    public void serverTick() {
        if (center == null) center = boss.position();
        pits.removeIf(Entity::isRemoved);
        int phase = phase2 ? 2 : 1;
        if (((MyfBossHost) boss).myfplus$phase() != phase) setPhase(phase);
        if (!introDone && target() != null) {
            introDone = true;
            Fx.banner(boss.level(), boss.position(), Fx.BOSS_SWAMPJAW, 1);
        }
        if (!phase2 && !transitionQueued && hpRatio() < 0.5f) {
            transitionQueued = true;
            director.force(mire);
        }
    }

    @Override
    public boolean blocksAttack(DamageSource source) {
        return submerged && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public float onHurt(DamageSource source, float amount) {
        if (dashing && source.getEntity() instanceof Player) stunRequested = true;
        return stunned ? amount * 1.25f : amount;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putBoolean("phase2", phase2);
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

    public void addPit(Vec3 pos) {
        pits.removeIf(Entity::isRemoved);
        while (pits.size() >= MAX_PITS) pits.remove(0).discard();
        MudPitEntity pit = new MudPitEntity(MyfEntities.MUD_PIT.get(), boss.level());
        pit.setPos(pos.x, pos.y, pos.z);
        pit.setOwner(boss);
        pit.life = phase2 ? 600 : 400;
        boss.level().addFreshEntity(pit);
        pits.add(pit);
        boss.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.MUD_PLACE, SoundSource.HOSTILE, 1.5f, 0.8f);
        Fx.rockBurst(boss.level(), pos, 1.2f, 5);
    }

    @Nullable
    public MudPitEntity nearestPit(Vec3 pos, double maxDist) {
        MudPitEntity best = null;
        double bestD = maxDist * maxDist;
        for (MudPitEntity p : pits) {
            if (p.isRemoved()) continue;
            double d = p.position().distanceToSqr(pos);
            if (d <= bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    public boolean nearAnyPit(Entity e, double extra) {
        for (MudPitEntity p : pits) {
            if (p.isRemoved()) continue;
            double r = p.radius + extra;
            if (Combat.horizontalDist(p.position(), e.position()) <= r && Math.abs(p.getY() - e.getY()) < 2.5) return true;
        }
        return false;
    }
}
