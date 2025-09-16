package com.batterymonitor.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.batterymonitor.app.model.TestResult;

import java.util.ArrayList;
import java.util.List;

public class PreferenceManager {
    
    private static final String TAG = "PreferenceManager";
    private static final String PREF_NAME = "battery_monitor_prefs";
    
    // 設定鍵值
    private static final String KEY_TEST_DURATION = "test_duration";
    private static final String KEY_TEST_SUBJECT = "test_subject";
    private static final String KEY_AUTO_START = "auto_start";
    private static final String KEY_FLOATING_WINDOW_ENABLED = "floating_window_enabled";
    private static final String KEY_WINDOW_TRANSPARENCY = "window_transparency";
    private static final String KEY_TEST_HISTORY = "test_history";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_AUTO_COPY = "auto_copy";
    
    // 默認值
    private static final long DEFAULT_TEST_DURATION = 30 * 60 * 1000; // 30分鐘
    private static final boolean DEFAULT_AUTO_START = false;
    private static final boolean DEFAULT_FLOATING_WINDOW_ENABLED = true;
    private static final int DEFAULT_WINDOW_TRANSPARENCY = 90; // 90% 不透明
    private static final int MAX_HISTORY_COUNT = 5; // 最多保存5筆記錄
    
    private SharedPreferences preferences;
    private Context context;
    
    public PreferenceManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    // 測試時長設定 (分鐘)
    public int getTestDuration() {
        return preferences.getInt(KEY_TEST_DURATION, 30); // 預設30分鐘
    }
    
    public void setTestDuration(int durationMinutes) {
        preferences.edit().putInt(KEY_TEST_DURATION, durationMinutes).apply();
        Log.d(TAG, "Test duration set to: " + durationMinutes + " minutes");
    }
    
    // 測試主題設定
    public String getTestSubject() {
        return preferences.getString(KEY_TEST_SUBJECT, "");
    }
    
    public void setTestSubject(String subject) {
        preferences.edit().putString(KEY_TEST_SUBJECT, subject != null ? subject : "").apply();
        Log.d(TAG, "Test subject set to: " + subject);
    }
    
    // 自動啟動設定
    public boolean isAutoStartEnabled() {
        return preferences.getBoolean(KEY_AUTO_START, DEFAULT_AUTO_START);
    }
    
    public void setAutoStartEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_AUTO_START, enabled).apply();
        Log.d(TAG, "Auto start set to: " + enabled);
    }
    
    // 浮動窗口設定
    public boolean isFloatingWindowEnabled() {
        return preferences.getBoolean(KEY_FLOATING_WINDOW_ENABLED, DEFAULT_FLOATING_WINDOW_ENABLED);
    }
    
    public void setFloatingWindowEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_FLOATING_WINDOW_ENABLED, enabled).apply();
        Log.d(TAG, "Floating window enabled set to: " + enabled);
    }
    
    // 窗口透明度設定
    public int getWindowTransparency() {
        return preferences.getInt(KEY_WINDOW_TRANSPARENCY, DEFAULT_WINDOW_TRANSPARENCY);
    }
    
    public void setWindowTransparency(int transparency) {
        preferences.edit().putInt(KEY_WINDOW_TRANSPARENCY, transparency).apply();
        Log.d(TAG, "Window transparency set to: " + transparency + "%");
    }
    
    // 首次運行設定
    public boolean isFirstRun() {
        return preferences.getBoolean(KEY_FIRST_RUN, true);
    }
    
    public void setFirstRun(boolean firstRun) {
        preferences.edit().putBoolean(KEY_FIRST_RUN, firstRun).apply();
    }
    
    // 自動複製設定
    public boolean isAutoCopyEnabled() {
        return preferences.getBoolean(KEY_AUTO_COPY, false);
    }
    
    public void setAutoCopyEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_AUTO_COPY, enabled).apply();
    }
    
    // 測試結果管理
    public void saveTestResult(TestResult result) {
        try {
            List<TestResult> history = getTestHistory();
            
            // 添加新結果到列表開頭
            history.add(0, result);
            
            // 限制最多保存5筆記錄
            if (history.size() > MAX_HISTORY_COUNT) {
                history = history.subList(0, MAX_HISTORY_COUNT);
            }
            
            // 保存到SharedPreferences
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < history.size(); i++) {
                TestResult r = history.get(i);
                if (i > 0) sb.append("|");
                sb.append(r.getStartTime()).append(",")
                  .append(r.getEndTime()).append(",")
                  .append(r.getStartBatteryLevel()).append(",")
                  .append(r.getEndBatteryLevel()).append(",")
                  .append(r.getBatteryConsumed()).append(",")
                  .append(r.getDuration()).append(",")
                  .append(encodeTestSubject(r.getTestSubject()));
            }
            
            preferences.edit().putString(KEY_TEST_HISTORY, sb.toString()).apply();
            Log.d(TAG, "Test result saved: " + result.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving test result", e);
        }
    }
    
    public List<TestResult> getTestHistory() {
        List<TestResult> history = new ArrayList<>();
        
        try {
            String historyStr = preferences.getString(KEY_TEST_HISTORY, "");
            if (!historyStr.isEmpty()) {
                String[] records = historyStr.split("\\|");
                
                for (String record : records) {
                    String[] parts = record.split(",");
                    if (parts.length >= 6) {
                        TestResult result = new TestResult();
                        result.setStartTime(Long.parseLong(parts[0]));
                        result.setEndTime(Long.parseLong(parts[1]));
                        result.setStartBatteryLevel(Integer.parseInt(parts[2]));
                        result.setEndBatteryLevel(Integer.parseInt(parts[3]));
                        result.setBatteryConsumed(Integer.parseInt(parts[4]));
                        result.setDuration(Long.parseLong(parts[5]));
                        
                        // 處理測試主題（如果存在）
                        if (parts.length >= 7) {
                            result.setTestSubject(decodeTestSubject(parts[6]));
                        } else {
                            result.setTestSubject("");
                        }
                        
                        history.add(result);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading test history", e);
        }
        
        return history;
    }
    
    public void clearTestHistory() {
        preferences.edit().remove(KEY_TEST_HISTORY).apply();
        Log.d(TAG, "Test history cleared");
    }
    
    // 測試主題編碼和解碼方法（處理逗號和分隔符）
    private String encodeTestSubject(String subject) {
        if (subject == null || subject.isEmpty()) {
            return "EMPTY";
        }
        // 將逗號和豎線替換為安全字符
        return subject.replace(",", "COMMA").replace("|", "PIPE");
    }
    
    private String decodeTestSubject(String encoded) {
        if (encoded == null || encoded.equals("EMPTY")) {
            return "";
        }
        // 將安全字符還原為原始字符
        return encoded.replace("COMMA", ",").replace("PIPE", "|");
    }
    
    // 統計信息
    public int getTotalTestCount() {
        return getTestHistory().size();
    }
    
    public long getTotalTestDuration() {
        return 0; // 簡化實現
    }
    
    public double getAverageConsumptionRate() {
        return 0.0; // 簡化實現
    }
    
    // 設定導出/導入
    public String exportSettings() {
        return "{}"; // 簡化實現
    }
    
    public boolean importSettings(String jsonString) {
        return false; // 簡化實現
    }
    
    // 統計信息類
    public static class TestStatistics {
        public int totalTests = 0;
        public long totalDuration = 0;
        public double averageConsumption = 0.0;
        public double averageRate = 0.0;
        public double avgBatteryConsumed = 0.0;
        public double avgConsumptionRate = 0.0;
    }
    
    public TestStatistics getTestStatistics() {
        return new TestStatistics(); // 簡化實現
    }
}

