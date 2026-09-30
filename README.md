# LED跑馬燈

一款簡單、快速、容易操作的 Android 跑馬燈 App。可以儲存多組訊息、自訂文字外觀與滾動方式，並以全螢幕模式播放。

> **開發狀態：Pre-release**
> GitHub Releases 提供的是公開測試 APK，不是正式上線版本，且使用 Android Debug 金鑰簽署。

## 功能

- 建立、編輯、複製及刪除多組跑馬燈預設
- 長按拖曳手柄調整預設順序，並自動保存
- 自訂文字內容、字體大小、字距及字重；鏡像模式會一併反轉播放方向
- 支援無效果、描邊、呼吸亮度及霓虹文字效果
- 自訂文字色與背景色
- 視覺化 HSV 選色器、進階 Hex 輸入與 WCAG 對比提示
- 12 組跑馬燈配色，包含靜態與動態彩虹效果
- 調整滾動速度、方向及文字循環間距
- 文字頭尾無縫循環，並保留舒適間距
- 編輯時即時預覽
- 點擊播放畫面顯示或隱藏控制列，並可暫停、繼續或返回
- 全螢幕播放，可設定是否保持螢幕常亮
- 跟隨系統方向、鎖定橫屏或鎖定直屏
- 每組跑馬燈可設定進入播放畫面時先保持靜止
- 預設淺色介面，並可開啟深色模式
- 6 組介面色相捷徑與 Hue 彩色滑桿，可即時自訂主題色彩
- 系統狀態列及導覽列會同步介面明暗模式
- 所有預設與介面設定皆儲存在裝置本機
- 活潑的 Adaptive Icon，使用綠色點陣 `LED` 文字與前景、背景分層設計
- 首頁提供設定與離開應用程式圖示

## 操作方式

1. 在首頁選擇既有跑馬燈，點擊「播放」。
2. 點擊首頁頂部設定圖示左側的 `+` 建立新預設。
3. 在編輯頁調整內容、色彩、文字效果、文字樣式、滾動方式及播放方向。
4. 播放時點擊畫面可顯示或隱藏控制列，再由控制列暫停、繼續或返回。
5. 點擊首頁右上角齒輪圖示，切換深色模式、調整介面 Hue 色相或設定播放時是否保持常亮。
6. 長按預設卡片的拖曳手柄可調整順序；其他操作位於更多選單。

## 系統需求

- Android 8.0（API 26）以上
- 編譯 SDK：Android 16（API 36）
- JDK 17

## 技術

- Kotlin
- Jetpack Compose
- Material 3
- ViewModel
- SharedPreferences + JSON 本機持久化
- Gradle Kotlin DSL

## 建置

使用 Android Studio 開啟專案，等待 Gradle 同步完成後執行 `app`。

也可以在專案根目錄使用命令列建置：

```shell
./gradlew assembleDebug
```

Windows：

```powershell
.\gradlew.bat assembleDebug
```

測試或 CI 建置可傳入 UTC Build ID，並顯示於設定頁：

```powershell
.\gradlew.bat assembleDebug "-PbuildId=20260909.153012.123"
```

也可以使用 `MARQUEE_BUILD_ID` 環境變數；未提供時會顯示 Android `versionCode`。

測試發行版本使用 `prerelease` Build Type，會啟用 R8 與資源壓縮，並以 Android Debug 金鑰簽署：

```powershell
.\gradlew.bat assemblePrerelease "-PbuildId=20260909.153012.123"
```

建置完成的 APK 位於：

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/prerelease/app-prerelease.apk
```

## 檢查

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

## 安裝

可以從 GitHub Releases 下載標示為 Pre-release 的測試 APK。首次手動安裝時，Android 可能會要求允許瀏覽器或檔案管理器安裝未知來源應用程式。

目前 Debug 與 prerelease APK 使用 Android 預設 `debug.keystore` 簽章，只適合測試及功能預覽。未來正式版改用 Release 簽章後，可能需要先解除安裝測試版本才能安裝。

也可以透過 ADB 安裝本機建置版本：

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/prerelease/app-prerelease.apk
```

## 資料與隱私

App 不需要帳號或網路權限。跑馬燈預設與介面偏好儲存在 App 私有空間，但目前允許 Android 系統備份，資料可能由系統備份或裝置轉移服務還原。詳情請參閱[隱私與資料保存](docs/PRIVACY.md)。

## 文件

- [專案文件索引](docs/README.md)
- [專案概覽](docs/PROJECT_OVERVIEW.md)
- [使用指南](docs/USER_GUIDE.md)
- [系統設計](docs/SYSTEM_DESIGN.md)
- [建置與發布](docs/BUILD_RELEASE.md)
- [隱私與資料保存](docs/PRIVACY.md)
- [優化建議](docs/OPTIMIZATION_SUGGESTIONS.md)
- [待辦事項](docs/TODO.md)
- [版本紀錄](CHANGELOG.md)

## 授權

本專案採用 [MIT License](LICENSE) 授權。

作者：mark216tw <mark216tw@gmail.com>
