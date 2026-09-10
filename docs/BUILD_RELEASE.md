# 建置與發布

## 開發環境

- Android Studio（支援 Android Gradle Plugin 8.11.1）
- JDK 17
- Android SDK 36
- Gradle Wrapper 8.14.3

最低支援 Android 8.0（API 26），compileSdk 與 targetSdk 均為 36。

## 建置指令

Windows：

```powershell
.\gradlew.bat assembleDebug
```

macOS／Linux：

```shell
./gradlew assembleDebug
```

Debug APK 位於 `app/build/outputs/apk/debug/app-debug.apk`。

## Build ID

設定頁會顯示編譯時的 Build ID。來源優先順序如下：

1. Gradle property：`-PbuildId=...`
2. 環境變數：`MARQUEE_BUILD_ID`
3. Android `versionCode`

範例：

```powershell
.\gradlew.bat assembleDebug "-PbuildId=20260910.120000.000"
```

## 測試與檢查

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

有連接裝置或模擬器時執行：

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Prerelease Build Type

`prerelease` 繼承 `release`，並具有以下設定：

- 版本名稱加上 `-prerelease`
- 啟用 R8 程式碼壓縮
- 啟用資源壓縮
- 使用 Android 預設最佳化 ProGuard 規則
- 使用 Debug 金鑰簽署

```powershell
.\gradlew.bat assemblePrerelease "-PbuildId=20260910.120000.000"
```

輸出位於 `app/build/outputs/apk/prerelease/app-prerelease.apk`。

Prerelease 只供測試及功能預覽，不得視為正式上線版本。

## GitHub 發布原則

- Prerelease 使用 `v<版本>-prerelease` tag。
- GitHub Release 必須勾選「Set as a pre-release」。
- Release 標題及說明必須清楚標示 Pre-release。
- 上傳 `app-prerelease.apk`，不將 APK commit 至 Git repository。
- 說明 Debug 簽章、測試用途及安裝限制。
- 正式版發布前必須配置獨立 Release signing、完成實機驗收並重新檢查隱私與備份政策。
