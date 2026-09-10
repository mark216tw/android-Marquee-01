# 系統設計文件

## 架構概觀

本專案採用單一 Android application module、單一 Activity 與 Jetpack Compose。畫面透過 callback 將操作傳給 `MarqueeViewModel`，再由 ViewModel 更新 Compose state 並寫入本機儲存。

```text
MainActivity / Compose Screens
             |
             v
      MarqueeViewModel
        |           |
        v           v
  PresetStore  AppPreferences
        |           |
        +----- SharedPreferences
```

專案目前沒有使用 Navigation Compose、依賴注入框架、資料庫或網路服務。

## 主要元件

| 元件 | 責任 |
| --- | --- |
| `MainActivity.kt` | App 啟動、主題組裝、系統列樣式與畫面切換 |
| `Screens.kt` | 首頁、設定、編輯器與播放器 Compose UI |
| `MarqueeViewModel.kt` | 預設清單、編輯草稿、外觀設定與操作流程 |
| `MarqueePreset.kt` | 跑馬燈資料模型及 enum 定義 |
| `MarqueeText.kt` | 無縫滾動、鏡像、文字效果及彩虹動畫 |
| `PresetStore.kt` | 預設 JSON 的序列化與 SharedPreferences 儲存 |
| `AppPreferences.kt` | 深色模式、Hue 與保持常亮設定 |
| `ColorPickerDialog.kt` | HSV 與 Hex 色彩選擇器 |
| `ColorUtils.kt` | 色碼解析、HSV 轉換與對比計算 |
| `AppTheme.kt` | Material 3 動態 Hue 色彩配置 |

## 導航設計

`MarqueeApp` 以 `rememberSaveable` 保存畫面名稱及選取的預設 ID，支援 `home`、`editor`、`player`、`settings` 四個畫面。這是輕量的手動導航，不具正式 back stack 或型別安全 route。

- 首頁可進入新增、編輯、播放或設定。
- 編輯頁會依草稿是否變更決定是否顯示放棄確認。
- 播放器離開時會解除畫面方向鎖定。
- 設定頁返回後回到首頁。

## 資料模型

`MarqueePreset` 包含以下資料：

- 識別與內容：`id`、`name`、`text`
- 字型：`fontSize`、`letterSpacing`、`fontWeight`
- 色彩：`foregroundColor`、`backgroundColor`、`themeMode`
- 滾動：`speed`、`direction`、`loopGap`
- 播放：`orientation`、`startPaused`
- 效果：`mirrorText`、`textEffect`

主要 enum 包含純色／彩虹模式、左右方向、螢幕方向，以及無效果／描邊／呼吸亮度／霓虹。

## 狀態與編輯流程

ViewModel 以 Compose `mutableStateOf` 保存預設、草稿與 App 偏好。編輯時保留初始草稿及目前草稿，利用 data class 結構相等性判斷是否有未儲存變更。

新增預設會插入清單最前方；編輯會覆寫原位置；複製會放在來源下一筆。刪除及排序完成後立即寫入本機儲存。

## 本機持久化

### 跑馬燈預設

- SharedPreferences 檔名：`marquee_presets`
- Key：`presets`
- 格式：JSON array 字串
- Enum：以 enum 名稱保存

目前沒有 schema version、逐筆容錯、備份副本或資料遷移。任一 JSON 項目解析失敗時，載入流程會回到內建範例預設。

### App 設定

- SharedPreferences 檔名：`app_appearance`
- `dark_theme`：Boolean，預設 `false`
- `theme_hue_v2`：Float，預設 `36`
- `keep_screen_on`：Boolean，預設 `true`

## 跑馬燈渲染

`MarqueeText` 量測容器及文字寬度，依 `文字寬度 + 循環間距` 計算週期距離。畫面建立足夠數量的重複文字，使用 `Animatable` 與線性 tween 持續位移，形成無縫循環。

- 向左：位置由 `0` 移動至負週期距離。
- 向右：位置由負週期距離移動至 `0`。
- 鏡像：字形以 `scaleX = -1` 翻轉，並反轉實際滾動方向。
- 呼吸亮度：共用一個 alpha 動畫，於 `1.0` 和 `0.45` 間循環。
- 動態彩虹：移動重複漸層 Brush。
- 暫停：Compose coroutine 取消，動畫保留當前值。

## 播放器生命週期

播放器依預設要求裝置方向，隱藏系統列並允許滑動暫時叫回。離開播放器時會顯示系統列、清除保持常亮狀態，並由上層流程解除方向鎖定。

## 測試配置

- `app/src/test`：色彩工具、草稿變更、排序及鏡像方向單元測試。
- `app/src/androidTest`：Compose 畫面行為與 SharedPreferences 持久化測試。

Android UI 測試需要連接裝置或模擬器。詳細指令請參閱[建置與發布](BUILD_RELEASE.md)。

## 已知技術風險

- 草稿未使用 `SavedStateHandle`，程序死亡後無法可靠恢復。
- JSON 缺少 schema 與單筆錯誤隔離。
- SharedPreferences 與 JSON 處理在呼叫執行緒執行，只適合目前的小量資料。
- 手動字串導航不適合大型畫面流程。
- TalkBack、RTL、平板、大字體與折疊裝置仍需更完整驗收。
- 正式 Release 簽章、CI 與自動發布尚未配置。
