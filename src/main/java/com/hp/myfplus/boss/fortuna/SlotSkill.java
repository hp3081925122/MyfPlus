package com.hp.myfplus.boss.fortuna;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.entity.SlotMachineEntity;
import com.mojang.logging.LogUtils;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

public class SlotSkill extends FortunaSkill {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private SlotMachineEntity machine;
    private int resultAt;
    private int chipRounds;
    private int stunAfter;
    private int endAt;

    public SlotSkill(FortunaBrain b) {
        super(b, "slot", 300, false);
    }

    @Override
    protected boolean usable(LivingEntity target, double dist) {
        return dist <= 22;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    public void start(LivingEntity target) {
        machine = null;
        resultAt = -1;
        chipRounds = 0;
        stunAfter = 0;
        endAt = -1;
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        DameFortunaEntity boss = b.boss;
        b.hoverHere(target, 1.5);
        if (t == 1) {
            boss.setAnimation(DameFortunaEntity.ANIM_CLAP);
            boss.playSound(SoundEvents.NOTE_BLOCK_CHIME.get(), 2.0f, 1.2f);
        }
        if (t == 10) {
            SlotMachineEntity m = new SlotMachineEntity(MyfEntities.SLOT_MACHINE.get(), boss.level());
            m.setOwner(boss);
            m.setPos(boss.getX(), boss.getY() + boss.getBbHeight() + 1, boss.getZ());
            int[] rigged = rig(boss.getRandom());
            m.rig(rigged);
            LOGGER.debug("Fortuna slot rigged: {} {} {}", rigged[0], rigged[1], rigged[2]);
            boss.level().addFreshEntity(m);
            machine = m;
            boss.setAnimation(DameFortunaEntity.ANIM_CARD_WAIT);
        }
        if (machine == null) return t > 12;
        if (endAt < 0) {
            if (machine.isRemoved()) return true;
            if (resultAt < 0 && machine.allStopped()) resultAt = machine.lastStop() + 10;
            if (resultAt >= 0 && machine.tickCount >= resultAt) {
                apply(target);
                endAt = t + 10 + chipRounds * 12;
            }
        }
        if (chipRounds > 0 && endAt > 0 && (endAt - t) % 12 == 0 && target != null) {
            chipRounds--;
            boss.setAnimation(DameFortunaEntity.ANIM_CHIPS_LAUNCH);
            b.fanChips(target, 3 + b.act() * 2);
        }
        if (endAt > 0 && t >= endAt) {
            if (stunAfter > 0) b.stunFor(stunAfter);
            return true;
        }
        return t > 300;
    }

    private int[] rig(RandomSource r) {
        int[] pool = {SlotMachineEntity.DICE, SlotMachineEntity.DICE, SlotMachineEntity.DICE, SlotMachineEntity.CHIP, SlotMachineEntity.CHIP, SlotMachineEntity.CHIP, SlotMachineEntity.SKULL, SlotMachineEntity.SKULL, SlotMachineEntity.HEART};
        int s = pool[r.nextInt(pool.length)];
        double roll = r.nextDouble();
        double triple = b.act() >= 2 ? 0.45 : 0.30;
        if (roll < triple) return new int[]{s, s, s};
        if (roll < 0.90) {
            int[] w = {s, s, s};
            w[r.nextInt(3)] = (s + 1 + r.nextInt(SlotMachineEntity.SYMBOLS - 1)) % SlotMachineEntity.SYMBOLS;
            return w;
        }
        List<Integer> all = new ArrayList<>();
        for (int i = 0; i < SlotMachineEntity.SYMBOLS; i++) all.add(i);
        int[] w = new int[3];
        for (int i = 0; i < 3; i++) w[i] = all.remove(r.nextInt(all.size()));
        return w;
    }

    private void apply(@Nullable LivingEntity target) {
        DameFortunaEntity boss = b.boss;
        SlotMachineEntity m = machine;
        int[] s = {m.symbol(0), m.symbol(1), m.symbol(2)};
        LOGGER.debug("Fortuna slot result: {} {} {}", s[0], s[1], s[2]);
        int best = -1, count = 1;
        for (int i = 0; i < 3; i++) {
            int c = 0;
            for (int j = 0; j < 3; j++) if (s[j] == s[i]) c++;
            if (c > count) {
                count = c;
                best = s[i];
            }
        }
        boolean triple = count == 3;
        if (best < 0) {
            boss.playSound(SoundEvents.FIRE_EXTINGUISH, 2.0f, 0.6f);
            FxEntity.at(boss.level(), FxType.FT_SMOKE, m.position().add(0, 0.6, 0)).size(1.6f).spawn();
            stunAfter = 30;
        }
        else {
            boss.playSound(triple ? SoundEvents.PLAYER_LEVELUP : SoundEvents.EXPERIENCE_ORB_PICKUP, 2.0f, triple ? 0.8f : 1.2f);
            FxEntity.at(boss.level(), FxType.FT_JACKPOT, m.position().add(0, 0.6, 0)).size(triple ? 1.6f : 1.0f).spawn();
            switch (best) {
                case SlotMachineEntity.DICE -> {
                    if (target != null) {
                        boss.setAnimation(DameFortunaEntity.ANIM_DICE_LAUNCH);
                        b.throwDice(target, triple ? 6 : 2, triple ? 3 : 1);
                    }
                }
                case SlotMachineEntity.CHIP -> chipRounds = triple ? 3 : 1;
                case SlotMachineEntity.HEART -> {
                    boss.heal(boss.getMaxHealth() * (triple ? 0.05f : 0.02f));
                    boss.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2.0f, 1.4f);
                }
                case SlotMachineEntity.SKULL -> {
                    if (target != null) b.curse(target, triple ? 200 : 100);
                }
                default -> {
                    if (triple) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.5f, 0.8f);
                        FxEntity.at(boss.level(), FxType.FT_BLAST, boss.position().add(0, 1.2, 0)).size(4f).spawn();
                        Fx.shake(boss.level(), boss.position(), 0.8f);
                        boss.hurt(boss.damageSources().magic(), boss.getMaxHealth() * 0.08f);
                        stunAfter = 60;
                    }
                    else stunAfter = 20;
                }
            }
        }
        m.discard();
    }

    @Override
    public void stop() {
        super.stop();
        if (machine != null && !machine.isRemoved()) machine.discard();
        machine = null;
    }
}
