# Meet Your Fight Plus

[Meet Your Fight](https://www.curseforge.com/minecraft/mc-mods/meet-your-fight) 的 Boss 增强附属，Minecraft 1.20.1 Forge（47.4.23，Java 17）。

- 沼牙：用「远古化石之饵」召唤强化版。合成材料为化石之饵、苔藓之牙、2 个海晶碎片和 4 根骨头。本体化石之饵仍然召唤原版沼牙。
- 敲钟人、命运夫人、罗莎琳：技能与像素特效重做。
- 设计说明见 `docs/设计文档-Boss技能与特效.md`。

## 构建

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-17.0.18'
.\gradlew.bat build
.\gradlew.bat runClient
```

沼牙特效贴图由 `python tools/swampjaw_fx.py` 生成（需要 Pillow）。

## 许可

All Rights Reserved。本模组不包含 Meet Your Fight 的代码与资源，运行时依赖本体。
