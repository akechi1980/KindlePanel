# KindlePanel

一个面向旧安卓平板的轻量级全屏看板应用，当前目标设备为 Kindle Fire HD 8 第 7 代一类老设备。

## 当前功能

- 本地看板模式
  - 左侧显示日期、星期和轻量翻页时钟
  - 右侧显示天气图标、温度、天气描述和城市名
- 网页模式
  - 使用全屏 `WebView` 展示网页
  - 支持设定网页地址和自动刷新
- 相册模式
  - 读取本地图片目录
  - 支持顺序播放、随机播放和轻量淡入切换
- 浮层操作
  - 空白区域连续点击三次后显示浮层
  - 右上角提供“设定 / 退出”
  - 右下角提供“看板 / 网页 / 相册”模式切换

## 设计原则

- 优先稳定、兼容、轻量
- 不依赖 Google Play Services
- 尽量兼容旧版 Android / Fire OS
- 所有界面文案使用中文
- 动画保持轻量，不做重型 3D 效果

## 配置说明

应用设定页当前支持以下项目：

- 默认启动模式
- 网页地址
- 网页自动刷新间隔
- 天气城市
- 天气刷新间隔
- 相册图片目录
- 相册切换间隔
- 相册播放方式
- 是否保持常亮

配置使用 `SharedPreferences` 持久化保存。

## 天气说明

- 当前天气数据源为 `Open-Meteo`
- 使用城市名查询地理位置，再拉取当前天气与当天高低温
- 网络失败时优先回退到本地缓存

## 构建方式

项目使用 Gradle 构建。

### Debug 安装

```bash
export JAVA_HOME="/path/to/your/jdk"
export ANDROID_HOME="/path/to/your/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
./gradlew installDebug --no-daemon
```

### Release 构建

```bash
export JAVA_HOME="/path/to/your/jdk"
export ANDROID_HOME="/path/to/your/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
./gradlew assembleRelease --no-daemon
```

默认产物路径：

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release-unsigned.apk`

## 当前安装说明

当前已经验证过：

- `debug` 包可以直接安装到 Kindle
- `release` 构建默认产物是未签名包，不能直接安装

如果需要把 `release` 包直接装到设备，可先用本机调试 keystore 补签名。当前测试使用过的签名方式为：

- keystore：`~/.android/debug.keystore`
- alias：`androiddebugkey`
- 证书名义：`C=US, O=Android, CN=Android Debug`

已签名测试包示例：

- `app/build/outputs/apk/release/app-release-debugsigned.apk`

ADB 安装示例：

```bash
adb install -r \
app/build/outputs/apk/release/app-release-debugsigned.apk
```

## 使用方法

1. 启动应用后会直接进入当前模式。
2. 空白区域连续点击三次，呼出浮层菜单。
3. 右上角可进入设定或退出。
4. 右下角可临时切换到看板、网页或相册模式。
5. 在设定页修改参数后保存，返回主界面生效。

## 相册模式注意事项

- 旧 Fire OS 上不要使用过新的 Java 接口写法
- 当前已兼容 `List.sort(...)` 在老系统上的崩溃问题
- 建议给相册模式配置一个干净、可控的图片目录

## 当前已知情况

- Android Studio 有时不会稳定识别当前 Gradle 工程，尤其在 wrapper 指向外部本地 Gradle 压缩包时更明显
- 当前 `gradle-wrapper.properties` 仍引用了外部本地 Gradle 压缩包
- 如果后续要长期维护，建议补正式签名配置，并把 Gradle 分发包改为项目内可控路径
