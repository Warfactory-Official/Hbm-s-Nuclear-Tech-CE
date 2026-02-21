<div dir=rtl align=center>

### [**English 🇺🇸**](README.md) / [**Русский 🇷🇺**](README_ru.md) / [**简体中文 🇨🇳**](README_zh.md) / **한국어 🇰🇷** / [**Українська 🇺🇦**](README_ua.md)
</div>

<p align="center"><img src="./github/icon.png" alt="Logo" width="300"></p>

<h1 align="center"> HBM의 원자력 기술 모드 커뮤니티 에디션 <br>
	<a href="https://www.curseforge.com/minecraft/mc-mods/hbm-nuclear-tech-mod-community-edition"><img src="http://cf.way2muchnoise.eu/1312314.svg" alt="CF"></a>
    <a href="https://modrinth.com/mod/ntm-ce"><img src="https://img.shields.io/modrinth/dt/ntm-ce?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
	<a href="https://discord.gg/eKFrH7P5ZR"><img src="https://img.shields.io/discord/1241479482964054057?color=5865f2&label=Discord&style=flat" alt="Discord"></a>
    <br>
</h1>

HBM의 원자력 기술 모드를 1.7.10에서 1.12.2로 완전판에 가깝게 포팅한 버전입니다. 다른 버전들 중에서도 가장 완성도가 높으며, 다른 개발자들이 다른 포크들을 업데이트하고 유지 관리하는 데 실패함에 따라 필요에 의해 개발되었습니다.

> **중요: 문제 보고 시 이슈 템플릿을 준수해 주세요** <br>
> 매일 많은 이슈가 접수되므로 템플릿에 명시된 엄격한 이슈 템플릿을 준수해야 합니다.
템플릿을 준수하지 않을 경우 경고 없이 이슈가 닫히고 잠깁니다. 이 규칙은 소급 적용되지 않습니다.
우리의 시간을 존중해 주시고, 문제 보고시 버그 리포트의 질을 높여 주시기 바랍니다.

> **중요: Universal Tweaks가 설치되어 있는 경우, 모델 회전 문제를 고치려면 `B:"Disable Fancy Missing Model"`을 `false`로 설정하세요.**
> 이 설정은 `config/Universal Tweaks - Tweaks.cfg`에서 확인할 수 있습니다.

<br>
<p align="center"><img src="./github/faq.png" alt="NTM:CE FAQ" width="700"></p>
<br>

### 서바이벌에서 플레이 가능한가요?

아직 수정해야 할 버그가 많지만, 모드 자체는 테스트 결과 치명적인 오류 없이 생존 모드에서 플레이할 수 있는 상태입니다.
다만, 수정해야 할 사소하거나 심각한 버그가 많고, 포팅해야 할 사소한 컨텐츠도 여전히 많습니다.

### NTM: 익스텐디드 에디션의 애드온/쉐이더와 호환되나요?

안타깝게도 불가능합니다. EE 애드온을 설치하면 충돌이 발생하여 모드팩을 플레이할 수 없게 됩니다. 새로운 총기 시스템이 포팅되었기 때문에,
셰이더도 호환되지 않아 총을 들고 있을 때 심각한 시각적 아티팩트가 발생합니다. <br>
또한 셰이더는 NTM 스카이박스와도 호환되지 않습니다. 이 문제는 'config/hbm -> hbm.cfg' 파일에서 'B:1.00_enableSkybox=true' 줄을 'false'로 변경하여 해결할 수 있습니다. <br>
셰이더 관련 문제를 해결하기 위해 최선을 다하고 있지만, 시간이 걸릴 듯 합니다.

### 익스텐디드 에디션과는 어떤 차이가 있나요?

**익스텐디드 에디션이 설치된 월드와는 호환되지 않습니다!** <br>
전체 모드의 약 75%를 재작성했고, 1.7.10 버전에서 가능한 모든 기능을 포팅했습니다.
따라서 현재로서는, 익스텐디드 에디션과 비교해 변경 사항을 추적하기 어렵습니다. 누락되거나 추가된 콘텐츠를 추적하기 위해 GitHub 이슈를 활용하고 있으니,
GitHub 이슈를 확인해 보시기 바랍니다.

### 익스텐디드 에디션에 기여하지 않고 새로 개발하는 이유는 무엇인가요?

Alcater의 경우, 1년 반 넘게 Curseforge 버전을 업데이트하지 않았습니다. 익스텐디드 에디션은 여러 가지 최적화가 부족한 부분과
일부 기능 중 구현이 이상한 부분이 있으며, Alcater가 협력을 거부했기 때문에 모드를 포크하여 별도로 작업하기로 결정했습니다.

### 아직 개발중이라면, 왜 CurseForge에 공개하나요?

**버그 리포트를 받기 위해서입니다.** <br>
Curse, Modrinth와 같은 웹사이트에 참여하지 않으면 모드의 인지도가 크게 감소한다는 것은 너무나도 자명한 사실이죠.
적절한 포팅 작업이 진행 중임을 플레이어 여러분께 알리고, 버그 리포트나 풀 리퀘스트를 통해 직접 저희에게 도움을 주시면 감사하겠습니다.
저희는 항상 새로운 기여자를 찾고 있습니다.

### 이 버전엔 특정 모드팩만을 위한 수정이 들어가나요?
**아니오!** <br>
이 포팅은 Warfactory 프로젝트의 일환으로 시작되었지만, 독립형 모드로 유지 관리되고 있습니다.
모든 변경 사항은 호환성, 안정성, 그리고 모드 팩 개발자의 개발 편의성을 보장하기 위한 것이며,
특정 모드팩을 위한 직접적인 수정은 들어가지 않습니다.

### 1.1x/1.2x로 이식할 예정이 있나요?

**아뇨. 그럴 계획은 없습니다.** <br>
한 번에 한 버전씩 꾸준히 작업하려고 합니다. 애초에 내부분열과 이 모드를 개발하는 데 수많은 팀이 참여했던 것이
이 모드의 포팅 가능성을 떨어뜨린 원인입니다. 이 때문에 저희는 한 번에 한 버전씩 집중적으로 작업하려고 합니다.

<br>
<p align="center"><img src="./github/dev_guide.png" alt="개발 가이드" width="700"></p>
<br>

## **개발시 Java 25를 사용합니다!!**

우리는 최신 구문과 API를 사용하면서도 1.12.2의 Java 8 바이트코드를 원활하게 타겟팅하기 위해 [JvmDowngrader](https://github.com/unimined/JvmDowngrader)를 사용합니다.

### 작업 시작 가이드

1. 이 저장소를 복제합니다.
2. JDK 25를 준비합니다.
3. `setupDecompWorkspace` 작업을 실행합니다(마인크래프트 소스 난독화 처리를 포함한 작업 공간 설정).
4. 모든 것이 정상인지 확인합니다. `runClient` 작업을 실행합니다(모드가 로드된 Minecraft 클라이언트가 열려야 함).

- 작업에는 항상 `gradle` 대신 `gradlew`(Linux/MACOS) 또는 `gradlew.bat`(Win)을 사용합니다. 이렇게 하면 각 개발자가 일관된 환경을 가질 수 있습니다.

### Apple M-chip 컴퓨터의 개발 관련 문제점

ARM 아키텍처용 네이티브가 없으므로 x86_64 JDK를 사용해야 합니다(적합한 JDK를 가장 쉽게 받는 방법은 IntelliJ SDK 관리자를 사용하는 겁니다).

다음 방법 중 하나를 사용하세요.
- GRADLE_OPTS 환경 변수 `export GRADLE_OPTS="-Dorg.gradle.java.home=/path/to/your/desired/jdk"`
- gradle.properties 파일의 추가 속성(~/.gradle 또는 pwd) `org.gradle.java.home=/path/to/your/desired/jdk`
- 터미널에서 -D 매개변수와 함께 직접 사용 `./gradlew -Dorg.gradle.java.home=/path/to/your/desired/jdk wantedTask`

#### 문제 해결법:

x86_64 JDK를 사용하더라도 Gradle이 로그에서 ARM 머신으로 처리하는 경우 다음을 수행하세요.
1. `git fetch; git clean -fdx; git reset --hard HEAD`로 작업 공간 정리. (중요: 로컬과 git을 동기화하고 모든 진행 상황을 삭제하게 됩니다.)
2. `rm -rf ~/.gradle`로 Gradle 캐시 삭제. (중요: 전체 Gradle 캐시를 삭제하게 됩니다.)
3. `rm -rf /path/to/used/jvm`으로 다운로드한 JVM 삭제.
   (사용된 JVM 경로는 /run/logs/latest.log에서 `Java is OpenJDK 64-Bit Server VM, version 1.8.0_442, running on Mac OS X:x86_64:15.3.2, installed at /이게/자바/경로/입니다`와 같이 확인할 수 있습니다.)
4. 작업 시작 가이드를 다시 따르세요.