# KindlePanel 架构草案

## 1. 项目目录结构建议

```text
KindlePanel/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/kindlepanel/
│       │   ├── MainActivity.java
│       │   ├── config/
│       │   ├── menu/
│       │   ├── mode/
│       │   │   ├── dashboard/
│       │   │   ├── photo/
│       │   │   └── web/
│       │   ├── power/
│       │   ├── settings/
│       │   ├── system/
│       │   └── weather/
│       └── res/
│           ├── drawable/
│           ├── layout/
│           ├── values/
│           └── xml/
├── docs/
│   └── architecture.md
├── build.gradle
├── gradle.properties
└── settings.gradle
```

设计原则：
- 保持单 `app` 模块，避免过早拆分多模块带来的构建复杂度。
- Java + XML 优先，减少旧设备上的构建链复杂性与运行时风险。
- 模式、设置、常亮、菜单、天气各自独立包，后续扩展不会挤在一个类中。

## 2. 页面与模块划分

### 页面

1. `MainActivity`
   负责全屏容器、模式切换、三击菜单入口、常亮应用。
2. `DashboardFragment`
   左侧日期/星期/大号时间，右侧天气信息。
3. `WebModeFragment`
   全屏 `WebView`，负责页面加载、失败提示、自动刷新。
4. `PhotoModeFragment`
   全屏图片轮播，负责目录读取、顺序/随机播放、淡入淡出。
5. `SettingsActivity`
   中文设置页，保存默认模式及各模式配置。

### 功能模块

1. `config`
   使用 `SharedPreferences` 持久化配置，避免引入数据库。
2. `power`
   集中管理常亮，统一在 Activity 生命周期中启停。
3. `menu`
   处理“三击显示菜单”和“超时自动隐藏”。
4. `weather`
   处理天气数据模型、图标映射、后续 API 接入。
5. `system`
   处理全屏沉浸式控制。

## 3. 关键类设计

### 核心控制

- `MainActivity`
  - 加载当前默认模式
  - 承载 `Fragment`
  - 处理三击菜单与设置返回后的重载
- `FullscreenController`
  - 集中处理沉浸式全屏标志位
- `ScreenAwakeController`
  - 统一封装 `FLAG_KEEP_SCREEN_ON`
- `TripleTapMenuController`
  - 记录点击时间窗口
  - 控制菜单显示/隐藏和空闲超时

### 配置

- `AppSettings`
  - 配置对象，集中保存所有可持久化字段
- `SettingsRepository`
  - 读写 `SharedPreferences`
  - 提供默认值与重置逻辑

### 模式

- `DashboardFragment`
  - 轻量时钟刷新
  - 调用 `WeatherRepository`
- `WebModeFragment`
  - `WebView` 初始化
  - 自动刷新调度
  - 错误视图切换
- `PhotoModeFragment`
  - 读取本地图片文件列表
  - 调度下一张图片
  - 顺序/随机播放

### 天气

- `WeatherInfo`
  - 当前天气模型
- `WeatherRepository`
  - 第一版先做接口骨架和占位数据
  - 后续再接轻量 HTTP 请求实现
- `WeatherIconMapper`
  - 将天气状态映射为统一风格图标资源

## 4. MVP 开发步骤

1. 建立项目骨架
   - 单 Activity + 三个 Fragment + 设置页
   - 深色主题、默认全屏、基础资源整理
2. 完成配置持久化
   - `SharedPreferences`
   - 设置页保存、恢复默认
3. 完成三击菜单与模式切换
   - 空白区域三击呼出
   - 菜单仅“退出 / 设定”
4. 完成本地看板 MVP
   - 时间日期显示
   - 天气区域先使用占位数据
   - 轻量“翻页感”动画
5. 完成网页模式 MVP
   - `WebView` 加载配置 URL
   - 自动刷新
   - 中文失败提示
6. 完成相册模式 MVP
   - 本地目录读取
   - 顺序/随机
   - 轻量淡入淡出
7. 最后再接真实天气接口
   - 独立实现 HTTP 获取
   - 增加失败回退与缓存

## 5. 老设备风险点与更稳妥方案

### 风险 1：Grafana 页面在旧 WebView 上兼容性不足

风险：
- Kindle Fire 旧版系统 WebView 可能对较新的 Grafana 前端支持不足。

稳妥方案：
- 第一版只做“可配置 URL + 自动刷新 + 中文错误提示”。
- 若后续发现兼容性差，优先让 Grafana 输出更简单的公开看板页面，避免在 App 内做复杂兼容层。

### 风险 2：本地相册目录访问在不同 Android 版本行为不一致

风险：
- 新系统受存储权限限制，旧系统则通常可直接访问文件路径。

稳妥方案：
- MVP 先支持手工填写本地目录路径。
- 后续若确实需要，再补目录选择器，但不在第一版引入复杂文档树适配。

### 风险 3：真实机械翻页动画性能与实现成本偏高

风险：
- 真翻页动画会引入额外绘制和复杂状态控制，不适合老设备常亮运行。

稳妥方案：
- 第一版使用轻量位移 + 透明度变化营造“翻页感”。
- 保证刷新简单、稳定、低耗。

### 风险 4：天气接口接入后可能带来网络抖动与 UI 阻塞

风险：
- 老设备网络波动下，若实现不慎，容易出现卡顿或空白。

稳妥方案：
- 天气请求必须与 UI 解耦。
- 第一版先保留仓储接口和占位数据，再逐步补缓存与超时处理。
