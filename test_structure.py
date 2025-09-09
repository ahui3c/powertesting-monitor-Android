#!/usr/bin/env python3
"""
Android電力監測應用項目結構檢查腳本
"""

import os
import sys

def check_file_exists(file_path, description):
    """檢查文件是否存在"""
    if os.path.exists(file_path):
        print(f"✓ {description}: {file_path}")
        return True
    else:
        print(f"✗ {description}: {file_path} (缺失)")
        return False

def check_directory_exists(dir_path, description):
    """檢查目錄是否存在"""
    if os.path.isdir(dir_path):
        print(f"✓ {description}: {dir_path}")
        return True
    else:
        print(f"✗ {description}: {dir_path} (缺失)")
        return False

def main():
    """主函數"""
    print("Android電力監測應用項目結構檢查")
    print("=" * 50)
    
    base_path = "/home/ubuntu/BatteryMonitorApp"
    
    if not os.path.exists(base_path):
        print(f"錯誤：項目根目錄不存在 {base_path}")
        sys.exit(1)
    
    os.chdir(base_path)
    
    # 檢查項目結構
    checks = []
    
    # 根級文件
    checks.append(check_file_exists("build.gradle", "根級構建文件"))
    checks.append(check_file_exists("settings.gradle", "設置文件"))
    checks.append(check_file_exists("gradle.properties", "Gradle屬性文件"))
    
    # 應用模塊
    checks.append(check_directory_exists("app", "應用模塊目錄"))
    checks.append(check_file_exists("app/build.gradle", "應用構建文件"))
    checks.append(check_file_exists("app/proguard-rules.pro", "ProGuard規則文件"))
    
    # AndroidManifest
    checks.append(check_file_exists("app/src/main/AndroidManifest.xml", "Android清單文件"))
    
    # Java源代碼
    java_base = "app/src/main/java/com/batterymonitor/app"
    checks.append(check_file_exists(f"{java_base}/MainActivity.java", "主活動"))
    checks.append(check_file_exists(f"{java_base}/SettingsActivity.java", "設定活動"))
    checks.append(check_file_exists(f"{java_base}/HistoryActivity.java", "歷史記錄活動"))
    
    # 服務和管理器
    checks.append(check_file_exists(f"{java_base}/service/FloatingWindowService.java", "浮動窗口服務"))
    checks.append(check_file_exists(f"{java_base}/manager/BatteryMonitor.java", "電量監測管理器"))
    checks.append(check_file_exists(f"{java_base}/manager/TestManager.java", "測試管理器"))
    
    # 廣播接收器
    checks.append(check_file_exists(f"{java_base}/receiver/BatteryReceiver.java", "電量廣播接收器"))
    checks.append(check_file_exists(f"{java_base}/receiver/BootReceiver.java", "開機廣播接收器"))
    
    # 數據模型和工具
    checks.append(check_file_exists(f"{java_base}/model/TestResult.java", "測試結果數據模型"))
    checks.append(check_file_exists(f"{java_base}/utils/PreferenceManager.java", "偏好設定管理器"))
    checks.append(check_file_exists(f"{java_base}/adapter/TestResultAdapter.java", "測試結果適配器"))
    
    # 資源文件
    res_base = "app/src/main/res"
    
    # 布局文件
    checks.append(check_file_exists(f"{res_base}/layout/activity_main.xml", "主界面布局"))
    checks.append(check_file_exists(f"{res_base}/layout/activity_settings.xml", "設定界面布局"))
    checks.append(check_file_exists(f"{res_base}/layout/activity_history.xml", "歷史記錄界面布局"))
    checks.append(check_file_exists(f"{res_base}/layout/floating_window_layout.xml", "浮動窗口布局"))
    checks.append(check_file_exists(f"{res_base}/layout/item_test_result.xml", "測試結果項目布局"))
    
    # 值資源
    checks.append(check_file_exists(f"{res_base}/values/strings.xml", "字符串資源"))
    checks.append(check_file_exists(f"{res_base}/values/colors.xml", "顏色資源"))
    checks.append(check_file_exists(f"{res_base}/values/themes.xml", "主題資源"))
    
    # Drawable資源
    drawable_files = [
        "floating_window_background.xml",
        "status_indicator.xml",
        "floating_button_background.xml",
        "floating_button_secondary_background.xml",
        "consumption_badge.xml",
        "ic_battery.xml",
        "ic_close.xml",
        "ic_settings.xml",
        "ic_history.xml",
        "ic_launcher_background.xml",
        "ic_launcher_foreground.xml"
    ]
    
    for drawable in drawable_files:
        checks.append(check_file_exists(f"{res_base}/drawable/{drawable}", f"Drawable資源: {drawable}"))
    
    # XML配置文件
    checks.append(check_file_exists(f"{res_base}/xml/backup_rules.xml", "備份規則"))
    checks.append(check_file_exists(f"{res_base}/xml/data_extraction_rules.xml", "數據提取規則"))
    
    # 圖標文件
    checks.append(check_file_exists(f"{res_base}/mipmap-anydpi-v26/ic_launcher.xml", "應用圖標"))
    checks.append(check_file_exists(f"{res_base}/mipmap-anydpi-v26/ic_launcher_round.xml", "圓形應用圖標"))
    
    # 統計結果
    total_checks = len(checks)
    passed_checks = sum(checks)
    
    print("\n" + "=" * 50)
    print(f"檢查完成：{passed_checks}/{total_checks} 項通過")
    
    if passed_checks == total_checks:
        print("✓ 所有必要文件都存在，項目結構完整！")
        return 0
    else:
        print(f"✗ 有 {total_checks - passed_checks} 個文件缺失")
        return 1

if __name__ == "__main__":
    sys.exit(main())

