package com.hp.myfplus.boss.fortuna;

import javax.annotation.Nullable;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.entity.AllInEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class AllInSkill extends FortunaSkill {
    public static final int DURATION = 200;
    @Nullable
    private AllInEntity tower;
    private float need;
    private int round;

    public AllInSkill(FortunaBrain b) {
        super(b, "all_in", -1, true);
    }

    @Override
    public void start(LivingEntity target) {
        DameFortunaEntity boss = b.boss;
        round = Math.max(0, Math.min(2, (boss.getPhase() - 1) / 2));
        double frac = round == 0 ? MyfConfig.ALL_IN_1.get() : round == 1 ? MyfConfig.ALL_IN_2.get() : MyfConfig.ALL_IN_3.get();
        need = (float) (boss.getMaxHealth() * frac);
        b.bet = 0;
        b.betting = true;
        AllInEntity e = new AllInEntity(MyfEntities.ALL_IN.get(), boss.level());
        e.setOwner(boss);
        e.setPos(boss.getX(), boss.getY() + boss.getBbHeight() + 0.6, boss.getZ());
        e.life = DURATION + 20;
        boss.level().addFreshEntity(e);
        tower = e;
        boss.setAnimation(DameFortunaEntity.ANIM_SPIN_POSE);
        boss.playSound(SoundEvents.CHAIN_PLACE, 2.5f, 0.6f);
        boss.playSound(SoundEvents.BELL_RESONATE, 1.5f, 1.4f);
        Fx.shake(boss.level(), boss.position(), 0.4f);
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        DameFortunaEntity boss = b.boss;
        if (!boss.isShuffling()) return true;
        b.hoverHere(target, 2.5);
        if (tower != null) tower.setProgress(b.bet / need);
        if (target != null && t % 40 == 20) {
            boss.setAnimation(DameFortunaEntity.ANIM_CHIPS_LAUNCH);
            b.fanChips(target, 5);
        }
        if (t % 40 == 26) boss.setAnimation(DameFortunaEntity.ANIM_SPIN_POSE);
        if (b.bet >= need) {
            win();
            return true;
        }
        if (t >= DURATION) {
            lose(target);
            return true;
        }
        return false;
    }

    private void win() {
        DameFortunaEntity boss = b.boss;
        b.betting = false;
        Vec3 p = tower != null ? tower.position() : boss.position();
        FxEntity.at(boss.level(), FxType.FT_COLLAPSE, p.add(0, 1.2, 0)).spawn();
        FxEntity.at(boss.level(), FxType.FT_BROKEN, p.add(0, 3.2, 0)).spawn();
        boss.playSound(SoundEvents.GLASS_BREAK, 2.5f, 0.8f);
        boss.playSound(SoundEvents.ANVIL_LAND, 1.5f, 1.2f);
        Fx.shake(boss.level(), boss.position(), 0.9f);
        Fx.rockBurst(boss.level(), b.groundAt(boss.position()), 3f, 14);
        boss.progressShuffle();
        if (boss.getPhase() != DameFortunaEntity.DEATH) {
            b.breakArmor(200);
            b.stunFor(40);
        }
    }

    private void lose(@Nullable LivingEntity target) {
        DameFortunaEntity boss = b.boss;
        b.betting = false;
        Vec3 p = tower != null ? tower.position() : boss.position();
        FxEntity.at(boss.level(), FxType.FT_CROWN, p.add(0, 1.5, 0)).spawn();
        boss.playSound(SoundEvents.PLAYER_LEVELUP, 2.0f, 0.6f);
        boss.setHealth(boss.getMaxHealth() * FortunaBrain.RESET[round]);
        boss.setPhase(round * 2);
        if (target != null) b.chipRain(target, 12);
    }

    @Override
    public void stop() {
        super.stop();
        b.betting = false;
        if (tower != null && !tower.isRemoved()) tower.discard();
        tower = null;
    }
}
