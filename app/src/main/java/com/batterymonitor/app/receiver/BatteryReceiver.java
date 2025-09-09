package com.batterymonitor.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BatteryReceiver";
    
    private BatteryChangeListener listener;
    
    public interface BatteryChangeListener {
        void onBatteryLevelChanged(int level, boolean isCharging);
        void onBatteryLow();
        void onBatteryOkay();
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        
        String action = intent.getAction();
        if (action == null) return;
        
        Log.d(TAG, "Received battery broadcast: " + action);
        
        switch (action) {
            case Intent.ACTION_BATTERY_CHANGED:
                handleBatteryChanged(intent);
                break;
                
            case Intent.ACTION_BATTERY_LOW:
                handleBatteryLow();
                break;
                
            case Intent.ACTION_BATTERY_OKAY:
                handleBatteryOkay();
                break;
        }
    }
    
    private void handleBatteryChanged(Intent intent) {
        try {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            
            if (level != -1 && scale != -1) {
                int batteryPct = Math.round((level / (float) scale) * 100);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                   status == BatteryManager.BATTERY_STATUS_FULL;
                
                Log.d(TAG, String.format("Battery level: %d%%, Charging: %b", batteryPct, isCharging));
                
                if (listener != null) {
                    listener.onBatteryLevelChanged(batteryPct, isCharging);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling battery changed", e);
        }
    }
    
    private void handleBatteryLow() {
        Log.w(TAG, "Battery low warning received");
        
        if (listener != null) {
            listener.onBatteryLow();
        }
    }
    
    private void handleBatteryOkay() {
        Log.d(TAG, "Battery okay notification received");
        
        if (listener != null) {
            listener.onBatteryOkay();
        }
    }
    
    /**
     * 設置電量變化監聽器
     */
    public void setBatteryChangeListener(BatteryChangeListener listener) {
        this.listener = listener;
    }
    
    /**
     * 移除電量變化監聽器
     */
    public void removeBatteryChangeListener() {
        this.listener = null;
    }
}

