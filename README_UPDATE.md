# TraeNoteDemo 远程更新功能使用指南

## 概述

本应用使用GitHub作为免费的更新服务器，通过在GitHub仓库中存储应用的更新包和版本信息，实现应用的远程更新功能。

## 配置步骤

### 1. 创建GitHub仓库

如果您还没有GitHub仓库，请先创建一个公开的GitHub仓库，用于存储应用的更新包和版本信息。

### 2. 修改UpdateManager.kt中的仓库信息

打开`app/src/main/java/com/harry/navigation/traenotedemo/util/UpdateManager.kt`文件，修改以下常量：

```kotlin
private const val DEFAULT_REPO_OWNER = "your-github-username" // 修改为您的GitHub用户名
private const val DEFAULT_REPO_NAME = "TraeNoteDemo" // 修改为您的仓库名称
private const val DEFAULT_UPDATE_FILE = "update.json" // 更新信息文件名，可以保持默认
```

### 3. 创建更新信息文件

在GitHub仓库的根目录下创建一个`update.json`文件，内容格式如下：

```json
{
  "versionCode": 2,
  "versionName": "1.1",
  "updateMessage": "1. 修复了一些已知问题\n2. 优化了应用性能\n3. 添加了新功能",
  "apkUrl": "https://github.com/your-github-username/TraeNoteDemo/releases/download/v1.1/app-release.apk"
}
```

参数说明：
- `versionCode`：应用的版本号，必须大于当前应用的版本号才会触发更新
- `versionName`：应用的版本名称，显示给用户看的版本号
- `updateMessage`：更新内容说明，支持换行符`\n`
- `apkUrl`：APK下载地址，可以使用GitHub Releases功能发布APK，然后使用下载链接

### 4. 发布应用更新

1. 修改应用的`versionCode`和`versionName`（在`app/build.gradle.kts`文件中）
2. 构建新版本的APK文件
3. 在GitHub仓库中创建一个新的Release，上传APK文件
4. 更新`update.json`文件中的版本信息和下载链接

## 使用方法

用户可以通过以下方式检查和安装更新：

1. 打开应用的设置界面
2. 在"应用更新"卡片中点击"检查更新