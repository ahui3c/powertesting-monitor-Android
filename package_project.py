#!/usr/bin/env python3
"""
Android電力監測應用項目打包腳本
"""

import os
import shutil
import zipfile
import datetime
import sys

def create_package():
    """創建項目打包"""
    
    print("Android電力監測應用項目打包")
    print("=" * 50)
    
    # 獲取當前時間戳
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    
    # 項目根目錄
    project_root = "/home/ubuntu/BatteryMonitorApp"
    
    # 打包目錄
    package_dir = f"/home/ubuntu/BatteryMonitorApp_Package_{timestamp}"
    
    # 創建打包目錄
    if os.path.exists(package_dir):
        shutil.rmtree(package_dir)
    os.makedirs(package_dir)
    
    print(f"創建打包目錄：{package_dir}")
    
    # 需要打包的文件和目錄
    items_to_package = [
        # 項目文件
        "app/",
        "build.gradle",
        "settings.gradle",
        "gradle.properties",
        
        # 文檔文件
        "README.md",
        "INSTALL.md",
        
        # 測試腳本
        "test_structure.py",
        "package_project.py"
    ]
    
    # 複製文件
    for item in items_to_package:
        src_path = os.path.join(project_root, item)
        dst_path = os.path.join(package_dir, item)
        
        if os.path.exists(src_path):
            if os.path.isdir(src_path):
                print(f"複製目錄：{item}")
                shutil.copytree(src_path, dst_path)
            else:
                print(f"複製文件：{item}")
                # 確保目標目錄存在
                dst_dir = os.path.dirname(dst_path)
                if dst_dir and not os.path.exists(dst_dir):
                    os.makedirs(dst_dir)
                shutil.copy2(src_path, dst_path)
        else:
            print(f"警告：文件不存在 {item}")
    
    # 創建項目信息文件
    create_project_info(package_dir)
    
    # 創建ZIP壓縮包
    zip_path = f"/home/ubuntu/BatteryMonitorApp_{timestamp}.zip"
    create_zip_package(package_dir, zip_path)
    
    # 生成項目統計
    generate_statistics(package_dir)
    
    print(f"\n✓ 項目打包完成！")
    print(f"打包目錄：{package_dir}")
    print(f"壓縮文件：{zip_path}")
    
    return package_dir, zip_path

def create_project_info(package_dir):
    """創建項目信息文件"""
    
    info_content = f"""# Android電力監測器項目信息

## 項目概述
- **項目名稱**：Android電力監測器 (Battery Monitor App)
- **版本**：1.0
- **打包時間**：{datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
- **目標平台**：Android 6.0+ (API 23+)

## 項目結構
```
BatteryMonitorApp/
├── app/                          # 應用模塊
│   ├── build.gradle             # 應用構建配置
│   ├── proguard-rules.pro       # ProGuard規則
│   └── src/main/
│       ├── AndroidManifest.xml  # 應用清單
│       ├── java/                # Java源代碼
│       │   └── com/batterymonitor/app/
│       │       ├── MainActivity.java
│       │       ├── SettingsActivity.java
│       │       ├── HistoryActivity.java
│       │       ├── service/     # 服務類
│       │       ├── manager/     # 管理器類
│       │       ├── receiver/    # 廣播接收器
│       │       ├── model/       # 數據模型
│       │       ├── utils/       # 工具類
│       │       └── adapter/     # 適配器類
│       └── res/                 # 資源文件
│           ├── layout/          # 布局文件
│           ├── values/          # 值資源
│           ├── drawable/        # 圖形資源
│           ├── mipmap-*/        # 應用圖標
│           └── xml/             # XML配置
├── build.gradle                 # 根級構建配置
├── settings.gradle              # 項目設置
├── gradle.properties            # Gradle屬性
├── README.md                    # 項目說明
├── INSTALL.md                   # 安裝說明
└── 工具腳本/                    # 開發工具
```

## 主要功能
1. **電力監測**：實時監測電池電量變化
2. **浮動窗口**：最上層顯示監測狀態
3. **數據記錄**：保存測試結果和統計信息
4. **個性化設定**：自定義測試參數和界面

## 技術特點
- **架構**：MVVM + 服務導向
- **UI框架**：Material Design
- **數據存儲**：SharedPreferences
- **權限管理**：動態權限申請
- **性能優化**：前台服務 + 電量優化

## 編譯說明
1. 使用Android Studio打開項目
2. 確保SDK版本：compileSdk 34, minSdk 23
3. 運行 `./gradlew assembleDebug` 編譯
4. APK文件位於 `app/build/outputs/apk/debug/`

## 安裝要求
- Android 6.0+ (API 23+)
- 約5MB存儲空間
- 浮動窗口權限
- 建議關閉電池優化

## 使用說明
詳細使用說明請參考 INSTALL.md 文件

## 開發者信息
- **開發工具**：Android Studio
- **編程語言**：Java 8
- **構建工具**：Gradle 8.0.2
- **目標SDK**：Android 14 (API 34)

---
此項目為開源項目，僅供學習和研究使用。
"""
    
    info_path = os.path.join(package_dir, "PROJECT_INFO.md")
    with open(info_path, 'w', encoding='utf-8') as f:
        f.write(info_content)
    
    print("✓ 創建項目信息文件")

def create_zip_package(source_dir, zip_path):
    """創建ZIP壓縮包"""
    
    print(f"創建壓縮包：{zip_path}")
    
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, dirs, files in os.walk(source_dir):
            for file in files:
                file_path = os.path.join(root, file)
                arc_path = os.path.relpath(file_path, source_dir)
                zipf.write(file_path, arc_path)
    
    # 獲取壓縮包大小
    size_mb = os.path.getsize(zip_path) / (1024 * 1024)
    print(f"✓ 壓縮包大小：{size_mb:.2f} MB")

def generate_statistics(package_dir):
    """生成項目統計信息"""
    
    print("\n項目統計信息：")
    print("-" * 30)
    
    # 統計文件數量
    total_files = 0
    java_files = 0
    xml_files = 0
    
    for root, dirs, files in os.walk(package_dir):
        for file in files:
            total_files += 1
            if file.endswith('.java'):
                java_files += 1
            elif file.endswith('.xml'):
                xml_files += 1
    
    print(f"總文件數：{total_files}")
    print(f"Java文件：{java_files}")
    print(f"XML文件：{xml_files}")
    
    # 統計代碼行數
    java_lines = 0
    java_dir = os.path.join(package_dir, "app/src/main/java")
    
    if os.path.exists(java_dir):
        for root, dirs, files in os.walk(java_dir):
            for file in files:
                if file.endswith('.java'):
                    file_path = os.path.join(root, file)
                    try:
                        with open(file_path, 'r', encoding='utf-8') as f:
                            java_lines += len(f.readlines())
                    except:
                        pass
    
    print(f"Java代碼行數：約{java_lines}行")
    
    # 計算目錄大小
    total_size = 0
    for root, dirs, files in os.walk(package_dir):
        for file in files:
            file_path = os.path.join(root, file)
            try:
                total_size += os.path.getsize(file_path)
            except:
                pass
    
    size_mb = total_size / (1024 * 1024)
    print(f"項目大小：{size_mb:.2f} MB")

def main():
    """主函數"""
    try:
        package_dir, zip_path = create_package()
        
        print(f"\n🎉 打包成功！")
        print(f"📁 項目目錄：{package_dir}")
        print(f"📦 壓縮文件：{zip_path}")
        print(f"\n📋 下一步：")
        print(f"1. 使用Android Studio打開項目目錄")
        print(f"2. 編譯生成APK文件")
        print(f"3. 參考INSTALL.md進行安裝")
        
        return 0
        
    except Exception as e:
        print(f"❌ 打包失敗：{e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())

