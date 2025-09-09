package com.batterymonitor.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.batterymonitor.app.R;
import com.batterymonitor.app.model.TestResult;
import com.batterymonitor.app.utils.ClipboardManager;
import com.batterymonitor.app.utils.DeviceInfoManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestResultDialog {
    
    private Context context;
    private TestResult testResult;
    private ClipboardManager clipboardManager;
    private DeviceInfoManager deviceInfoManager;
    
    public TestResultDialog(Context context, TestResult testResult) {
        this.context = context;
        this.testResult = testResult;
        this.clipboardManager = new ClipboardManager(context);
        this.deviceInfoManager = new DeviceInfoManager(context);
    }
    
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        // 創建自定義布局
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_test_result, null);
        
        // 初始化視圖
        initViews(dialogView);
        
        builder.setView(dialogView);
        builder.setCancelable(true);
        
        AlertDialog dialog = builder.create();
        
        // 設置按鈕點擊事件
        Button btnCopy = dialogView.findViewById(R.id.btn_copy_result);
        Button btnClose = dialogView.findViewById(R.id.btn_close_dialog);
        
        btnCopy.setOnClickListener(v -> {
            clipboardManager.copyTestResult(testResult);
        });
        
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void initViews(View dialogView) {
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvDeviceInfo = dialogView.findViewById(R.id.tv_device_info);
        TextView tvTimeRange = dialogView.findViewById(R.id.tv_time_range);
        TextView tvDuration = dialogView.findViewById(R.id.tv_duration);
        TextView tvBatteryChange = dialogView.findViewById(R.id.tv_battery_change);
        TextView tvConsumption = dialogView.findViewById(R.id.tv_consumption);
        TextView tvConsumptionRate = dialogView.findViewById(R.id.tv_consumption_rate);
        
        // 設置標題
        tvTitle.setText("📊 測試完成");
        
        // 設置設備信息
        tvDeviceInfo.setText(deviceInfoManager.getDeviceModel());
        
        // 設置時間範圍
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String startTime = timeFormat.format(new Date(testResult.getStartTime()));
        String endTime = timeFormat.format(new Date(testResult.getEndTime()));
        tvTimeRange.setText(startTime + " - " + endTime);
        
        // 設置持續時間
        long durationMinutes = testResult.getDuration() / (60 * 1000);
        long durationSeconds = (testResult.getDuration() % (60 * 1000)) / 1000;
        if (durationMinutes > 0) {
            tvDuration.setText(durationMinutes + "分" + durationSeconds + "秒");
        } else {
            tvDuration.setText(durationSeconds + "秒");
        }
        
        // 設置電量變化
        tvBatteryChange.setText(testResult.getStartBatteryLevel() + "% → " + testResult.getEndBatteryLevel() + "%");
        
        // 設置消耗量
        tvConsumption.setText(testResult.getBatteryConsumed() + "%");
        
        // 設置消耗率
        tvConsumptionRate.setText(String.format("%.1f%%/小時", testResult.getConsumptionRate()));
    }
}

