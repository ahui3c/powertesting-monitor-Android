package com.batterymonitor.app.manager;

import android.content.Context;
import android.util.Log;

public class TestManager {
    
    private static final String TAG = "TestManager";
    
    private Context context;
    private BatteryMonitor batteryMonitor;
    
    private boolean isTestRunning = false;
    private int startBatteryLevel = 100;
    private long startTime = 0;
    
    public TestManager(Context context, BatteryMonitor batteryMonitor) {
        this.context = context;
        this.batteryMonitor = batteryMonitor;
    }
    
    public void startTest() {
        try {
            if (!isTestRunning) {
                isTestRunning = true;
                startBatteryLevel = batteryMonitor.getCurrentBatteryLevel();
                startTime = System.currentTimeMillis();
                Log.d(TAG, "Test started at battery level: " + startBatteryLevel + "%");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting test", e);
        }
    }
    
    public void stopTest() {
        try {
            if (isTestRunning) {
                isTestRunning = false;
                Log.d(TAG, "Test stopped");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping test", e);
        }
    }
    
    public boolean isTestRunning() {
        return isTestRunning;
    }
    
    public boolean hasCompletedTest() {
        return !isTestRunning && startTime > 0;
    }
    
    public int getLastTestBatteryConsumed() {
        if (startTime > 0) {
            return Math.max(0, startBatteryLevel - batteryMonitor.getCurrentBatteryLevel());
        }
        return 0;
    }
    
    public long getLastTestDuration() {
        if (startTime > 0) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
    
    public int getBatteryConsumed() {
        return getLastTestBatteryConsumed();
    }
    
    public long getElapsedTime() {
        return getLastTestDuration();
    }
    
    public String getWakeLockStatus() {
        return "正常";
    }
    
    public boolean canWriteSettings() {
        return true;
    }
}
