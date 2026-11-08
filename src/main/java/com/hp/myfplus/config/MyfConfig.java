package com.hp.myfplus.config;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;

public final class MyfConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final BossCfg SWAMPJAW;
    public static final BossCfg BELLRINGER;
    public static final BossCfg FORTUNA;
    public static final BossCfg ROSALYNE;

    public static final ForgeConfigSpec.IntValue BEAT_P1;
    public static final ForgeConfigSpec.IntValue BEAT_P2;
    public static final ForgeConfigSpec.DoubleValue ALL_IN;
    public static final ForgeConfigSpec.IntValue PARRY_WINDOW;
    public static final ForgeConfigSpec.IntValue PARRY_STUN;
    public static final ForgeConfigSpec.IntValue DUEL_RADIUS;

    public static final ForgeConfigSpec.DoubleValue FANG_CHANCE;
    public static final ForgeConfigSpec.IntValue FANG_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue FANG_BASE;
    public static final ForgeConfigSpec.DoubleValue FANG_SCALE;
    public static final ForgeConfigSpec.IntValue FANG_SLOW;
    public static final ForgeConfigSpec.IntValue BELL_HITS;
    public static final ForgeConfigSpec.DoubleValue BELL_RATIO;
    public static final ForgeConfigSpec.DoubleValue BELL_RADIUS;
    public static final ForgeConfigSpec.DoubleValue DIE_LOW;
    public static final ForgeConfigSpec.DoubleValue DIE_STEP;
    public static final ForgeConfigSpec.DoubleValue DIE_HIGH;
    public static final ForgeConfigSpec.DoubleValue DIE_LUCK;
    public static final ForgeConfigSpec.IntValue CLOAK_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue CLOAK_BASE;
    public static final ForgeConfigSpec.DoubleValue CLOAK_SCALE;
    public static final ForgeConfigSpec.IntValue CLOAK_STUN;

    public static final ForgeConfigSpec.ConfigValue<Double> WARN_CELL;
    public static final ForgeConfigSpec.BooleanValue SCREEN_SHAKE;
    public static final ForgeConfigSpec.BooleanValue BOSS_FOG;
    public static final ForgeConfigSpec.BooleanValue PHASE_BANNER;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        SWAMPJAW = new BossCfg(b, "swampjaw", "沼牙", "Swampjaw");
        BELLRINGER = new BossCfg(b, "bellringer", "敲钟人", "Bellringer");
        b.push("bellringer");
        BEAT_P1 = b.comment("第一阶段节拍间隔（tick）", "Beat interval in phase 1 (ticks)").defineInRange("beatIntervalPhase1", 20, 8, 60);
        BEAT_P2 = b.comment("第二阶段节拍间隔（tick）", "Beat interval in phase 2 (ticks)").defineInRange("beatIntervalPhase2", 14, 6, 60);
        b.pop();
        FORTUNA = new BossCfg(b, "dame_fortuna", "命运夫人", "Dame Fortuna");
        b.push("dame_fortuna");
        ALL_IN = b.comment("转入二阶段时梭哈需要的伤害（最大生命比例）", "Damage needed to win the phase-transition All-In (fraction of max health)").defineInRange("allInDamage", 0.10, 0.01, 1.0);
        b.pop();
        ROSALYNE = new BossCfg(b, "rosalyne", "罗莎琳", "Rosalyne");
        b.push("rosalyne");
        PARRY_WINDOW = b.comment("凋花架势持续时间（tick）", "Withering Stance duration (ticks)").defineInRange("parryWindow", 40, 10, 200);
        PARRY_STUN = b.comment("近战攻击架势中的罗莎琳被格挡后的眩晕时长（tick），0 为不眩晕", "Stun duration (ticks) for attackers whose melee hit is parried by Rosalyne, 0 to disable").defineInRange("parryStunTicks", 40, 0, 200);
        DUEL_RADIUS = b.comment("荆棘决斗场半径（格）", "Thorn duel arena radius (blocks)").defineInRange("duelArenaRadius", 9, 5, 24);
        b.pop();
        b.push("accessories");
        FANG_CHANCE = b.comment("化石獠牙手镯：近战命中召唤骨刺的几率", "Fossil Fang Bracelet: chance for a melee hit to call a bone spike").defineInRange("fangChance", 0.25, 0.0, 1.0);
        FANG_COOLDOWN = b.comment("化石獠牙手镯：触发冷却（tick）", "Fossil Fang Bracelet: cooldown (ticks)").defineInRange("fangCooldown", 20, 0, 1200);
        FANG_BASE = b.comment("化石獠牙手镯：骨刺基础伤害", "Fossil Fang Bracelet: spike base damage").defineInRange("fangBaseDamage", 3.0, 0.0, 1000.0);
        FANG_SCALE = b.comment("化石獠牙手镯：骨刺额外伤害占佩戴者攻击力的比例", "Fossil Fang Bracelet: extra spike damage as a fraction of the wearer attack damage").defineInRange("fangAttackScale", 0.4, 0.0, 10.0);
        FANG_SLOW = b.comment("化石獠牙手镯：缓慢 II 持续时间（tick）", "Fossil Fang Bracelet: Slowness II duration (ticks)").defineInRange("fangSlowTicks", 40, 0, 1200);
        BELL_HITS = b.comment("共鸣腰铃：每第几次命中触发共鸣", "Resonant Belt Bell: hits needed to trigger a resonance").defineInRange("bellHits", 4, 2, 20);
        BELL_RATIO = b.comment("共鸣腰铃：共鸣伤害占触发那一击伤害的比例", "Resonant Belt Bell: resonance damage as a fraction of the triggering hit").defineInRange("bellDamageRatio", 0.5, 0.0, 10.0);
        BELL_RADIUS = b.comment("共鸣腰铃：共鸣半径（格）", "Resonant Belt Bell: resonance radius (blocks)").defineInRange("bellRadius", 3.0, 1.0, 16.0);
        DIE_LOW = b.comment("庄家骰戒：掷出 1 点时的伤害倍率", "House Die Ring: damage multiplier on a roll of 1").defineInRange("dieOneMultiplier", 0.5, 0.0, 10.0);
        DIE_STEP = b.comment("庄家骰戒：掷出 2~5 点时每点增加的伤害比例", "House Die Ring: extra damage per pip on rolls of 2 to 5").defineInRange("diePipBonus", 0.05, 0.0, 1.0);
        DIE_HIGH = b.comment("庄家骰戒：掷出 6 点时的伤害倍率", "House Die Ring: damage multiplier on a roll of 6").defineInRange("dieSixMultiplier", 2.0, 0.0, 10.0);
        DIE_LUCK = b.comment("庄家骰戒：额外幸运", "House Die Ring: bonus luck").defineInRange("dieLuck", 1.0, 0.0, 100.0);
        CLOAK_COOLDOWN = b.comment("落日玫瑰披风：格挡冷却（tick）", "Sunset Rose Cloak: parry cooldown (ticks)").defineInRange("cloakCooldown", 240, 20, 12000);
        CLOAK_BASE = b.comment("落日玫瑰披风：反击基础伤害", "Sunset Rose Cloak: counter base damage").defineInRange("cloakBaseDamage", 2.0, 0.0, 1000.0);
        CLOAK_SCALE = b.comment("落日玫瑰披风：反击额外伤害占佩戴者攻击力的比例", "Sunset Rose Cloak: extra counter damage as a fraction of the wearer attack damage").defineInRange("cloakAttackScale", 1.0, 0.0, 10.0);
        CLOAK_STUN = b.comment("落日玫瑰披风：反击眩晕时长（tick），对 Boss 无效", "Sunset Rose Cloak: counter stun duration (ticks), no effect on bosses").defineInRange("cloakStunTicks", 30, 0, 1200);
        b.pop();
        COMMON_SPEC = b.build();

        ForgeConfigSpec.Builder c = new ForgeConfigSpec.Builder();
        WARN_CELL = c.comment("预警格子边长（格），可选 0.25 或 0.5", "Warning grid cell size in blocks, 0.25 or 0.5").defineInList("warningCellSize", 0.25, List.of(0.25, 0.5));
        SCREEN_SHAKE = c.comment("Boss 重击时是否震动镜头", "Shake the camera on heavy boss hits").define("screenShake", true);
        BOSS_FOG = c.comment("Boss 特定阶段是否改变雾气", "Let certain boss phases change the fog").define("bossFog", true);
        PHASE_BANNER = c.comment("是否显示 Boss 阶段横幅", "Show boss phase banners").define("phaseBanner", true);
        CLIENT_SPEC = c.build();
    }

    private MyfConfig() {
    }

    public static final class BossCfg {
        private final ForgeConfigSpec.BooleanValue enabled;
        private final ForgeConfigSpec.DoubleValue damage;
        private final ForgeConfigSpec.DoubleValue cooldown;

        BossCfg(ForgeConfigSpec.Builder b, String key, String zh, String en) {
            b.push(key);
            enabled = b.comment("是否启用" + zh + "的技能重做，关闭后恢复本体技能", "Enable the " + en + " skill rework; disable to restore the original skills").define("enableRework", true);
            damage = b.comment(zh + "技能伤害倍率", en + " skill damage multiplier").defineInRange("skillDamageMultiplier", 1.0, 0.0, 100.0);
            cooldown = b.comment(zh + "技能冷却倍率", en + " skill cooldown multiplier").defineInRange("cooldownMultiplier", 1.0, 0.1, 10.0);
            b.pop();
        }

        public boolean enabled() {
            return COMMON_SPEC.isLoaded() ? enabled.get() : true;
        }

        public double damage() {
            return COMMON_SPEC.isLoaded() ? damage.get() : 1.0;
        }

        public double cooldown() {
            return COMMON_SPEC.isLoaded() ? cooldown.get() : 1.0;
        }
    }
}
