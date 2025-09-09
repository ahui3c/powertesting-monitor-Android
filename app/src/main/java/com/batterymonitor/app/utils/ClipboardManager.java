package com.batterymonitor.app.utils;

import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.batterymonitor.app.model.TestResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 剪貼簿管理器
 * 負責處理測試結果的複製功能
 */
public class ClipboardManager {
    
    private static final String TAG = "ClipboardManager";
    
    private Context context;
    private android.content.ClipboardManager systemClipboard;
    
    public ClipboardManager(Context context) {
        this.context = context.getApplicationContext();
        this.systemClipboard = (android.content.ClipboardManager) 
            context.getSystemService(Context.CLIPBOARD_SERVICE);
    }
    
    /**
     * 複製測試結果到剪貼簿
     */
    public boolean copyTestResult(TestResult result) {
        if (result == null) {
            Log.w(TAG, "Cannot copy null test result");
            return false;
        }
        
        try {
            String formattedResult = formatTestResult(result);
            
            ClipData clipData = ClipData.newPlainText("電力測試結果", formattedResult);
            systemClipboard.setPrimaryClip(clipData);
            
            // 顯示成功提示
            showToast("測試結果已複製到剪貼簿");
            
            Log.d(TAG, "Test result copied to clipboard successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy test result to clipboard", e);
            showToast("複製失敗");
            return false;
        }
    }
    
    /**
     * 複製簡化版測試結果
     */
    public boolean copyTestResultSimple(TestResult result) {
        if (result == null) {
            return false;
        }
        
        try {
            String simpleResult = formatTestResultSimple(result);
            
            ClipData clipData = ClipData.newPlainText("電力測試", simpleResult);
            systemClipboard.setPrimaryClip(clipData);
            
            showToast("結果已複製");
            
            Log.d(TAG, "Simple test result copied to clipboard");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy simple test result", e);
            return false;
        }
    }
    
    /**
     * 複製當前測試狀態
     */
    public boolean copyCurrentStatus(int batteryLevel, long elapsedTime, int consumed) {
        try {
            String status = formatCurrentStatus(batteryLevel, elapsedTime, consumed);
            
            ClipData clipData = ClipData.newPlainText("電力監測狀態", status);
            systemClipboard.setPrimaryClip(clipData);
            
            showToast("狀態已複製");
            
            Log.d(TAG, "Current status copied to clipboard");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy current status", e);
            return false;
        }
    }
    
    /**
     * 格式化完整測試結果
     */
    private String formatTestResult(TestResult result) {
        StringBuilder sb = new StringBuilder();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        
        // 獲取設備信息
        DeviceInfoManager deviceInfo = new DeviceInfoManager(context);
        
        sb.append("📱 電力監測測試結果\n");
        sb.append("═══════════════════\n\n");
        
        // 設備信息
        sb.append("📱 設備信息:\n");
        sb.append("型號: ").append(deviceInfo.getDeviceModel()).append("\n");
        sb.append("系統: ").append(deviceInfo.getAndroidVersion()).append("\n\n");
        
        // 測試時間
        sb.append("🕐 測試時間:\n");
        sb.append("開始: ").append(dateFormat.format(new Date(result.getStartTime()))).append("\n");
        sb.append("結束: ").append(dateFormat.format(new Date(result.getEndTime()))).append("\n");
        sb.append("持續: ").append(formatDuration(result.getActualDuration())).append("\n\n");
        
        // 電量信息
        sb.append("🔋 電量變化:\n");
        sb.append("開始電量: ").append(result.getStartBatteryLevel()).append("%\n");
        sb.append("結束電量: ").append(result.getEndBatteryLevel()).append("%\n");
        sb.append("消耗電量: ").append(result.getBatteryConsumed()).append("%\n\n");
        
        // 統計信息
        sb.append("📊 統計數據:\n");
        sb.append("消耗率: ").append(String.format("%.2f", result.getConsumptionRate())).append("%/小時\n\n");
        
        // 測試條件
        sb.append("⚙️ 測試條件:\n");
        sb.append("測試應用: 電力監測器\n");
        sb.append("測試模式: 螢幕常亮監測\n");
        
        sb.append("\n───────────────────\n");
        sb.append("由電力監測器生成 ").append(dateFormat.format(new Date()));
        
        return sb.toString();
    }
    
    /**
     * 格式化簡化版測試結果
     */
    private String formatTestResultSimple(TestResult result) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        return String.format("電力測試 %s-%s: %d%%→%d%% (-%d%%) %s",
            timeFormat.format(new Date(result.getStartTime())),
            timeFormat.format(new Date(result.getEndTime())),
            result.getStartBatteryLevel(),
            result.getEndBatteryLevel(),
            result.getBatteryConsumed(),
            formatDurationShort(result.getActualDuration())
        );
    }
    
    /**
     * 格式化當前狀態
     */
    private String formatCurrentStatus(int batteryLevel, long elapsedTime, int consumed) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        
        return String.format("電力監測 %s: %d%% (-%d%%) %s",
            timeFormat.format(new Date()),
            batteryLevel,
            consumed,
            formatDurationShort(elapsedTime)
        );
    }
    
    /**
     * 格式化持續時間
     */
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds = seconds % 60;
        minutes = minutes % 60;
        
        if (hours > 0) {
            return String.format("%d小時%d分%d秒", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d分%d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }
    
    /**
     * 格式化短持續時間
     */
    private String formatDurationShort(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds = seconds % 60;
        minutes = minutes % 60;
        
        if (hours > 0) {
            return String.format("%dh%dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm%ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * 顯示Toast提示
     */
    private void showToast(String message) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.w(TAG, "Failed to show toast: " + message, e);
        }
    }
    
    /**
     * 檢查剪貼簿是否可用
     */
    public boolean isClipboardAvailable() {
        return systemClipboard != null;
    }
    
    /**
     * 獲取剪貼簿內容（用於調試）
     */
    public String getClipboardContent() {
        try {
            if (systemClipboard != null && systemClipboard.hasPrimaryClip()) {
                ClipData clipData = systemClipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    ClipData.Item item = clipData.getItemAt(0);
                    if (item != null && item.getText() != null) {
                        return item.getText().toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get clipboard content", e);
        }
        return null;
    }
    
    /**
     * 清空剪貼簿
     */
    public void clearClipboard() {
        try {
            ClipData clipData = ClipData.newPlainText("", "");
            systemClipboard.setPrimaryClip(clipData);
            Log.d(TAG, "Clipboard cleared");
        } catch (Exception e) {
            Log.w(TAG, "Failed to clear clipboard", e);
        }
    }
}

