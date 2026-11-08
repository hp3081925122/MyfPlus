# Meet Your Fight Plus

[Meet Your Fight](https://www.curseforge.com/minecraft/mc-mods/meet-your-fight) 的 Boss 增强附属，Minecraft 1.20.1 Forge（47.4.23，Java 17）。

- 为四个本体 Boss 各加入强化版（远古沼泽巨颌怪、万钟敲钟人、豪赌福尔图娜女爵、落日之锋罗莎琳）；本体 Boss 不受影响。
- `1201` 分支：强化 Boss 保留本体的全部 AI 与阶段流程，在其上提高攻击频率与追击能力，并追加新技能；生命耗尽时进入二阶段，再次加强并解锁二阶段技能。新技能特效全部使用本体已有的弹幕与粒子。
- `1.20.1-forge` 分支：技能与像素特效全部重做的旧版本。
- 强化版召唤物只用本体 Boss 掉落物合成；强化 Boss 额外掉落专属物品，可合成 4 件 Curios 饰品。
- 前置：Meet Your Fight、Curios API。
- 设计说明见 `docs/设计文档-1201分支-原版逻辑强化.md`。

## 构建

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-17.0.18'
.\gradlew.bat build
.\gradlew.bat runClient
```

CurseForge 项目资料在 `curseforge/`。

## 许可

All Rights Reserved。本模组不包含 Meet Your Fight 的代码与资源，运行时依赖本体。
