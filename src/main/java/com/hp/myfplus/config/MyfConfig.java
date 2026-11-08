package com.hp.myfplus.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class MyfConfig {
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final BossCfg SWAMPJAW;
    public static final BossCfg BELLRINGER;
    public static final BossCfg FORTUNA;
    public static final BossCfg ROSALYNE;

    public static final ForgeConfigSpec.DoubleValue FANG_CHANCE;
    public static final ForgeConfigSpec.IntValue FANG_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue FANG_BASE;
    public static final ForgeConfigSpec.DoubleValue FANG_SCALE;
    public static final ForgeConfigSpec.DoubleValue FANG_RADIUS;
    public static final ForgeConfigSpec.IntValue BELL_HITS;
    public static final ForgeConfigSpec.DoubleValue BELL_RATIO;
    public static final ForgeConfigSpec.DoubleValue DIE_CHANCE;
    public static final ForgeConfigSpec.IntValue DIE_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue DIE_BASE;
    public static final ForgeConfigSpec.DoubleValue DIE_SCALE;
    public static final ForgeConfigSpec.DoubleValue DIE_LUCK;
    public static final ForgeConfigSpec.IntValue CLOAK_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue CLOAK_BASE;
    public static final ForgeConfigSpec.DoubleValue CLOAK_SCALE;
    public static final ForgeConfigSpec.IntValue CLOAK_STUN;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        SWAMPJAW = new BossCfg(b, "swampjaw", "远古沼泽巨颌怪", "Ancient Swampjaw");
        BELLRINGER = new BossCfg(b, "bellringer", "万钟敲钟人", "Resonant Bellringer");
        FORTUNA = new BossCfg(b, "dame_fortuna", "豪赌福尔图娜女爵", "High-Roller Dame Fortuna");
        ROSALYNE = new BossCfg(b, "rosalyne", "落日之锋罗莎琳", "Rosalyne, Blade of Sunset");
        b.push("accessories");
        FANG_CHANCE = b.comment("化石獠牙手镯：近战命中放置水雷的几率", "Fossil Fang Bracelet: chance for a melee hit to plant a mine").defineInRange("fangChance", 0.2, 0.0, 1.0);
        FANG_COOLDOWN = b.comment("化石獠牙手镯：触发冷却（tick）", "Fossil Fang Bracelet: cooldown (ticks)").defineInRange("fangCooldown", 40, 0, 1200);
        FANG_BASE = b.comment("化石獠牙手镯：水雷基础伤害", "Fossil Fang Bracelet: mine base damage").defineInRange("fangBaseDamage", 3.0, 0.0, 1000.0);
        FANG_SCALE = b.comment("化石獠牙手镯：水雷额外伤害占佩戴者攻击力的比例", "Fossil Fang Bracelet: extra mine damage as a fraction of the wearer attack damage").defineInRange("fangAttackScale", 0.5, 0.0, 10.0);
        FANG_RADIUS = b.comment("化石獠牙手镯：水雷爆炸半径（格）", "Fossil Fang Bracelet: mine blast radius (blocks)").defineInRange("fangRadius", 2.5, 0.5, 8.0);
        BELL_HITS = b.comment("共鸣腰铃：每第几次命中触发十字钟鸣", "Resonant Belt Bell: hits needed to trigger a cross toll").defineInRange("bellHits", 4, 2, 20);
        BELL_RATIO = b.comment("共鸣腰铃：每道幽灵线伤害占触发那一击伤害的比例", "Resonant Belt Bell: damage of each ghost line as a fraction of the triggering hit").defineInRange("bellDamageRatio", 0.3, 0.0, 10.0);
        DIE_CHANCE = b.comment("庄家骰戒：命中时掷出骰子炸弹的几率", "House Die Ring: chance to throw a dice bomb on hit").defineInRange("dieChance", 0.15, 0.0, 1.0);
        DIE_COOLDOWN = b.comment("庄家骰戒：触发冷却（tick）", "House Die Ring: cooldown (ticks)").defineInRange("dieCooldown", 60, 0, 1200);
        DIE_BASE = b.comment("庄家骰戒：每道筹码线基础伤害", "House Die Ring: base damage of each chip line").defineInRange("dieBaseDamage", 2.0, 0.0, 1000.0);
        DIE_SCALE = b.comment("庄家骰戒：每道筹码线额外伤害占佩戴者攻击力的比例", "House Die Ring: extra chip line damage as a fraction of the wearer attack damage").defineInRange("dieAttackScale", 0.3, 0.0, 10.0);
        DIE_LUCK = b.comment("庄家骰戒：额外幸运", "House Die Ring: bonus luck").defineInRange("dieLuck", 1.0, 0.0, 100.0);
        CLOAK_COOLDOWN = b.comment("落日玫瑰披风：触发冷却（tick）", "Sunset Rose Cloak: cooldown (ticks)").defineInRange("cloakCooldown", 200, 20, 12000);
        CLOAK_BASE = b.comment("落日玫瑰披风：每道玫瑰线基础伤害", "Sunset Rose Cloak: base damage of each rose line").defineInRange("cloakBaseDamage", 2.0, 0.0, 1000.0);
        CLOAK_SCALE = b.comment("落日玫瑰披风：每道玫瑰线额外伤害占佩戴者攻击力的比例", "Sunset Rose Cloak: extra rose line damage as a fraction of the wearer attack damage").defineInRange("cloakAttackScale", 0.5, 0.0, 10.0);
        CLOAK_STUN = b.comment("落日玫瑰披风：命中后的眩晕时长（tick），对 Boss 无效", "Sunset Rose Cloak: stun duration on hit (ticks), no effect on bosses").defineInRange("cloakStunTicks", 20, 0, 1200);
        b.pop();
        COMMON_SPEC = b.build();
    }

    private MyfConfig() {
    }

    public static final class BossCfg {
        private final ForgeConfigSpec.DoubleValue health;
        private final ForgeConfigSpec.DoubleValue attack;
        private final ForgeConfigSpec.DoubleValue damage;
        private final ForgeConfigSpec.DoubleValue cooldown;
        private final ForgeConfigSpec.DoubleValue chance;
        private final ForgeConfigSpec.BooleanValue phase2;

        BossCfg(ForgeConfigSpec.Builder b, String key, String zh, String en) {
            b.push(key);
            health = b.comment(zh + "生命倍率（只在生成时应用一次）", en + " max health multiplier (applied once on spawn)").defineInRange("healthMultiplier", 1.0, 0.01, 1000.0);
            attack = b.comment(zh + "攻击力倍率（只在生成时应用一次）", en + " attack damage multiplier (applied once on spawn)").defineInRange("attackMultiplier", 1.0, 0.0, 1000.0);
            damage = b.comment(zh + "弹幕与新技能伤害倍率", en + " projectile and new skill damage multiplier").defineInRange("skillDamageMultiplier", 1.0, 0.0, 100.0);
            cooldown = b.comment(zh + "攻击冷却倍率，越小攻击越频繁", en + " attack cooldown multiplier, lower attacks more often").defineInRange("cooldownMultiplier", 1.0, 0.1, 10.0);
            chance = b.comment(zh + "每次攻击改放新技能的几率", en + " chance for each attack to be a new skill").defineInRange("newSkillChance", 0.4, 0.0, 1.0);
            phase2 = b.comment(zh + "生命耗尽时是否进入二阶段", en + " enters phase 2 when its health runs out").define("enablePhase2", true);
            b.pop();
        }

        public double health() {
            return COMMON_SPEC.isLoaded() ? health.get() : 1.0;
        }

        public double attack() {
            return COMMON_SPEC.isLoaded() ? attack.get() : 1.0;
        }

        public double damage() {
            return COMMON_SPEC.isLoaded() ? damage.get() : 1.0;
        }

        public double cooldown() {
            return COMMON_SPEC.isLoaded() ? cooldown.get() : 1.0;
        }

        public double chance() {
            return COMMON_SPEC.isLoaded() ? chance.get() : 0.4;
        }

        public boolean phase2() {
            return !COMMON_SPEC.isLoaded() || phase2.get();
        }
    }
}
