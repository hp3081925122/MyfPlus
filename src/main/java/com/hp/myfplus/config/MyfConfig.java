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
    public static final ForgeConfigSpec.DoubleValue ALL_IN_1;
    public static final ForgeConfigSpec.DoubleValue ALL_IN_2;
    public static final ForgeConfigSpec.DoubleValue ALL_IN_3;
    public static final ForgeConfigSpec.IntValue PARRY_WINDOW;
    public static final ForgeConfigSpec.IntValue DUEL_RADIUS;

    public static final ForgeConfigSpec.BooleanValue SCREEN_OVERLAY;
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
        ALL_IN_1 = b.comment("第一次梭哈需要的伤害（最大生命比例）", "Damage needed for the first All-In (fraction of max health)").defineInRange("allInDamage1", 0.10, 0.01, 1.0);
        ALL_IN_2 = b.comment("第二次梭哈需要的伤害（最大生命比例）", "Damage needed for the second All-In (fraction of max health)").defineInRange("allInDamage2", 0.12, 0.01, 1.0);
        ALL_IN_3 = b.comment("第三次梭哈需要的伤害（最大生命比例）", "Damage needed for the third All-In (fraction of max health)").defineInRange("allInDamage3", 0.15, 0.01, 1.0);
        b.pop();
        ROSALYNE = new BossCfg(b, "rosalyne", "罗莎琳", "Rosalyne");
        b.push("rosalyne");
        PARRY_WINDOW = b.comment("凋花架势持续时间（tick）", "Withering Stance duration (ticks)").defineInRange("parryWindow", 40, 10, 200);
        DUEL_RADIUS = b.comment("荆棘决斗场半径（格）", "Thorn duel arena radius (blocks)").defineInRange("duelArenaRadius", 9, 5, 24);
        b.pop();
        COMMON_SPEC = b.build();

        ForgeConfigSpec.Builder c = new ForgeConfigSpec.Builder();
        SCREEN_OVERLAY = c.comment("罗莎琳第三阶段是否显示暮色屏幕叠加", "Show the dusk screen overlay during Rosalyne's last phase").define("screenOverlay", true);
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
