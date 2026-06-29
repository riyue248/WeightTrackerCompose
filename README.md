# 体重记录 App

Kotlin Multiplatform + Compose Multiplatform 跨平台体重追踪应用，支持 **Android** 和 **iOS**。

## 功能

- 新增/编辑/删除体重记录
- 设置目标体重，自动计算距目标的差值
- 每条记录显示**比上次重了/轻了多少**
- 顶部一键切换 **kg / 斤**
- 数据本地存储，不联网

---

## 获取 iOS 安装包

每次代码推送到 GitHub，[Actions](../../actions) 会自动编译出 IPA：

1. 点击仓库顶部 **Actions** 标签
2. 点击最新的 **Build iOS IPA** 运行记录
3. 拉到页面底部 **Artifacts** 区域
4. 下载 **WeightTracker-iOS.zip**，解压得到 `.ipa` 文件

---

## iPhone 安装 + 永久续签教程

### 1. 电脑装 AltServer

1. 下载 AltServer：https://altstore.io （选 Windows 版）
2. 安装后打开，任务栏出现 **◇ 菱形图标**，右键点它确认在运行

### 2. iPhone 装 AltStore

1. 数据线连 iPhone 到电脑
2. 在 iTunes / 访达里勾选 **"通过 Wi-Fi 与此 iPhone 同步"**（这步很重要）
3. 点任务栏 AltServer 图标 → **Install AltStore** → 选你的 iPhone
4. 输入你的 **Apple ID**（免费账号就行）
5. iPhone 桌面出现 AltStore → 去 **设置→通用→VPN与设备管理** 点信任

### 3. 安装 App

1. 把 `.ipa` 发到 iPhone（微信/AirDrop/QQ 都行）
2. iPhone 文件 App 里找到它，点分享 → **用 AltStore 打开**
3. 自动安装

### 4. 自动续签（关键！）

1. 电脑上的 **AltServer 保持开着**（可以开机自启）
2. iPhone 和电脑 **连同一个 WiFi** 就行，不用插线
3. AltStore 在后台**到期前自动刷新签名**
4. 打开 AltStore 能看到剩余天数，连上 WiFi 就自动变回 7 天

**只要电脑开着 AltServer + 同 WiFi，就永远不会过期。**

---

## 项目结构

```
composeApp/src/
├── commonMain/          ← 跨平台代码（Android + iOS 共享）
│   └── kotlin/.../
│       ├── data/           数据模型 + JSON 存储
│       ├── viewmodel/      纯 Kotlin ViewModel
│       ├── util/           工具函数
│       └── ui/             全部 Compose UI
├── androidMain/         ← Android 入口 + 数据迁移
└── iosMain/             ← iOS 入口
```

## 开发

```bash
# Android APK
./gradlew :composeApp:assembleDebug

# iOS Framework（需 macOS）
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```
