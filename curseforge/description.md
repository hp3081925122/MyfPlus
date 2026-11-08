# Meet Your Fight Plus

An addon for **Meet Your Fight** that adds enhanced versions of its four bosses. Each one has a new moveset and its own pixel-art effects. The original bosses are not changed: their summon items still summon the normal versions, and the enhanced versions use new summon items crafted from the original bosses' drops.

Requires **Meet Your Fight** and **Curios API**.

## Enhanced bosses

Every enhanced boss has two phases. The first lethal hit in phase 1 refills its health and plays a transition, and it only dies when phase 2 is emptied. Every damaging skill has a visible telegraph, and every random result (dice, roulette, slots) is shown to you before it takes effect.

### Ancient Swampjaw

A fossil predator that treats the ground like water.

*   Dives into the ground and swims toward you under a wave of blocks, then bursts out and leaps at you.
*   Fossil jaws erupt under your feet and snap shut. Spits a volley of fossil spikes that rain down on marked spots.
*   Spinning fin blades, a double tail sweep and, in phase 2, a ring of fangs that closes on you.

### Resonant Bellringer

A rhythm boss: the bell sets the beat and every attack lands on a beat.

*   Bell waves, echo marks that explode one beat later, and a death knell counting down above your head.
*   Scale Quake: lines of sound pillars erupt one per beat.
*   Belfry Resonance transition and a final grand toll in phase 2.

### High-Roller Dame Fortuna

The house always shows you its hand.

*   Loaded dice, roulette, a slot machine, chip fans and a Royal Flush finisher.
*   All-In replaces the original card shuffle as her phase transition.

### Rosalyne, Blade of Sunset

A precise melee duelist themed around thorns and sunset.

*   Three-hit dusk blade combo, thorn corridors, a parry stance that stuns careless attackers, and sunset slashes.
*   Corolla and thorn dueling arena transitions, then the Final Waltz in phase 2.

## Summon items

Crafted only from the original bosses' drops, so you have to beat each original boss first (at least twice).

*   Ancient Fossil Bait → Ancient Swampjaw
*   Resonant Haunted Bell → Resonant Bellringer
*   All-In Devil's Ante → High-Roller Dame Fortuna
*   Sunset Dusk Key → Rosalyne, Blade of Sunset

## Drops and accessories

Enhanced bosses drop everything the original boss drops, plus one exclusive trophy: Ancient Jawbone, Resonant Clapper, House's Gilded Die or Sunset Rose. Each trophy crafts a Curios accessory:

*   **Fossil Fang Bracelet** (bracelet): melee hits can drop a fossil spike on the target, dealing damage and Slowness II.
*   **Resonant Belt Bell** (belt): every 4th hit releases a resonance that damages nearby enemies.
*   **House Die Ring** (ring): rolls a die on every hit. A 1 halves the damage, 2 to 5 add a little, a 6 doubles it. Also gives +1 Luck.
*   **Sunset Rose Cloak** (back): blocks one melee hit, counters the attacker and stuns non-boss mobs. Has a cooldown.

## Config

`config/myfplus-common.toml`: each boss's rework can be turned off (restoring its original skills), and you can change skill damage and cooldown multipliers, several skill parameters (beat intervals, parry window, arena radius and more) and every accessory value. `config/myfplus-client.toml` toggles screen shake, boss fog and phase banners.

## License and source

All Rights Reserved. This mod does not contain Meet Your Fight's code or assets; it needs the original mod at runtime.

Source: https://github.com/hp3081925122/MyfPlus

---

# 迎战增强

**Meet Your Fight（迎战）** 的附属模组。它为本体的四个 Boss 各加了一个强化版，每个强化版都有全新的技能组和专属像素特效。本体 Boss 保持原样，本体召唤物仍然召唤普通版。强化版要用新的召唤物召唤，这些召唤物用本体 Boss 的掉落物合成。

前置：**Meet Your Fight**、**Curios API**。

## 强化 Boss

每个强化 Boss 都有两个阶段。一阶段第一次受到致命伤时会回满血并播放转阶段演出，二阶段血量打空才会死亡。所有造成伤害的技能都有可见的前摇提示。所有随机结果（骰子、轮盘、老虎机）都会在生效前先展示给玩家。

### 远古沼泽巨颌怪

把地面当作水域的化石掠食者。

*   钻进地里，顶着方块浪游向玩家，再破土跃起扑击。
*   化石巨颌从玩家脚下破土咬合；吐出一簇化石骨刺，砸向事先标出的落点。
*   回旋骨鳍刃、连续两次回身尾击；二阶段会展开獠牙环，向圆心合拢。

### 万钟敲钟人

节奏型 Boss：钟声定下节拍，所有攻击都在拍点上结算。

*   钟波；余音印记会在一拍后爆炸；丧钟会在玩家头顶倒计时。
*   音阶裂地：每一拍升起一排声波柱。
*   转阶段为钟楼共振，二阶段有万钟归一大招。

### 豪赌福尔图娜女爵

庄家的每一手牌都摊开给你看。

*   灌铅骰子、轮盘、老虎机、筹码扇射，以及皇家同花顺终结技。
*   转阶段改为梭哈，取代本体的洗牌找牌。

### 落日之锋，罗莎琳

以荆棘与落日为主题的精准近战决斗者。

*   暮刃三段、荆棘回廊、凋花架势（格挡反击，会眩晕无脑平 A 的玩家）、落日斩。
*   转阶段依次是花冠和荆棘决斗场，二阶段有终幕圆舞。

## 召唤物

召唤物只能用本体 Boss 的掉落物合成，所以每个本体 Boss 至少要先打两次。

*   远古化石之饵 → 远古沼泽巨颌怪
*   共鸣缠魂铃铛 → 万钟敲钟人
*   梭哈恶魔赌注 → 豪赌福尔图娜女爵
*   落日黄昏之钥 → 落日之锋，罗莎琳

## 掉落与饰品

强化 Boss 会掉落本体 Boss 的全部掉落物，另外固定掉落 1 个专属物品：远古颌骨、共鸣钟舌、庄家金骰或落日玫瑰。专属物品可以合成对应的 Curios 饰品：

*   **化石獠牙手镯**（手镯）：近战命中时有几率在目标头上落下化石骨刺，造成伤害并附加缓慢 II。
*   **共鸣腰铃**（腰带）：每第 4 次命中触发共鸣，伤害周围的敌人。
*   **庄家骰戒**（戒指）：每次造成伤害时掷骰。1 点伤害减半，2~5 点小幅加伤，6 点伤害翻倍。另外幸运 +1。
*   **落日玫瑰披风**（背部）：格挡一次近战攻击并反击，非 Boss 生物会被眩晕。有冷却时间。

## 配置

`config/myfplus-common.toml`：可以单独关闭每个 Boss 的技能重做（恢复本体技能），也可以调整技能伤害倍率、冷却倍率、部分技能参数（节拍间隔、架势时长、决斗场半径等）以及所有饰品数值。`config/myfplus-client.toml` 可以开关镜头震动、Boss 雾气和阶段横幅。

## 许可与源码

All Rights Reserved。本模组不包含 Meet Your Fight 的代码和资源，运行时需要安装本体。

源码：https://github.com/hp3081925122/MyfPlus
