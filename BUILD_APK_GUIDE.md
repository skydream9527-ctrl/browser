# APK 构建指南

由于本地环境限制（无 Android SDK 和 Gradle），您可以通过以下方式获取 APK：

## 方案1：上传到 GitHub 后使用 GitHub Actions 自动构建

1. **在 GitHub 创建仓库**
   - 访问 https://github.com/new
   - 创建名为 `browser` 的仓库

2. **推送代码到 GitHub**
   ```bash
   git remote add github https://github.com/yourusername/browser.git
   git push github master
   ```

3. **GitHub Actions 自动构建**
   - 推送后会自动触发构建
   - 访问仓库的 Actions 标签页查看进度
   - 构建完成后下载 APK

## 方案2：使用 Android Studio 本地构建

1. **下载项目**
   ```bash
   git clone https://github.com/yourusername/browser.git
   cd browser
   ```

2. **在 Android Studio 中打开**
   - 打开 Android Studio
   - File -> Open -> 选择项目目录

3. **构建 APK**
   - Build -> Build Bundle(s) / APK(s) -> Build APK(s)
   - APK 将保存在 `app/build/outputs/apk/debug/app-debug.apk`

## 方案3：使用命令行（需要环境配置）

### 前提条件
- 安装 Android SDK
- 安装 Gradle
- 设置环境变量：
  ```bash
  export ANDROID_HOME=/path/to/android-sdk
  export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
  ```

### 构建命令
```bash
# 进入项目目录
cd browser

# 清理并构建
gradle clean
gradle assembleDebug

# 查找 APK
find app/build/outputs/apk -name "*.apk"
```

## 方案4：使用 GitHub Codespaces 在线构建

1. 在 GitHub 仓库页面点击 "Code" -> "Codespaces" -> "Create codespace"
2. 等待环境启动
3. 在终端运行：
   ```bash
   ./gradlew assembleDebug
   ```
4. 下载生成的 APK 文件

## APK 安装

构建完成后，可以通过以下方式安装：

1. **ADB 安装**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. **手动安装**
   - 将 APK 复制到手机
   - 在文件管理器中点击安装
   - 允许安装未知来源应用

## 注意事项

- Debug APK 可以直接安装和测试
- Release APK 需要签名才能发布
- 首次安装可能需要允许安装未知来源应用