# 浏览器应用 (Browser App)

一个支持多搜索引擎的 Android 浏览器应用。

## 功能特性

- 支持多个搜索引擎：百度、搜狗、必应、抖音
- 搜索引擎下拉选择
- 快捷访问按钮
- 内置浏览器页面
- WebView 支持 JavaScript 和缩放
- 加载进度条显示
- 返回主页浮动按钮

## 项目结构

```
browser/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/browser/
│   │       │   ├── MainActivity.kt      # 主页面（搜索）
│   │       │   └── BrowserActivity.kt   # 浏览器页面
│   │       ├── res/
│   │       │   ├── layout/              # 布局文件
│   │       │   ├── drawable/            # 图标和背景
│   │       │   ├── values/              # 字符串、颜色、主题
│   │       │   └── mipmap-anydpi-v26/   # 应用图标
│   │       └── AndroidManifest.xml      # 应用清单
│   └── build.gradle.kts                 # 模块级构建配置
├── build.gradle.kts                     # 项目级构建配置
├── settings.gradle.kts                  # Gradle设置
└── gradle.properties                    # Gradle属性
```

## 构建APK

### 方法1：使用 Android Studio

1. 打开 Android Studio
2. 导入项目
3. 点击 `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`
4. APK 将生成在 `app/build/outputs/apk/debug/` 目录

### 方法2：使用命令行（需要 Android SDK 和 Gradle）

**Windows:**
```batch
build.bat
```

**Linux/Mac:**
```bash
./build.sh
```

### 方法3：手动构建

```bash
# 清理构建
cd browser
gradle clean

# 构建Debug APK
gradle assembleDebug

# APK 位置
# app/build/outputs/apk/debug/app-debug.apk
```

## 系统要求

- Android API 24+ (Android 7.0+)
- 目标SDK: 34 (Android 14)
- 编译SDK: 34

## 权限

- `INTERNET`: 访问网络
- `ACCESS_NETWORK_STATE`: 检查网络状态

## 使用的技术

- Kotlin
- Android SDK 34
- Material Design 3
- View Binding
- WebView

## 搜索引擎URL

- 百度: https://www.baidu.com/s?wd=
- 搜狗: https://www.sogou.com/web?query=
- 必应: https://www.bing.com/search?q=
- 抖音: https://www.douyin.com/search/

## 版本历史

### v1.0.0
- 初始版本
- 支持4个搜索引擎
- 基础浏览器功能

## Git 仓库

```bash
git clone https://git.n.xiaomi.com/gongyunhe/browser.git
```

## 许可证

MIT License