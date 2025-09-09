package com.batterymonitor.app.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class DeviceInfoManager {
    
    private static final String TAG = "DeviceInfoManager";
    
    private Context context;
    
    public DeviceInfoManager(Context context) {
        this.context = context;
    }
    
    /**
     * 獲取設備型號
     */
    public String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }
    
    /**
     * 獲取設備品牌
     */
    public String getDeviceBrand() {
        return capitalize(Build.BRAND);
    }
    
    /**
     * 獲取Android版本
     */
    public String getAndroidVersion() {
        return "Android " + Build.VERSION.RELEASE;
    }
    
    /**
     * 獲取API級別
     */
    public int getApiLevel() {
        return Build.VERSION.SDK_INT;
    }
    
    /**
     * 獲取設備完整信息
     */
    public String getDeviceInfo() {
        return String.format("%s (%s)", getDeviceModel(), getAndroidVersion());
    }
    
    /**
     * 獲取詳細設備信息
     */
    public String getDetailedDeviceInfo() {
        return String.format(
            "設備: %s\n系統: %s (API %d)\n品牌: %s",
            getDeviceModel(),
            getAndroidVersion(),
            getApiLevel(),
            getDeviceBrand()
        );
    }
    
    /**
     * 首字母大寫
     */
    private String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    /**
     * 獲取用於複製的設備信息
     */
    public String getDeviceInfoForCopy() {
        return getDeviceModel();
    }
}

