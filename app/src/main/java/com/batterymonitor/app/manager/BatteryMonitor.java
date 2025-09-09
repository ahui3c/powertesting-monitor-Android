package com.batterymonitor.app.manager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryMonitor {
    
    private static final String TAG = "BatteryMonitor";
    
    private Context context;
    private int currentBatteryLevel = 100;
    private boolean isCharging = false;
    
    public BatteryMonitor(Context context) {
        this.context = context;
        updateBatteryInfo();
    }
    
    public void updateBatteryInfo() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);
            
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                
                if (level >= 0 && scale > 0) {
                    currentBatteryLevel = (int) ((level / (float) scale) * 100);
                }
                
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating battery info", e);
        }
    }
    
    public int getCurrentBatteryLevel() {
        updateBatteryInfo();
        return currentBatteryLevel;
    }
    
    public boolean isCharging() {
        updateBatteryInfo();
        return isCharging;
    }
    
    public String getBatteryStatusDescription() {
        updateBatteryInfo();
        if (isCharging) {
            return currentBatteryLevel + "% (充電中)";
        } else {
            return currentBatteryLevel + "%";
        }
    }
}
