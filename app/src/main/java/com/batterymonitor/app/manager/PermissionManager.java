package com.batterymonitor.app.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

/**
 * 權限管理器
 * 負責檢查和自動引導用戶設定必要權限
 */
public class PermissionManager {
    
    private static final String TAG = "PermissionManager";
    
    private Context context;
    private Activity activity;
    
    // 權限檢查結果
    public static class PermissionStatus {
        public boolean hasOverlayPermission = false;
        public boolean hasBatteryOptimizationDisabled = false;
        public boolean canWriteSettings = false;
        public boolean hasAllRequiredPermissions = false;
        
        @Override
        public String toString() {
            return String.format("Permissions{overlay=%s, battery=%s, settings=%s, all=%s}",
                hasOverlayPermission, hasBatteryOptimizationDisabled, 
                canWriteSettings, hasAllRequiredPermissions);
        }
    }
    
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied(String reason);
        void onPermissionCheckComplete(PermissionStatus status);
    }
    
    public PermissionManager(Context context) {
        this.context = context.getApplicationContext();
        if (context instanceof Activity) {
            this.activity = (Activity) context;
        }
    }
    
    public PermissionManager(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }
    
    /**
     * 檢查所有必要權限
     */
    public PermissionStatus checkAllPermissions() {
        PermissionStatus status = new PermissionStatus();
        
        // 檢查浮動窗口權限
        status.hasOverlayPermission = checkOverlayPermission();
        
        // 檢查電池優化設定
        status.hasBatteryOptimizationDisabled = checkBatteryOptimization();
        
        // 檢查系統設定寫入權限
        status.canWriteSettings = checkWriteSettingsPermission();
        
        // 判斷是否所有必要權限都已獲得
        status.hasAllRequiredPermissions = status.hasOverlayPermission && 
                                         status.hasBatteryOptimizationDisabled;
        
        Log.d(TAG, "Permission check result: " + status);
        return status;
    }
    
    /**
     * 自動請求所有必要權限
     */
    public void requestAllPermissions(PermissionCallback callback) {
        if (activity == null) {
            if (callback != null) {
                callback.onPermissionDenied("需要Activity上下文來請求權限");
            }
            return;
        }
        
        PermissionStatus status = checkAllPermissions();
        
        if (status.hasAllRequiredPermissions) {
            Log.d(TAG, "All permissions already granted");
            if (callback != null) {
                callback.onPermissionGranted();
            }
            return;
        }
        
        // 顯示權限說明對話框
        showPermissionExplanationDialog(status, callback);
    }
    
    /**
     * 檢查浮動窗口權限
     */
    public boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true; // 舊版本默認有權限
    }
    
    /**
     * 請求浮動窗口權限
     */
    public void requestOverlayPermission() {
        if (activity == null) {
            Log.w(TAG, "Cannot request overlay permission without Activity");
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Log.d(TAG, "Requesting overlay permission");
                
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                
                try {
                    activity.startActivity(intent);
                    showToast("請允許「顯示在其他應用程式上層」權限");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open overlay permission settings", e);
                    showToast("無法打開權限設定頁面");
                }
            }
        }
    }
    
    /**
     * 檢查電池優化設定
     */
    public boolean checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
            }
        }
        return true; // 舊版本默認不受電池優化影響
    }
    
    /**
     * 請求忽略電池優化
     */
    public void requestBatteryOptimizationExemption() {
        if (activity == null) {
            Log.w(TAG, "Cannot request battery optimization without Activity");
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
                Log.d(TAG, "Requesting battery optimization exemption");
                
                try {
                    // 方法1：直接請求忽略電池優化
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    activity.startActivity(intent);
                    
                    showToast("請選擇「不優化」以確保應用正常運行");
                    
                } catch (Exception e) {
                    Log.w(TAG, "Direct battery optimization request failed, trying settings page", e);
                    
                    try {
                        // 方法2：打開電池優化設定頁面
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        activity.startActivity(intent);
                        
                        showToast("請找到「電力監測器」並設為「不優化」");
                        
                    } catch (Exception e2) {
                        Log.e(TAG, "Failed to open battery optimization settings", e2);
                        showToast("無法打開電池優化設定");
                    }
                }
            }
        }
    }
    
    /**
     * 檢查系統設定寫入權限
     */
    public boolean checkWriteSettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(context);
        }
        return true; // 舊版本默認有權限
    }
    
    /**
     * 請求系統設定寫入權限
     */
    public void requestWriteSettingsPermission() {
        if (activity == null) {
            Log.w(TAG, "Cannot request write settings permission without Activity");
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Log.d(TAG, "Requesting write settings permission");
                
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                
                try {
                    activity.startActivity(intent);
                    showToast("請允許「修改系統設定」權限以支援螢幕常亮功能");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open write settings permission", e);
                    showToast("無法打開系統設定權限頁面");
                }
            }
        }
    }
    
    /**
     * 顯示權限說明對話框
     */
    private void showPermissionExplanationDialog(PermissionStatus status, PermissionCallback callback) {
        if (activity == null) return;
        
        StringBuilder message = new StringBuilder();
        message.append("為了確保應用正常運行，需要設定以下權限：\n\n");
        
        if (!status.hasOverlayPermission) {
            message.append("🪟 浮動窗口權限\n");
            message.append("- 用於顯示電力監測浮動窗口\n");
            message.append("- 必需權限\n\n");
        }
        
        if (!status.hasBatteryOptimizationDisabled) {
            message.append("🔋 電池優化設定\n");
            message.append("- 防止應用被系統關閉\n");
            message.append("- 確保監測準確性\n");
            message.append("- 必需權限\n\n");
        }
        
        if (!status.canWriteSettings) {
            message.append("⚙️ 系統設定權限\n");
            message.append("- 用於螢幕常亮功能\n");
            message.append("- 可選權限（建議開啟）\n\n");
        }
        
        message.append("點擊「立即設定」將引導您完成權限配置。");
        
        new AlertDialog.Builder(activity)
            .setTitle("權限設定")
            .setMessage(message.toString())
            .setPositiveButton("立即設定", (dialog, which) -> {
                startPermissionSetupProcess(status, callback);
            })
            .setNegativeButton("稍後設定", (dialog, which) -> {
                if (callback != null) {
                    callback.onPermissionDenied("用戶選擇稍後設定權限");
                }
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * 開始權限設定流程
     */
    private void startPermissionSetupProcess(PermissionStatus status, PermissionCallback callback) {
        // 按優先級順序請求權限
        if (!status.hasOverlayPermission) {
            requestOverlayPermission();
        } else if (!status.hasBatteryOptimizationDisabled) {
            requestBatteryOptimizationExemption();
        } else if (!status.canWriteSettings) {
            requestWriteSettingsPermission();
        }
        
        // 設定檢查定時器，等待用戶完成設定
        if (activity != null) {
            activity.runOnUiThread(() -> {
                // 延遲檢查權限狀態
                activity.getWindow().getDecorView().postDelayed(() -> {
                    PermissionStatus newStatus = checkAllPermissions();
                    if (callback != null) {
                        callback.onPermissionCheckComplete(newStatus);
                        
                        if (newStatus.hasAllRequiredPermissions) {
                            callback.onPermissionGranted();
                        }
                    }
                }, 3000); // 3秒後檢查
            });
        }
    }
    
    /**
     * 打開應用設定頁面
     */
    public void openAppSettings() {
        if (activity == null) {
            Log.w(TAG, "Cannot open app settings without Activity");
            return;
        }
        
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            activity.startActivity(intent);
            
            showToast("請在應用設定中配置必要權限");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to open app settings", e);
            showToast("無法打開應用設定頁面");
        }
    }
    
    /**
     * 獲取權限狀態描述
     */
    public String getPermissionStatusDescription() {
        PermissionStatus status = checkAllPermissions();
        StringBuilder sb = new StringBuilder();
        
        sb.append("權限狀態:\n");
        sb.append("浮動窗口: ").append(status.hasOverlayPermission ? "✓ 已授予" : "✗ 未授予").append("\n");
        sb.append("電池優化: ").append(status.hasBatteryOptimizationDisabled ? "✓ 已關閉" : "✗ 未關閉").append("\n");
        sb.append("系統設定: ").append(status.canWriteSettings ? "✓ 已授予" : "✗ 未授予").append("\n");
        sb.append("整體狀態: ").append(status.hasAllRequiredPermissions ? "✓ 完整" : "✗ 不完整");
        
        return sb.toString();
    }
    
    /**
     * 顯示Toast提示
     */
    private void showToast(String message) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.w(TAG, "Failed to show toast: " + message, e);
        }
    }
    
    /**
     * 檢查是否為特定廠商設備，可能需要特殊處理
     */
    public boolean isSpecialDevice() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        return manufacturer.contains("xiaomi") || 
               manufacturer.contains("huawei") || 
               manufacturer.contains("oppo") || 
               manufacturer.contains("vivo") || 
               manufacturer.contains("samsung");
    }
    
    /**
     * 獲取設備特定的權限設定建議
     */
    public String getDeviceSpecificAdvice() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        
        if (manufacturer.contains("xiaomi")) {
            return "小米設備：請在「安全中心」→「應用管理」→「權限」中設定";
        } else if (manufacturer.contains("huawei")) {
            return "華為設備：請在「手機管家」→「應用啟動管理」中設定";
        } else if (manufacturer.contains("oppo")) {
            return "OPPO設備：請在「設定」→「電池」→「應用耗電管理」中設定";
        } else if (manufacturer.contains("vivo")) {
            return "vivo設備：請在「i管家」→「應用管理」→「權限管理」中設定";
        } else if (manufacturer.contains("samsung")) {
            return "三星設備：請在「設定」→「應用程式」→「特殊存取權」中設定";
        } else {
            return "請在系統設定中找到應用權限管理進行設定";
        }
    }
}

