<div dir=rtl align=center>

### [**English 🇺🇸**](README.md) / [**Русский 🇷🇺**](README_ru.md) / **简体中文 🇨🇳** / [**한국어 🇰🇷**](README_kr.md) / [**Українська 🇺🇦**](README_ua.md)
</div>

<p align="center"><img src="./github/icon.png" alt="Logo" width="300"></p>

<h1 align="center"> HBM的核科技 社区版 <br>
	<a href="https://www.curseforge.com/minecraft/mc-mods/hbm-nuclear-tech-mod-community-edition"><img src="http://cf.way2muchnoise.eu/1312314.svg" alt="CF"></a>
    <a href="https://modrinth.com/mod/ntm-ce"><img src="https://img.shields.io/modrinth/dt/ntm-ce?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
    <a href="https://www.mcmod.cn/class/21023.html"><img src="https://img.shields.io/badge/MC-%E7%99%BE%E7%A7%91-58B6D8?style=flat&labelColor=86C155" alt="MC百科"></a>
    <a href="https://discord.gg/eKFrH7P5ZR"><img src="https://img.shields.io/discord/1241479482964054057?color=5865f2&label=Discord&style=flat" alt="Discord"></a>
    <a href="https://qm.qq.com/q/xbjhxWXxYc"><img src="https://img.shields.io/badge/QQ群-515151?style=flat&logo=qq&logoColor=white" alt="QQ"></a>
<br>
</h1>

这是一个HBM的核科技mod的1.12.2**权威移植**，在所有移植中完成度最高，因其他开发者未能持续更新与维护其它fork而生。

> **注意：提交问题（Issue）时请严格遵循模板**  
> 鉴于我们每日收到的问题数量，我们**强制**执行模板中规定的报告规范。  
未按模板提交的问题将会**直接关闭并锁定**。该规则**不追溯既往**。请尊重我们的时间，使用英文提交**高质量**的问题报告。

> **注意：如安装了 Universal Tweaks，请将 `B:"Disable Fancy Missing Model"` 设为 `false` 以修复模型旋转问题**  
> 配置位置：`config/Universal Tweaks - Tweaks.cfg`

<br>
<p align="center"><img src="./github/faq.png" alt="NTM:CE FAQ" width="700"></p>
<br>

### 现在能正常生存吗？

可以正常进行游戏而**不会发生致命崩溃**。小缺陷是存在的，也还有些内容尚待移植。

### 与 NTM: Extended Edition 的附属mod / 光影兼容吗？

很遗憾，**不兼容**。安装Ex版的附属大概率会导致崩溃，从而使整合包无法游玩；此前移植的新枪械系统和光影**不兼容**，在持枪时可见严重伪影。  
此外，光影也与NTM的天空盒材质（Skybox）不兼容；可在 `config/hbm -> hbm.cfg` 中将 `B:1.00_enableSkybox=true` 改为 `false` 解决。  
我们计划修复与光影相关的问题，但这需要时间。

### 与 Extended Edition 有多大差异？

与Ex版存档**完全不兼容！**  
我们已经重写了**约 75%** 的整体代码，尽可能移植每一个特性。变化之多已难以一一罗列。欢迎查看我们的 Issue 区，我们用它来追踪缺失内容。

### 为什么不直接改进 Extended Edition？

Alcater 在 CurseForge 上的版本已**超过 1.5 年**没有更新。他的版本存在严重的性能瓶颈和怪异的逻辑实现，更不用说他拒绝与我们合作。因此我们选择fork并独立开发。

### 既然还在开发中，为什么要上 CurseForge？

**我们需要 Bug 反馈。**  
如果没有 CurseForge、Modrinth 这样的平台提供的公开渠道，我们的曝光度将**大幅降低**。我们希望玩家知道：有一个**正规的移植**正在进行中，并可以通过**提交 Issue**或**直接发起 Pull Request** 来帮助我们。我们**始终欢迎新贡献者**。

### 是否会针对某些整合包做定制改动？
**不会！**  
虽然该移植最初起源于 WarFactory 项目，但目前作为**独立模组**维护。任何改动都仅为确保**兼容性、稳定性**，或**方便整合包作者**的开发；不会为特定整合包做直接定制。

### 会移植到 1.1x/1.2x 吗？

**我们目前没有这个计划。**  
我们需要**专注于一个版本**。碎片化，以及曾经众多团队的各自为政，是导致本模组此前的多次移植版本失败的关键原因。因此我们希望**集中力量**先把一个版本做好。

<br>
<p align="center"><img src="./github/dev_guide.png" alt="Development Guide" width="700"></p>
<br>

## **开发环境使用 Java 25！**

我们使用 [JvmDowngrader](https://github.com/unimined/JvmDowngrader) 来在使用现代 Java 语法与 API 的同时，无缝地生成面向 1.12.2 的 Java 8 字节码。

### 快速上手

1. 克隆此仓库
2. 准备 **JDK 25**
3. 运行任务 `setupDecompWorkspace`（会完成工作区初始化与 MC 源码的反编译/反混淆）
4. 确认一切正常后，运行任务 `runClient`（应启动 Minecraft 客户端）

* 请**始终**使用 `gradlew`（Linux/macOS）或 `gradlew.bat`（Windows）来执行任务，而不是系统的 `gradle`，以确保所有开发者环境一致。

### Apple M 系列机器的开发注意事项

由于当前**没有 ARM 架构**的本地库（natives），因此你需要使用 **x86_64 JDK**（最简单的方式是通过 IntelliJ 的 SDK 管理器获取）。

你可以使用以下任一方法指定 JDK：

- 设置环境变量 `GRADLE_OPTS`：  
  `export GRADLE_OPTS="-Dorg.gradle.java.home=/path/to/your/desired/jdk"`
- 在 `gradle.properties`（`~/.gradle` 或项目目录）中添加属性：  
  `org.gradle.java.home=/path/to/your/desired/jdk`
- 在终端直接使用 `-D` 参数：  
  `./gradlew -Dorg.gradle.java.home=/path/to/your/desired/jdk wantedTask`

#### 故障排查

1. 如果尽管你使用了 x86_64 JDK，但日志中 Gradle 仍将你的机器识别为 ARM，请按以下步骤操作：
    1) 清理工作区：`git fetch; git clean -fdx; git reset --hard HEAD`  
       （**注意**：会将本地同步到远端状态，并**删除所有本地改动**）
    2) 清理 Gradle 缓存：`rm -rf ~/.gradle`  
       （**注意**：会**清空整个** Gradle 缓存）
    3) 清理下载的 JVM：`rm -rf /path/to/used/jvm`  
       （已用 JVM 的路径可在 `/run/logs/latest.log` 中找到，如：  
       `Java is OpenJDK 64-Bit Server VM, version 1.8.0_442, running on Mac OS X:x86_64:15.3.2, installed at /this/is/the/path`）
    4) 重新执行“快速上手”步骤。
