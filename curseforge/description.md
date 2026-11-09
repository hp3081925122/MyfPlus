# Meet Your Fight Plus

An addon for **Meet Your Fight** that adds enhanced versions of its four bosses. Each enhanced boss keeps all of the original boss's attacks and phases, attacks more often, chases you harder and adds new skills built from Meet Your Fight's own projectiles and effects. The original bosses are not changed: their summon items still summon the normal versions, and the enhanced versions use new summon items crafted from the original bosses' drops.

Requires **Meet Your Fight** and **Curios API**.

## Enhanced bosses

Every enhanced boss has two phases. When its health runs out in phase 1, it refills its health and enters phase 2, where it attacks even more often and unlocks extra skills. It only dies when phase 2 is emptied. Dame Fortuna is the exception: see below. Skill damage scales with the boss's attack damage.

### Ancient Swampjaw

*   Faster swoops, fewer bombs between swoops and a shorter stun.
*   Rebite: after a swoop it can turn around and swoop again. Mud Spit: lobs mines at where you are and where you are going. Tail Sweep: punishes you for standing close.
*   Phase 2: Mire Vortex (circles above you dropping a ring of mines, then swoops) and Foul Rain (mines fall on marked spots).

### Resonant Bellringer

*   Follows you faster, attacks more times per round with shorter gaps.
*   Cross Toll, Bell Ring and Echo Toll: new ghost line patterns aimed at you.
*   Phase 2 starts with its rage attack, then adds Pendulum (a sweeping wall of lines) and Carillon (a checkerboard of falling lines).

### High-Roller Dame Fortuna

*   Twice the original health. Throws more dice, spins faster and scatters chips more often. All three card shuffles are kept; guessing the first shuffle correctly sends her into phase 2.
*   Pincer Dice, Chip Rain and Raise Dash (two fast passes leaving dice bombs behind).
*   Phase 2: Royal Flush (a grid of homing chips) and All-In (a ring of dice bombs).

### Rosalyne, Blade of Sunset

*   Approaches faster and swings more often. The seal, rose spirits and frenzy are kept.
*   Rose Fan (a fan of rose ghost lines) and Blink Strike (reappears behind you and slashes).
*   Phase 2: more dash circles, a Rose Fan after every crash, Sunset Ring (a ring of rose lines followed by a crash) and Corolla (summons two rose spirits to help her).

## Summon items

Crafted only from the original bosses' drops, so you have to beat each original boss first (at least twice).

*   Ancient Fossil Bait → Ancient Swampjaw
*   Resonant Haunted Bell → Resonant Bellringer
*   All-In Devil's Ante → High-Roller Dame Fortuna
*   Sunset Dusk Key → Rosalyne, Blade of Sunset

## Drops and accessories

Enhanced bosses drop everything the original boss drops, plus one exclusive trophy: Ancient Jawbone, Resonant Clapper, House's Gilded Die or Sunset Rose. Each trophy crafts a Curios accessory based on that boss's new skills:

*   **Fossil Fang Bracelet** (bracelet): melee hits can plant a mine under the target that soon explodes.
*   **Resonant Belt Bell** (belt): every 4th consecutive hit fires Bellringer ghost lines at the target from four sides.
*   **House Die Ring** (ring): hits can throw a Fortuna dice bomb at the target. Also gives +1 Luck.
*   **Sunset Rose Cloak** (back): when you are hit in melee, fires a fan of rose ghost lines at the attacker and stuns non-boss mobs. Has a cooldown. The lines cannot hit Rosalyne or rose spirits.

Accessory projectiles never hit you, other players or your pets.

## Config

`config/myfplus-common.toml`: for each boss, health and attack multipliers (applied once when it spawns), skill damage multiplier, cooldown multiplier, the chance for each attack to be a new skill, and whether it has phase 2. Every accessory value is also configurable.

## License and source

All Rights Reserved. This mod does not contain Meet Your Fight's code or assets; it needs the original mod at runtime.

Source: https://github.com/hp3081925122/MyfPlus

---

# 迎战增强

**Meet Your Fight（迎战）** 的附属模组。它为本体的四个 Boss 各加了一个强化版。强化版保留本体 Boss 的全部招式和阶段流程，攻击更频繁、追击更紧，并加入用迎战本体弹幕和特效做成的新技能。本体 Boss 保持原样，本体召唤物仍然召唤普通版。强化版要用新的召唤物召唤，这些召唤物用本体 Boss 的掉落物合成。

前置：**Meet Your Fight**、**Curios API**。

## 强化 Boss

每个强化 Boss 都有两个阶段。一阶段生命耗尽时会回满血进入二阶段，攻击更频繁并解锁额外技能，二阶段血量打空才会死亡。命运女爵例外，见下文。技能伤害随 Boss 攻击力变化。

### 远古沼泽巨颌怪

*   俯冲更快，两次俯冲之间投的水雷更少，被打晕的时间更短。
*   连环回咬：俯冲后可能立刻掉头再冲一次。泥沼喷吐：朝你的位置和预判位置抛水雷。甩尾横扫：贴身时甩尾。
*   二阶段：泥沼漩涡（在你头顶绕圈投下一圈水雷后俯冲）、腐水暴雨（水雷砸向预先标出的落点）。

### 万钟敲钟人

*   追身更快，每轮攻击次数更多、间隔更短。
*   十字钟鸣、钟环、回响追钟：新的幽灵线攻击。
*   二阶段一开始就触发狂暴攻击，之后加入钟摆（来回扫过的幽灵线墙）和万钟齐鸣（棋盘格天降线）。

### 豪赌福尔图娜女爵

*   血量是本体的两倍。骰子更多、旋转更快、撒筹码更密。保留本体全部三次洗牌找牌，第一次猜对就进入二阶段。
*   夹击骰、筹码雨、加注冲刺（两次高速穿过你并沿路留下骰子炸弹）。
*   二阶段：同花顺（一排排追踪筹码）、梭哈（一圈骰子炸弹）。

### 落日之锋，罗莎琳

*   接近更快、挥砍更勤。保留封印、玫瑰灵和狂乱。
*   玫瑰扇刃（扇形玫瑰幽灵线）、瞬步斩（出现在你背后挥砍）。
*   二阶段：绕圈冲刺次数更多，每次下劈后接玫瑰扇刃；落日环刃（一圈玫瑰幽灵线后接下劈）；花冠再临（召唤两只玫瑰灵助战）。

## 召唤物

召唤物只能用本体 Boss 的掉落物合成，所以每个本体 Boss 至少要先打两次。

*   远古化石之饵 → 远古沼泽巨颌怪
*   共鸣缠魂铃铛 → 万钟敲钟人
*   梭哈恶魔赌注 → 豪赌福尔图娜女爵
*   落日黄昏之钥 → 落日之锋，罗莎琳

## 掉落与饰品

强化 Boss 会掉落本体 Boss 的全部掉落物，另外固定掉落 1 个专属物品：远古颌骨、共鸣钟舌、庄家金骰或落日玫瑰。专属物品可以合成对应的 Curios 饰品，效果来自该 Boss 的新技能：

*   **化石獠牙手镯**（手镯）：近战命中时有几率在目标脚下埋下水雷，片刻后爆炸。
*   **共鸣腰铃**（腰带）：连续第 4 次命中时，从目标四个方向射出敲钟人幽灵线。
*   **庄家骰戒**（戒指）：命中时有几率向目标掷出命运骰子炸弹。另外幸运 +1。
*   **落日玫瑰披风**（背部）：受到近战攻击时向攻击者扇形射出玫瑰幽灵线，非 Boss 生物会被眩晕。有冷却时间。幽灵线打不中罗莎琳和玫瑰灵。

饰品弹幕不会打中自己、其他玩家和自己的宠物。

## 配置

`config/myfplus-common.toml`：每个 Boss 都可以调生命和攻击倍率（只在生成时应用一次）、技能伤害倍率、冷却倍率、每次攻击改放新技能的几率，以及是否启用二阶段。所有饰品数值也可以配置。

## 许可与源码

All Rights Reserved。本模组不包含 Meet Your Fight 的代码和资源，运行时需要安装本体。

源码：https://github.com/hp3081925122/MyfPlus
