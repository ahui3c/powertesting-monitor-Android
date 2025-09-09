package com.batterymonitor.app.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 電力測試結果數據模型
 * 包含複製功能所需的計算方法
 */
public class TestResult {
    
    private long startTime;         // 開始時間戳
    private long endTime;           // 結束時間戳
    private int startBatteryLevel;  // 開始電量百分比
    private int endBatteryLevel;    // 結束電量百分比
    private long plannedDuration;   // 計劃測試時長（毫秒）
    private long actualDuration;    // 實際測試時長（毫秒）
    
    // 構造函數
    public TestResult() {
    }
    
    public TestResult(long startTime, long endTime, int startBatteryLevel, 
                     int endBatteryLevel, long plannedDuration, long actualDuration) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startBatteryLevel = startBatteryLevel;
        this.endBatteryLevel = endBatteryLevel;
        this.plannedDuration = plannedDuration;
        this.actualDuration = actualDuration;
    }
    
    // Getter和Setter方法
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public int getStartBatteryLevel() {
        return startBatteryLevel;
    }
    
    public void setStartBatteryLevel(int startBatteryLevel) {
        this.startBatteryLevel = startBatteryLevel;
    }
    
    public int getEndBatteryLevel() {
        return endBatteryLevel;
    }
    
    public void setEndBatteryLevel(int endBatteryLevel) {
        this.endBatteryLevel = endBatteryLevel;
    }
    
    public long getPlannedDuration() {
        return plannedDuration;
    }
    
    public void setPlannedDuration(long plannedDuration) {
        this.plannedDuration = plannedDuration;
    }
    
    public long getActualDuration() {
        return actualDuration;
    }
    
    public void setActualDuration(long actualDuration) {
        this.actualDuration = actualDuration;
    }
    
    // 兼容性方法
    public void setDuration(long duration) {
        this.actualDuration = duration;
    }
    
    public long getDuration() {
        return actualDuration;
    }
    
    public void setBatteryConsumed(int consumed) {
        // 這個方法不需要實際設置值，因為getBatteryConsumed()是計算得出的
        // 保留此方法僅為兼容性
    }
    
    // 計算方法
    
    /**
     * 獲取電量消耗百分比
     */
    public int getBatteryConsumed() {
        return Math.max(0, startBatteryLevel - endBatteryLevel);
    }
    
    /**
     * 獲取電量消耗率（每小時消耗百分比）
     */
    public double getConsumptionRate() {
        if (actualDuration <= 0) {
            return 0.0;
        }
        
        double hoursElapsed = actualDuration / (1000.0 * 60.0 * 60.0);
        return getBatteryConsumed() / hoursElapsed;
    }
    
    /**
     * 獲取測試完成度百分比
     */
    public double getCompletionPercentage() {
        if (plannedDuration <= 0) {
            return 100.0;
        }
        
        return Math.min(100.0, (actualDuration * 100.0) / plannedDuration);
    }
    
    /**
     * 判斷測試是否提前結束
     */
    public boolean isEarlyTermination() {
        return actualDuration < plannedDuration * 0.9; // 少於90%視為提前結束
    }
    
    /**
     * 獲取效率評級
     */
    public String getEfficiencyRating() {
        double rate = getConsumptionRate();
        
        if (rate <= 5.0) {
            return "優秀";
        } else if (rate <= 10.0) {
            return "良好";
        } else if (rate <= 20.0) {
            return "一般";
        } else if (rate <= 30.0) {
            return "較差";
        } else {
            return "很差";
        }
    }
    
    /**
     * 獲取預估剩餘使用時間（基於當前消耗率）
     */
    public long getEstimatedRemainingTime() {
        double rate = getConsumptionRate();
        if (rate <= 0) {
            return Long.MAX_VALUE;
        }
        
        // 假設當前電量為endBatteryLevel，計算到5%的剩餘時間
        int usableBattery = Math.max(0, endBatteryLevel - 5);
        double hoursRemaining = usableBattery / rate;
        
        return (long) (hoursRemaining * 60 * 60 * 1000); // 轉換為毫秒
    }
    
    /**
     * 格式化開始時間
     */
    public String getFormattedStartTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(startTime));
    }
    
    /**
     * 格式化結束時間
     */
    public String getFormattedEndTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(endTime));
    }
    
    /**
     * 格式化測試持續時間
     */
    public String getFormattedDuration() {
        return formatDuration(actualDuration);
    }
    
    /**
     * 格式化計劃持續時間
     */
    public String getFormattedPlannedDuration() {
        return formatDuration(plannedDuration);
    }
    
    /**
     * 格式化時間長度
     */
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds = seconds % 60;
        minutes = minutes % 60;
        
        if (hours > 0) {
            return String.format("%d小時%d分鐘", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d分鐘%d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }
    
    /**
     * 格式化消耗率
     */
    public String getFormattedConsumptionRate() {
        return String.format("%.1f%%/h", getConsumptionRate());
    }
    
    /**
     * 格式化短開始時間
     */
    public String getFormattedStartTimeShort() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(startTime));
    }
    
    /**
     * 格式化短結束時間
     */
    public String getFormattedEndTimeShort() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(endTime));
    }
    
    /**
     * 獲取簡短摘要
     */
    public String getSummary() {
        return String.format("消耗%d%% (%s) - %s", 
            getBatteryConsumed(), 
            getFormattedDuration(), 
            getEfficiencyRating());
    }
    
    /**
     * 檢查測試結果是否有效
     */
    public boolean isValid() {
        return startTime > 0 && 
               endTime > startTime && 
               startBatteryLevel >= 0 && startBatteryLevel <= 100 &&
               endBatteryLevel >= 0 && endBatteryLevel <= 100 &&
               actualDuration > 0;
    }
    
    @Override
    public String toString() {
        return String.format("TestResult{start=%s, end=%s, battery=%d%%→%d%%, duration=%s}", 
            getFormattedStartTime(), 
            getFormattedEndTime(),
            startBatteryLevel, 
            endBatteryLevel, 
            getFormattedDuration());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TestResult that = (TestResult) obj;
        return startTime == that.startTime &&
               endTime == that.endTime &&
               startBatteryLevel == that.startBatteryLevel &&
               endBatteryLevel == that.endBatteryLevel;
    }
    
    @Override
    public int hashCode() {
        int result = Long.hashCode(startTime);
        result = 31 * result + Long.hashCode(endTime);
        result = 31 * result + startBatteryLevel;
        result = 31 * result + endBatteryLevel;
        return result;
    }
}

