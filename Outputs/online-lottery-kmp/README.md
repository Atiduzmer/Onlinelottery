# 好运彩店 Android 客户端

本工程依据仓库 `Imports/APP项目结构.txt` 和三张参考图创建，是可直接用 Android Studio 打开的 Kotlin Multiplatform + Compose Multiplatform Android 工程。

## 工程结构

- `shared`：KMP 公共界面、交互状态和模拟业务数据。
- `androidApp`：Android Application、Manifest、Activity 和 APK 构建配置。
- `qa`：模拟器验收截图和交互验证留档。

当前阶段实现的是可运行客户端原型，未连接真实登录、充值、支付、投注、出票或开奖服务。

## Android Studio

用 Android Studio 打开本目录，等待 Gradle Sync 完成，然后选择 `androidApp` 运行配置。

环境基线：Android Studio 2026.1.2、JDK 21、Android SDK 36、minSdk 24。

## 命令行构建

```powershell
.\gradlew.bat :androidApp:assembleDebug --no-configuration-cache
```

Debug APK 输出位置：

`androidApp/build/outputs/apk/debug/androidApp-debug.apk`

为了便于直接取用，最终验证通过的 APK 另复制到：

`../online-lottery-debug.apk`

Release APK 需要在接入正式接口后，再配置正式包名、版本信息和独立签名文件。
