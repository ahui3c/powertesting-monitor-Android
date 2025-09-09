package com.batterymonitor.app.manager;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class WakeLockManager {
    
    private static final String TAG = "WakeLockManager";
    private static final String WAKE_LOCK_TAG = "BatteryMonitor:ScreenOn";
    
    private Context context;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private boolean isWakeLockHeld = false;
    
    public WakeLockManager(Context context) {
        this.context = context;
        this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }
    
    public void acquireWakeLock() {
        try {
            if (powerManager != null && !isWakeLockHeld) {
                // 使用SCREEN_BRIGHT_WAKE_LOCK來完全防止螢幕變暗
                wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    WAKE_LOCK_TAG
                );
                
                if (wakeLock != null) {
                    wakeLock.acquire(30 * 60 * 1000L);
                    isWakeLockHeld = true;
                    Log.d(TAG, "WakeLock acquired - screen will stay bright");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error acquiring WakeLock", e);
        }
    }
    
    public void releaseWakeLock() {
        try {
            if (wakeLock != null && isWakeLockHeld) {
                wakeLock.release();
                isWakeLockHeld = false;
                wakeLock = null;
                Log.d(TAG, "WakeLock released - screen can sleep");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error releasing WakeLock", e);
        }
    }
    
    public boolean isWakeLockHeld() {
        return isWakeLockHeld && wakeLock != null && wakeLock.isHeld();
    }
    
    public String getWakeLockStatus() {
        if (isWakeLockHeld()) {
            return "螢幕保持明亮";
        } else {
            return "允許螢幕變暗";
        }
    }
    
    public void cleanup() {
        releaseWakeLock();
    }
}
