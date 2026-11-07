package com.hp.myfplus.boss.rosalyne;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.entity.DuelRingEntity;
import com.hp.myfplus.entity.PetalBladeEntity;
import com.hp.myfplus.entity.ThornEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.RosalyneEntity;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RosalyneBrain extends BossBrain<RosalyneEntity> {
    private static final Set<String> REMOVED = Set.of("AdvanceAndSwingAttack", "CircleAndDashAttack", "VerticalCrashAttack");

    public final TripleSlashSkill triple;
    public final CorridorSkill corridor;
    public final StanceSkill stance;
    public final SunsetSkill sunset;
    public final CorollaSkill corolla;
    public final ArenaSkill arena;
    public final WaltzSkill waltz;

    public int act = 1;
    public boolean invulnerable;
    public boolean parrying;
    @Nullable
    public LivingEntity counterMelee;
    @Nullable
    public LivingEntity counterRanged;
    public int vulnerable;
    public final List<PetalBladeEntity> petals = new ArrayList<>();
    public final List<List<ThornEntity>> thornGroups = new ArrayList<>();
    @Nullable
    public DuelRingEntity ring;
    private boolean introDone;
    private boolean corollaQueued;
    private boolean arenaQueued;
    private int noTarget;

    public RosalyneBrain(RosalyneEntity boss) {
        super(boss, MyfConfig.ROSALYNE);
        triple = director.add(new TripleSlashSkill(this));
        corridor = director.add(new CorridorSkill(this));
        stance = director.add(new StanceSkill(this));
        sunset = director.add(new SunsetSkill(this));
        corolla = director.add(new CorollaSkill(this));
        arena = director.add(new ArenaSkill(this));
        waltz = director.add(new WaltzSkill(this));
    }

    public void install(GoalSelector goals) {
        goals.removeAllGoals(g -> REMOVED.contains(g.getClass().getSimpleName()));
        goals.addGoal(2, director);
    }

    public boolean fighting() {
        int p = boss.getPhase();
        return p == RosalyneEntity.PHASE_1 || p == RosalyneEntity.PHASE_3;
    }

    @Override
    public int globalGap() {
        int gap = 22 + boss.getRandom().nextInt(16);
        return act >= 3 ? (int) (gap * 0.7) : gap;
    }

    @Override
    public void serverTick() {
        if (((MyfBossHost) boss).myfplus$phase() != act) setPhase(act);
        if (vulnerable > 0) vulnerable--;
        LivingEntity target = target();
        if (!fighting()) return;
        if (restoreAfterLoad && target != null) restore(target);
        if (!introDone && target != null) {
            introDone = true;
            Fx.banner(boss.level(), boss.position(), Fx.BOSS_ROSALYNE, act);
        }
        if (act == 1 && !corollaQueued && hpRatio() < 0.5f) {
            corollaQueued = true;
            director.force(corolla);
        }
        if (act == 2 && !arenaQueued && hpRatio() < 0.2f) {
            arenaQueued = true;
            director.force(arena);
        }
        petals.removeIf(Entity::isRemoved);
        if (act >= 2 && !invulnerable) {
            long now = now();
            if (target != null && now % 100 == 0) {
                int fired = 0;
                for (PetalBladeEntity p : petals) {
                    if (fired >= 2 || p.mode() != PetalBladeEntity.ORBIT) continue;
                    Vec3 dir = target.getEyePosition().subtract(p.position()).normalize();
                    Vec3 side = new Vec3(-dir.z, 0.4, dir.x).scale(fired == 0 ? 0.5 : -0.5);
                    p.launch(PetalBladeEntity.HOMING, target, dir.add(side).normalize().scale(0.5));
                    fired++;
                }
            }
            if (now % 200 == 100 && orbiting() < 8) addPetal(freeSlot());
        }
        if (ring != null) {
            if (ring.isRemoved()) ring = null;
            else if (target == null) {
                if (++noTarget > 600) {
                    ring.discard();
                    ring = null;
                }
            }
            else noTarget = 0;
        }
    }

    public int orbiting() {
        int n = 0;
        for (PetalBladeEntity p : petals) if (p.mode() == PetalBladeEntity.ORBIT) n++;
        return n;
    }

    private int freeSlot() {
        for (int s = 0; s < 8; s++) {
            boolean used = false;
            for (PetalBladeEntity p : petals) if (p.mode() == PetalBladeEntity.ORBIT && (int) p.slot == s) used = true;
            if (!used) return s;
        }
        return 0;
    }

    public PetalBladeEntity addPetal(int slot) {
        PetalBladeEntity p = new PetalBladeEntity(MyfEntities.PETAL_BLADE.get(), boss.level());
        p.setOwner(boss);
        p.slot = slot;
        p.setPos(boss.getX(), boss.getY() + 1.1, boss.getZ());
        p.damage = Combat.damage(boss, cfg, 0.4);
        p.contactDamage = Combat.damage(boss, cfg, 0.3);
        boss.level().addFreshEntity(p);
        petals.add(p);
        return p;
    }

    @Override
    public boolean blocksAttack(DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (invulnerable) return true;
        if (!parrying) return false;
        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity living) || attacker == boss) return false;
        if (source.getDirectEntity() instanceof Projectile pr) {
            if (pr.isAlive() && pr.getOwner() != boss) reflect(pr, living);
            else if (counterRanged == null) counterRanged = living;
        }
        else if (counterMelee == null) counterMelee = living;
        return true;
    }

    @Override
    public boolean deflects(Projectile p) {
        if (p.getOwner() == boss) return true;
        if (invulnerable || !parrying || !(p.getOwner() instanceof LivingEntity shooter)) return false;
        reflect(p, shooter);
        return true;
    }

    public void reflect(Projectile p, LivingEntity shooter) {
        if (counterRanged == null) counterRanged = shooter;
        Vec3 dir = shooter.getEyePosition().subtract(p.position());
        dir = dir.lengthSqr() < 1.0E-4 ? p.getDeltaMovement().scale(-1).normalize() : dir.normalize();
        double speed = Math.max(0.8, p.getDeltaMovement().length()) * 1.2;
        p.setOwner(boss);
        if (p instanceof AbstractArrow a) a.pickup = AbstractArrow.Pickup.DISALLOWED;
        if (p instanceof AbstractHurtingProjectile h) {
            h.xPower = dir.x * 0.1;
            h.yPower = dir.y * 0.1;
            h.zPower = dir.z * 0.1;
        }
        p.setPos(p.getX() + dir.x * 0.6, p.getY() + dir.y * 0.6, p.getZ() + dir.z * 0.6);
        p.setDeltaMovement(dir.scale(speed));
        p.hurtMarked = true;
        FxEntity.at(boss.level(), FxType.RS_GLINT, p.position()).spawn();
        boss.playSound(net.minecraft.sounds.SoundEvents.ANVIL_PLACE, 1.0f, 2.0f);
    }

    @Override
    public float onHurt(DamageSource source, float amount) {
        return vulnerable > 0 ? amount * 1.3f : amount;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("act", act);
        tag.putBoolean("intro", introDone);
    }

    @Override
    public void load(CompoundTag tag) {
        act = Math.max(1, tag.getInt("act"));
        introDone = tag.getBoolean("intro");
        corollaQueued = act >= 2;
        arenaQueued = act >= 3;
        restoreAfterLoad = true;
    }

    private boolean restoreAfterLoad;

    private void restore(@Nullable LivingEntity target) {
        restoreAfterLoad = false;
        if (act >= 2) for (int s = 0; s < 8; s++) addPetal(s);
        if (act >= 3 && target != null) spawnRing(groundAt(boss.position().add(target.position()).scale(0.5)), MyfConfig.DUEL_RADIUS.get());
    }

    public Vec3 groundAt(Vec3 p) {
        return new Vec3(p.x, GroundProbe.under(boss.level(), p.x, p.z, p.y + 0.5), p.z);
    }

    public void place(Vec3 p) {
        boss.setPos(p.x, p.y, p.z);
        boss.setDeltaMovement(Vec3.ZERO);
        boss.getMoveControl().setWantedPosition(p.x, p.y, p.z, 0.1);
    }

    public void face(float yaw) {
        boss.setYRot(yaw);
        boss.yBodyRot = yaw;
        boss.yHeadRot = yaw;
    }

    public void swingSound() {
        boss.playSound(MYFSounds.rosalyneSwing.get(), 1.5f, 0.9f + boss.getRandom().nextFloat() * 0.2f);
    }

    public void blink(Vec3 to) {
        FxEntity.at(boss.level(), FxType.RS_PETAL_BURST, boss.position().add(0, 1, 0)).spawn();
        place(to);
        FxEntity.at(boss.level(), FxType.RS_PETAL_BURST, to.add(0, 1, 0)).spawn();
        boss.playSound(net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.5f);
    }

    public void hitSector(Vec3 origin, float yaw, float radius, float halfAngle, double mult, double kb, Set<Integer> hit) {
        AABB box = new AABB(origin, origin).inflate(radius, 2.5, radius);
        for (LivingEntity v : Combat.foes(boss, box)) {
            if (hit.contains(v.getId())) continue;
            if (Combat.horizontalDist(v.position(), origin) > radius) continue;
            if (Math.abs(Mth.wrapDegrees(Combat.yawTo(origin, v.position()) - yaw)) > halfAngle && Combat.horizontalDist(v.position(), origin) > 1.0) continue;
            hit.add(v.getId());
            Combat.hit(boss, boss.damageSources().mobAttack(boss), Combat.damage(boss, cfg, mult), v, kb, origin);
        }
    }

    public void hitSegment(Vec3 a, Vec3 c, double width, double mult, double kb, Set<Integer> hit) {
        AABB box = new AABB(a, c).inflate(width + 1, 2.5, width + 1);
        double dx = c.x - a.x, dz = c.z - a.z, len2 = Math.max(1.0E-4, dx * dx + dz * dz);
        for (LivingEntity v : Combat.foes(boss, box)) {
            if (hit.contains(v.getId())) continue;
            double k = Mth.clamp(((v.getX() - a.x) * dx + (v.getZ() - a.z) * dz) / len2, 0, 1);
            double qx = a.x + dx * k, qz = a.z + dz * k;
            double ox = v.getX() - qx, oz = v.getZ() - qz;
            if (ox * ox + oz * oz > width * width) continue;
            hit.add(v.getId());
            Vec3 from = new Vec3(qx - ox * 2, v.getY(), qz - oz * 2);
            Combat.hit(boss, boss.damageSources().mobAttack(boss), Combat.damage(boss, cfg, mult), v, kb, from);
        }
    }

    public DuelRingEntity spawnRing(Vec3 center, float radius) {
        if (ring != null && !ring.isRemoved()) ring.discard();
        DuelRingEntity r = new DuelRingEntity(MyfEntities.DUEL_RING.get(), boss.level());
        r.setPos(center.x, center.y, center.z);
        r.setOwner(boss);
        r.radius = radius;
        r.damage = Combat.damage(boss, cfg, 0.35);
        boss.level().addFreshEntity(r);
        ring = r;
        return r;
    }
}
