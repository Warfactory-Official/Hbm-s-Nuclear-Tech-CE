<div dir=rtl align=center>

### **English 🇺🇸** / [**Русский 🇷🇺**](README_ru.md) / [**简体中文 🇨🇳**](README_zh.md) / [**한국어 🇰🇷**](README_kr.md) / [**Українська 🇺🇦**](README_ua.md)
</div>

<p align="center"><img src="./github/icon.png" alt="Logo" width="300"></p>

<h1 align="center"> HBM's Nuclear Tech Mod Community Edition  <br>
	<a href="https://www.curseforge.com/minecraft/mc-mods/hbm-nuclear-tech-mod-community-edition"><img src="http://cf.way2muchnoise.eu/1312314.svg" alt="CF"></a>
    <a href="https://modrinth.com/mod/ntm-ce"><img src="https://img.shields.io/modrinth/dt/ntm-ce?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
	<a href="https://discord.gg/eKFrH7P5ZR"><img src="https://img.shields.io/discord/1241479482964054057?color=5865f2&label=Discord&style=flat" alt="Discord"></a>
    <br>
</h1>

A definitive port of HBM's Nuclear Tech Mod from 1.7.10 to 1.12.2, the most completed one among others. Came from necessity as other developers have failed to update and maintain other forks.

> [!IMPORTANT]
> **FOLLOW THE ISSUE TEMPLATE WHILE REPORTING ISSUES**  
> Due to the amount of issues we get daily, we enforce strict issue report guidelines stated in the templates.  
Failure to follow the templates will result in closing and locking of the issue without a warning. Rule does not apply
retroactively. Please respect our time and make sure issue reports are of quality.

> [!NOTE]
> If you have Universal Tweaks installed, set `B:"Disable Fancy Missing Model"` to `false` to fix model rotation  
> This can be found at `config/Universal Tweaks - Tweaks.cfg`

<br>
<p align="center"><img src="./github/faq.png" alt="NTM:CE FAQ" width="700"></p>
<br>

### Is it survival ready?
Yes!
### Is the mod compatible with NTM: Extended edition addons?
No. It will crash.
### Shaders?
Should be compatible with most shaders; If you find any visual artifacts, please report them to us.
### How different is it from Extended edition?
**Extended worlds are fully incompatible!** <br>
We have rewritten ~75% of the entire mod, porting every single feature we can.
The amount of changes is difficult to track at this point. I invite you to check our GitHub issues, as we use them to
track missing/added content.

<br>
<p align="center"><img src="./github/dev_guide.png" alt="Development Guide" width="700"></p>
<br>

## **For development Java 25 is used!**

We have [JvmDowngrader](https://github.com/unimined/JvmDowngrader) to target Java 8 bytecode seamlessly while still using modern syntax and apis.


### General quickstart
1. Clone this repository.
2. Prepare JDK 25
3. Run task `setupDecompWorkspace` (this will setup workspace, including MC sources deobfuscation)
4. Ensure everything is OK. Run task `runClient` (should open minecraft client with mod loaded)


- Always use `gradlew` (Linux/MACOS) or `gradlew.bat` (Win) and not `gradle` for tasks. So each dev will have consistent environment.
### Development quirks for Apple M-chip machines.

Since there are no natives for ARM arch, therefore you will have to use x86_64 JDK (the easiest way to get the right one is IntelliJ SDK manager)

You can use one of the following methods:
- GRADLE_OPTS env variable `export GRADLE_OPTS="-Dorg.gradle.java.home=/path/to/your/desired/jdk"`
- additional property in gradle.properties (~/.gradle or pwd) `org.gradle.java.home=/path/to/your/desired/jdk`
- direct usage with -D param in terminal `./gradlew -Dorg.gradle.java.home=/path/to/your/desired/jdk wantedTask`

#### Troubleshooting:

1. If you see that even when using x86_64 JDK in logs gradle treats you as ARM machine. Do following:
    1. Clear workspace `git fetch; git clean -fdx; git reset --hard HEAD` (IMPORTANT: will sync local to git, and remove all progress)
    2. Clear gradle cache `rm -rf ~/.gradle` (IMPORTANT: will erase WHOLE gradle cache)
    3. Clear downloaded JVMs `rm -rf /path/to/used/jvm`
       (path to used jvm can be found in /run/logs/latest.log like this `Java is OpenJDK 64-Bit Server VM, version 1.8.0_442, running on Mac OS X:x86_64:15.3.2, installed at /this/is/the/path`)
    4. Repeat quickstart.

## Maven
Our server is extremely unreliable and slow, curse maven is recommended for releases.

### Snapshots
These represent the latest commit for a given version.

```groovy
repositories {
    maven {
        name "Warfactory Snapshots"
        url "https://repo.warfactory.co/snapshots"
    }
}
dependencies {
    // Java 8, unobfuscated
    implementation "com.hbm:ntm-ce:2.4.0.0-SNAPSHOT:dev"
    // Java 25, unobfuscated
    implementation "com.hbm:ntm-ce-java25:2.4.0.0-SNAPSHOT:dev"
    // Java 8, obfuscated
    implementation "com.hbm:ntm-ce:2.4.0.0-SNAPSHOT"
    // Java 25, obfuscated
    implementation "com.hbm:ntm-ce-java25:2.4.0.0-SNAPSHOT"
}
```

### Releases
These correspond to a CurseForge / Modrinth release.

```groovy
repositories {
    maven {
        name "Warfactory Releases"
        url "https://repo.warfactory.co/releases"
    }
}
dependencies {
    // Java 8, unobfuscated
    implementation "com.hbm:ntm-ce:2.4.0.0:dev"
    // Java 25, unobfuscated
    implementation "com.hbm:ntm-ce-java25:2.4.0.0:dev"
    // Java 8, obfuscated
    implementation "com.hbm:ntm-ce:2.4.0.0"
    // Java 25, obfuscated
    implementation "com.hbm:ntm-ce-java25:2.4.0.0"
}
```

Normally you should use unobfuscated jars for development.  
If you are on Cleanroom and is using JDK 25 then both Java 8 and Java 25 variants are fine; otherwise the Java 8 ones are recommended.
