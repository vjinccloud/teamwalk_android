# Handoff — Next session 的你直接看

## 🎯 現在在哪

**剛完成 v3.0.41 (versionCode 81) 給 Kady 做最終 UAT 驗證**。

主軸是修 #0002992（Pixel 7 上密碼 3 個月 alert 白屏卡死問題）。已經 commit + push + tag 三家都同步。等 Kady 在 UAT 驗收。

當前 HEAD：`3f25694`（Kotlin版本_第一階段 branch）
當前 tag：`v3.0.41` 指向 `3f25694`

## 📌 #0002992 最終解法（核心要記得）

[MyWebChromeClient.kt](app/src/main/java/com/taiwanlife/teamwalk/utils/MyWebChromeClient.kt) `onJsAlert` 內：

```kotlin
// keyword 比對：訊息同時包含「三個月」+「密碼」→ 直接 result.confirm() 跳過 dialog
if (message != null && message.contains("三個月") && message.contains("密碼")) {
    result.confirm()
    return true
}
// 其他 alert（密碼錯誤等）走正常 AlertDialogManager dialog
```

**為什麼這樣修（記住這點別搞錯）**：

**Dialog 在 LoginActivity 1dp×1dp 隱形 webview 觸發下根本顯示不出來** — 我們試過很多方法（Handler.post、isShowing 檢查、用 Activity context、改 try-catch）都救不回來，也試過用 androidx.appcompat 換系統版 AlertDialog，但對「三個月 alert + 緊接 redirect」這種特例還是失敗。

既然 dialog 顯示不出來、user 卡白屏 → **吞掉這個 alert**讓 webview 自動 redirect 到改密碼頁（CSSOWebViewActivity）。reasonable rationalization：反正 user 點 OK 後本來就要去改密碼，跳過去也沒差。

**這是 user 自己想出來的解法**（不是我提的）。是 pragmatic 而非 root-cause fix，**root cause 沒完全釐清**（推測是 1dp×1dp webview window state + JS redirect 競態的複合因素，但沒科學驗證過）。

**順便修的另一塊**：MyWebChromeClient 對其他 alert（密碼錯誤、驗證碼錯誤等）也從 `androidx.appcompat.app.AlertDialog` 改成 `AlertDialogManager.getAlertDialog`（用系統版 `android.app.AlertDialog`）。這個換掉之後其他 alert 就會正常顯示 dialog，**只剩三個月 alert 那種「alert + 立即 redirect」的特例需要 keyword-skip 繞過**。

## 🏗 架構重點 — 三個 webview，要記住

```
LoginActivity 1dp×1dp 隱形 webview（背景做事）
   └─ MyWebChromeClient（utils 共用類別）
   └─ 用途：POST 帳密給 CSSO server，自動處理 cookie/redirect/拿 ticket

MainActivity 全螢幕 webview（SPA 主畫面 = 阿龍那頁）
   └─ MyWebChromeClient（共用同一個）
   └─ 用途：載入 web_url bridge 頁面，跟 Web SPA 互動

CSSOWebViewActivity 全螢幕 webview（user 互動的 CSSO 網頁）
   └─ inline 匿名 WebChromeClient（不共用）
   └─ 用途：改密碼/忘記密碼/註冊 三種 purpose
   └─ 內部 dialog 用 AlertDialogManager.getAlertDialog
```

## 🚨 雷點 — 不要亂動

1. **`MyWebChromeClient` 用 `AlertDialogManager.getAlertDialog`**（系統版 `android.app.AlertDialog`）
   - 不要改回 `androidx.appcompat.app.AlertDialog`！會在 1dp×1dp webview 環境 attach 失敗
   - 這是這次 #0002992 修復的關鍵之一

2. **LoginActivity 的 `viewBinding.webview` 是 1dp×1dp 隱形的**（`activity_login.xml`）
   - 不要把它放大「以為 user 應該看到 webview」— 是故意隱形的（hack）
   - 它只負責 POST CSSO 拿 ticket，user 看的是原生帳密 UI

3. **WebView 是 SurfaceView，會蓋掉 native 元件**
   - 主畫面的 native loading_container 在 MainActivity 看不見（被 WebView 蓋）
   - 但 LoginActivity 沒 WebView 蓋掉的問題（webview 1dp）
   - 這個是 Android WebView 的本質限制，沒辦法

4. **build.gradle.kts 簽章密碼明碼寫死**：`Best2021`（debug/sit/uat 共用）
   - 這是資安隱憂，但全 repo 都這樣不要動，等之後重構
   - keystore 路徑：`app/teamwalk-uat.jks`

5. **`SecuredPreferenceStoreManager`** Tink 加密過的 SP，**不要直接 SharedPreferences** 存敏感資料
   - JWT/CASTGC/FID 都走這個
   - 失敗復原強化過了（v3.0.41 commit `6c2afbf`）

6. **不要動 CSSOWebViewActivity 的 inline WebChromeClient**
   - 那個 work，動了就壞

## 📋 已完成項目（承接客戶單，最近）

### 第一輪 7 張單（v3.0.40 之前已交付）— NT$ 4,600
- #0003003 文字大小跑版（textZoom=100）
- #0003005 CSSO User-Agent 補上 `/env=taiwanlife_teamwalk_app`
- #0003006 WebView cache buster `?_=timestamp`
- #0003008 HC 權限拒絕一鍵跳設定頁 + 同步偵測未授權 dialog 補確認 onClick
- #0003016 /landing 載入完成監測（含 webviewFinished JS bridge + 10s timer 重建 + 自動登出）
- #0002957 三隻 crash 防護 + Fitbit 補 try-catch fallback
- #0002967 同步資料延遲分析（沒改 code，是 Samsung Health 寫入 HC 延遲問題，行業共通）

### 第二輪修復（剛完成 v3.0.41）— NT$ 1,800
- #0002992 alert 白屏 → MyWebChromeClient 改 AlertDialogManager + keyword skip
- #0002992 連線中卡住 → CSSOWebViewActivity onProgressChanged 100% + 8s timeout + onReceivedError 補關
- Crashlytics keystore crash → SecuredPreferenceStoreManager 復原機制強化
- OkHttp UA 補 `taiwanlife_teamwalk_app/版號` + getSysParam 5 秒 throttle 防 DDoS
- #0003016 dialog 文案「連線異常，請稍後再試」→「連線異常，請重新登入」

### 開發流程改善
- APK 檔名加 git short hash（debug 期間不用升版）
- 升版策略：debug 階段不升版/不 tag、確認交付才升

**累計報價：NT$ 6,400（暫估）**

## 🚧 目前卡在 / 等待中

- **等 Kady UAT 驗收 v3.0.41**（拿 90 天密碼帳號驗證所有 alert 流程）
- **#0002967 同步資料**：等 Web/後端工程師查 server-side dedupe + Web 顯示邏輯
- **CSSO IP 限制**：Android UAT debug 連不到真 cssouat（公司網路才行），無法本機驗證真 CSSO 行為

## 🚫 試過但不行的方案（避免重蹈覆轍）

1. **`androidx.appcompat.app.AlertDialog` 直接用**
   - 在 1dp×1dp webview 觸發下 attach window 失敗、dialog 顯示不出來
   - 換系統版 `android.app.AlertDialog`（透過 `AlertDialogManager`）才解

2. **`isContextValid()` 預先檢查 + try-catch**
   - 之前 #0002957 為防 BadTokenException crash 加的
   - 但 isContextValid 在 mid-transition 狀態會誤殺合法 alert
   - 改成「直接試 show，catch BadTokenException」較好

3. **用 `Handler.post(Looper.getMainLooper())` 包 dialog show**
   - 試圖避開 webview callback 跟 UI thread 同 frame 競態
   - 沒解決問題

4. **自動 `result.confirm()` + Toast 顯示訊息**
   - 簡化版，但 user 沒看到強制提示，CSSO 設計本意被破壞
   - 已 revert（除了三個月那條規則例外）

5. **Toast 診斷**（[診斷-1] 之類）
   - 對 debug 期間有用，但不能上 release
   - 已清掉所有診斷 toast（commit `4f7c362`）

6. **`isShowing` 檢查 + Toast fallback**
   - dialog.show() 不拋 exception 但 isShowing=false 的情境
   - 不可靠，已 revert

## 🔧 技術棧 / 環境

```
Kotlin 2.2.21
AGP 8.9.2
minSdk 29 / targetSdk 36 / compileSdk 36
JavaVersion.VERSION_17
Koin DI 4.0.3 (BOM)
Retrofit 2.10 + OkHttp 5.3 + Gson 2.13.2
Firebase BOM 33.11.0 (FCM + Crashlytics + Analytics)
androidx.health.connect 1.1.0-rc02
Tink 1.20.0（敏感資料加密）
PatternLockView (jitpack)
Timber 5.0.1
ViewBinding（不是 DataBinding/Compose）
```

### Build Variants
```
debug      → mutron mock CSSO（demo.mutron.com.tw），applicationIdSuffix=.debug，DebugTeamWalk
sit        → mutron mock CSSO，.sit
uat        → 真台壽 UAT（cssouat.taiwanlife.com），.uat，UATTeamWalk（要 VPN）
release    → 正式（csso.taiwanlife.com），TeamWalk
```

`debug/sit/uat` 都 `ENABLE_API_LOG=true`、共用 `teamwalk-uat.jks`（密碼 Best2021）。
release 用 `keystore.jks`，密碼工程師自己問。

### Git Remotes（3 家都要 push）
```
origin     → git@github-wade:vjinccloud/teamwalk_android.git (內部)
mutron     → git@github-wade:MutronTitan/taiwanlife-teamwalk-app-android.git (測試)
taiwanlife → git@bitbucket-wade:th1nkd1fferent/teamwalk-2-android.git (台壽)
```

每次 push 三個都要：
```bash
git push origin Kotlin版本_第一階段 && git push mutron Kotlin版本_第一階段 && git push taiwanlife Kotlin版本_第一階段
```

Tag push 同理：
```bash
git push origin <tag> && git push mutron <tag> && git push taiwanlife <tag>
```

### Branch
**Kotlin版本_第一階段** 是主開發 branch（之前老闆/前位開發者命名）。原本應該合回去 main / master 但目前不動。

## 📁 重要檔案結構

```
app/src/main/java/com/taiwanlife/teamwalk/
├── MyApplication.kt              ← Timber.plant + Koin + SP init
├── Config.kt                     ← 全域常數、SP keys、event names
├── EnvironmentManager.kt         ← 讀 build variant 的 resValue（csso_url, api_url 等）
├── base/
│   ├── BaseActivity.kt           ← 所有 Activity 父類別、loading_container 邏輯
│   └── BaseViewModel.kt          ← ApiFlow / RawFlow / CSSOFlow + apiCallAsSharedFlow
├── di/AppModule.kt               ← Koin DI（OkHttpClient + Retrofit + ViewModels）
├── remote/
│   ├── service/                  ← Retrofit interfaces
│   ├── interceptor/
│   │   ├── TokenInterceptor.kt   ← JWT injection + 客製 UA
│   │   └── ApiLoggingInterceptor.kt
│   ├── *Repository.kt            ← API/CSSO/Garmin/Fitbit/HealthConnect Repository
│   └── HealthConnectRepository.kt ← HC 讀寫
├── service/TWFirebaseMessagingService.kt  ← FCM
├── ui/
│   ├── login/
│   │   ├── LoginActivity.kt      ← 帳密登入頁，內含 1dp×1dp webview ⚠️
│   │   └── CSSOWebViewActivity.kt ← 改密碼/忘密/註冊全螢幕 webview ⚠️
│   ├── main/
│   │   ├── MainActivity.kt       ← 主畫面，內含 SPA WebView，與 Web JS bridge 互動
│   │   └── webview/
│   │       ├── MyWebView.kt      ← 主 webview 實作
│   │       └── MyWebMessageListener.kt ← JS bridge handler（webviewFinished 在這）
│   └── ...其他 Activity
├── utils/
│   ├── MyWebChromeClient.kt      ← LoginActivity + MainActivity 共用 alert handler ⚠️
│   ├── AlertDialogManager.kt     ← 系統版 android.app.AlertDialog 包裝
│   ├── SecuredPreferenceStoreManager.kt ← Tink 加密 SP ⚠️
│   ├── BindingManager.kt         ← Garmin/Fitbit/HC 綁定流程
│   ├── HealthConnectHelper.kt    ← HC 權限請求
│   ├── PermissionManager.kt      ← runtime 權限
│   └── Extension.kt              ← 一堆 extension functions
```

## 🎨 命名慣例

- **commit message 格式**（看歷史 git log 模仿）：
  ```
  feat. <功能說明>
  fix. <修了什麼>
  chore. <雜事>
  diag. <診斷用，臨時的>
  ```
- **不要加 `Co-Authored-By: Claude...`**（user 明確要求過）
- **issue 編號用 `#0003003` 七位數**（前位開發者習慣）
- **SharedPreferences key 用 `SP_` 前綴**（Config.kt 集中）
- **Event 用 `EVENT_` 前綴**（Config.kt 集中）

## 🗣 我（user）的偏好

- **講話風格**：直接、不客套、有結論。不要「抱歉」開頭、不要「希望幫到您」結尾。
- **回答結構**：盡量用 markdown 表格 / 流程圖 / 對照清單。不要長篇大論。
- **解釋技術**：類比要打到位（用 ATM、收據、抽屜等）。不要照字面翻譯英文術語。
- **不確定的時候**：直接說「不確定」或「猜測」，不要用過度肯定的語氣（之前我用「肯定是 window.location.href 害的」被質疑過）
- **commit 前**：每次改完都會 commit + push 三家。不要只 push origin。
- **diff 範圍**：改一個 bug 就改一個 bug 的東西，不要 scope creep（之前我自作聰明加診斷 toast 結果失控過）
- **報價**：用人情價，老朋友合作。簡單修不要算太多。

## 📝 跟 user 確認過的決策

1. **debug 期間不升版**：版號穩定，commit 用 git hash 識別。確認交付才升 + tag。
2. **APK 檔名帶 git hash**：`TeamWalk-uat-v3.0.41-vc81-3f25694.apk`（[build.gradle.kts](app/build.gradle.kts) 有 `gitShortHash()` function）
3. **Tag 策略**：只在交付節點 tag。不為了實驗 tag。
4. **舊 debug tag 已清掉**：v3.0.41~v3.0.47 已從三家 remote 刪除，現在 v3.0.41 指向真正的 release commit (3f25694)。
5. **#0002967 同步問題**：等後端確認 server 端 dedupe + Web 顯示邏輯。Android 端只能加 log/audit，沒實際 fix。
6. **#0002992 用 keyword-skip**：不重寫架構（不改用 OkHttp 直打 CSSO，那要 8-16 hr）。

## 🔮 下一步可能要做的（依優先序）

1. **Kady 回報 v3.0.41 UAT 驗收結果** ← 等這個
   - OK → 結案、給 PM 請款
   - 還有問題 → 看是哪一塊、繼續修

2. **#0002967 同步問題後續**：等後端 / Web 端反饋。可能要改 HC 讀法（aggregateGroupByDuration → readRecords / aggregate 整天）。

3. **可能的架構重構（長期）**：
   - LoginActivity 的 1dp×1dp webview → 改用 OkHttp 直打 CSSO（要寫 cookie/redirect/HTML parser，8-16 hr）
   - 拔掉 webview 一勞永逸，跟 iOS 對齊

4. **資安強化**：
   - 簽章密碼從 build.gradle 移到 ~/.gradle.properties
   - 加 Certificate Pinning 用 PK pinning（OWASP 推薦）取代現在的 leaf cert pinning（OEM cert 續簽會炸 App）

## 🆘 常見問題快速參考

| 問題 | 解法 |
|---|---|
| 哪個 webview 觸發 alert？ | LoginActivity 1dp×1dp webview = 登入流程；CSSOWebViewActivity = 改密碼/忘密/註冊 |
| Push 失敗 | 檢查三個 remote 都試一次；taiwanlife 之前曾鎖唯讀（升級 Bitbucket 方案後通） |
| build 名稱奇怪 | check `gitShortHash()` 在 build.gradle.kts 是否成功取到 git hash |
| 哪些 string 不能改 | `R.string.ok` / `R.string.confirm1` / `R.string.cancel` 全 App 多處用 |
| FID 跟 ANDROID_ID 區別 | `appUuid`=Firebase Installations ID（重灌會變）；`deviceId`=ANDROID_ID（裝置綁，重灌不變） |

## ✍️ 最後給你的話

User 是個誠實 / 直接的工程師。他不需要客套，但**會抓你解釋的漏洞**（譬如「window.location.href 害的」這種武斷說法）。**保留謙虛、有不確定就說不確定**。

User 對程式碼**有自己想法**，會自己改 code 然後告訴你，**不要把他的改動當錯誤 revert 掉**。

每次重大改動前先**畫流程 / 列方案讓他選**，不要自作主張。

#0002992 keyword-skip 是 user 自己想出來的解法，他主導，我配合。**這種情境多了**，user 不是來聽你 over-engineer 的。

收工愉快。
