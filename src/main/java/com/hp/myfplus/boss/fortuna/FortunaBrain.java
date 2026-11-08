package com.hp.myfplus.boss.fortuna;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.entity.ChipEntity;
import com.hp.myfplus.entity.DiceEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.phys.Vec3;

public class FortunaBrain extends BossBrain<DameFortunaEntity> {
    private static final Set<String> REMOVED = Set.of("DoTheShuffle", "WaitShuffle", "SpinAttack", "DiceAttack", "ChipsAttack");
    public static final UUID ARMOR_BREAK = UUID.fromString("5f0d1c8e-6b8a-4f0e-9a3c-7d1e2b4c6a02");

    public final DiceSkill dice;
    public final RouletteSkill roulette;
    public final SlotSkill slot;
    public final ChipSkill chips;
    public final AllInSkill allIn;
    public final StunSkill stun;
    public final RoyalFlushSkill flush;

    private final Map<UUID, Long> cursed = new HashMap<>();
    public boolean betting;
    public float bet;
    private boolean allInQueued;
    private boolean introDone;
    private int lastAct = 1;
    private long armorUntil = -1;

    public FortunaBrain(DameFortunaEntity boss) {
        super(boss, MyfConfig.FORTUNA);
        dice = director.add(new DiceSkill(this));
        roulette = director.add(new RouletteSkill(this));
        slot = director.add(new SlotSkill(this));
        chips = director.add(new ChipSkill(this));
        allIn = director.add(new AllInSkill(this));
        stun = director.add(new StunSkill(this));
        flush = director.add(new RoyalFlushSkill(this));
    }

    public void install(GoalSelector goals) {
        goals.removeAllGoals(g -> REMOVED.contains(g.getClass().getSimpleName()));
        goals.addGoal(2, director);
    }

    public int act() {
        return boss.getPhase() >= DameFortunaEntity.PHASE_2 ? 2 : 1;
    }

    @Override
    public boolean firstPhase() {
        return boss.getPhase() == DameFortunaEntity.PHASE_1;
    }

    @Override
    protected void enterSecondPhase() {
        boss.setPhase(DameFortunaEntity.SHUFFLE_1);
    }

    @Override
    public int globalGap() {
        return 24 + boss.getRandom().nextInt(16) - act() * 4;
    }

    @Override
    public void serverTick() {
        int a = act();
        if (((MyfBossHost) boss).myfplus$phase() != a) setPhase(a);
        if (!introDone && target() != null) {
            introDone = true;
            lastAct = a;
            Fx.banner(boss.level(), boss.position(), Fx.BOSS_FORTUNA, a);
        }
        if (introDone && a > lastAct && boss.isAttackPhase()) {
            lastAct = a;
            Fx.banner(boss.level(), boss.position(), Fx.BOSS_FORTUNA, a);
        }
        if (boss.isShuffling()) {
            if (!allInQueued && !director.isRunning(allIn)) {
                allInQueued = true;
                director.force(allIn);
            }
        }
        else allInQueued = false;
        if (armorUntil > 0 && now() >= armorUntil) {
            armorUntil = -1;
            AttributeInstance armor = boss.getAttribute(Attributes.ARMOR);
            if (armor != null) armor.removeModifier(ARMOR_BREAK);
        }
        if (now() % 20 == 0) cursed.values().removeIf(t -> t < now());
    }

    @Override
    public float onDealDamage(LivingEntity victim, float amount) {
        Long until = cursed.get(victim.getUUID());
        return until != null && until >= now() ? amount * 1.3f : amount;
    }

    public void curse(LivingEntity victim, int ticks) {
        cursed.put(victim.getUUID(), now() + ticks);
        FxEntity.at(boss.level(), FxType.FT_CURSE, victim.position()).follow(victim, 0, victim.getBbHeight() + 0.7, 0).life(ticks).spawn();
        victim.playSound(SoundEvents.WITHER_SKELETON_AMBIENT, 1.0f, 0.6f);
    }

    public void breakArmor(int ticks) {
        AttributeInstance armor = boss.getAttribute(Attributes.ARMOR);
        if (armor == null) return;
        armor.removeModifier(ARMOR_BREAK);
        armor.addTransientModifier(new AttributeModifier(ARMOR_BREAK, "myfplus all-in", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        armorUntil = now() + ticks;
    }

    public void stunFor(int ticks) {
        stun.duration = ticks;
        director.force(stun);
    }

    public void hoverHere(LivingEntity target, double lift) {
        double y = target != null ? Math.max(boss.getY() - 0.2, target.getY() + lift) : boss.getY();
        hover(boss.getX(), y, boss.getZ(), 0.5);
    }

    public Vec3 groundAt(Vec3 p) {
        return new Vec3(p.x, GroundProbe.under(boss.level(), p.x, p.z, p.y + 0.5), p.z);
    }

    public Vec3 hand() {
        Vec3 f = Combat.dirFromYaw(boss.getYRot());
        return boss.position().add(f.x * 0.5, boss.getBbHeight() * 0.8, f.z * 0.5);
    }

    public void throwDice(LivingEntity target, int count, int minValue) {
        RandomSource r = boss.getRandom();
        Vec3 from = hand();
        Vec3 v = target.getDeltaMovement();
        Vec3 lead = new Vec3(v.x * 8, 0, v.z * 8);
        double ring = r.nextDouble() * Math.PI * 2;
        for (int i = 0; i < count; i++) {
            Vec3 spread = Vec3.ZERO;
            if (i > 0) {
                double a = ring + (i - 1) * Math.PI * 2 / Math.max(1, count - 1) + (r.nextDouble() - 0.5) * 0.6;
                double d = 1.4 + r.nextDouble() * 1.4;
                spread = new Vec3(Math.cos(a) * d, 0, Math.sin(a) * d);
            }
            Vec3 land = groundAt(target.position().add(lead).add(spread).add(0, 1, 0));
            int ticks = 14 + r.nextInt(5);
            double vy = (land.y - from.y + 0.5 * DiceEntity.GRAVITY * ticks * ticks) / ticks;
            double f = DiceEntity.travel(from.y, vy, land.y, DiceEntity.BOUNCES);
            Vec3 h = new Vec3(land.x - from.x, 0, land.z - from.z).scale(1 / f);
            if (h.length() > 1.2) h = h.normalize().scale(1.2);
            DiceEntity d = new DiceEntity(MyfEntities.DICE.get(), boss.level());
            d.setPos(from.x, from.y, from.z);
            d.setOwner(boss);
            d.target = target;
            d.spread = spread;
            d.value = minValue + r.nextInt(7 - minValue);
            d.damage = Combat.damage(boss, cfg, 0.7);
            d.setDeltaMovement(h.x, vy, h.z);
            boss.level().addFreshEntity(d);
        }
        boss.playSound(SoundEvents.BONE_BLOCK_PLACE, 1.5f, 1.2f);
    }

    public void fanChips(LivingEntity target, int count) {
        Vec3 from = hand();
        Vec3 aim = target.getEyePosition().subtract(from);
        float baseYaw = Combat.yawTo(from, target.position());
        double pitch = Math.atan2(aim.y, Math.sqrt(aim.x * aim.x + aim.z * aim.z));
        for (int i = 0; i < count; i++) {
            float yaw = baseYaw + (count == 1 ? 0 : (i / (float) (count - 1) - 0.5f) * 60f);
            Vec3 d = Combat.dirFromYaw(yaw);
            Vec3 v = new Vec3(d.x * Math.cos(pitch), Math.sin(pitch), d.z * Math.cos(pitch)).scale(0.7);
            ChipEntity c = new ChipEntity(MyfEntities.CHIP.get(), boss.level());
            c.setPos(from.x, from.y, from.z);
            c.setOwner(boss);
            c.damage = Combat.damage(boss, cfg, 0.4);
            c.setDeltaMovement(v);
            boss.level().addFreshEntity(c);
        }
        boss.playSound(SoundEvents.CHAIN_PLACE, 1.5f, 1.6f);
    }

    public void chipRain(LivingEntity target, int count) {
        RandomSource r = boss.getRandom();
        for (int i = 0; i < count; i++) {
            double a = r.nextDouble() * Math.PI * 2, d = r.nextDouble() * 5;
            Vec3 land = groundAt(target.position().add(Math.cos(a) * d, 0, Math.sin(a) * d));
            int delay = 15 + i * 2;
            FxEntity.at(boss.level(), FxType.WARN_CIRCLE, land).size(1f).life(delay).palette(2).spawn();
            ChipEntity c = new ChipEntity(MyfEntities.CHIP.get(), boss.level());
            c.setPos(land.x, land.y + delay * 0.6, land.z);
            c.setOwner(boss);
            c.bounces = 0;
            c.aoe = 1f;
            c.life = delay + 10;
            c.damage = Combat.damage(boss, cfg, 0.5);
            c.setDeltaMovement(0, -0.6, 0);
            boss.level().addFreshEntity(c);
        }
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putBoolean("intro", introDone);
        tag.putInt("act", lastAct);
    }

    @Override
    public void load(CompoundTag tag) {
        introDone = tag.getBoolean("intro");
        lastAct = Math.max(1, tag.getInt("act"));
    }
}
