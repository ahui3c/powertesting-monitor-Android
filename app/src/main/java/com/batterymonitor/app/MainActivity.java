package com.batterymonitor.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.batterymonitor.app.dialog.AboutDialog;
import com.batterymonitor.app.dialog.TestResultDialog;
import com.batterymonitor.app.manager.BatteryMonitor;
import com.batterymonitor.app.manager.TestManager;
import com.batterymonitor.app.model.TestResult;
import com.batterymonitor.app.service.FloatingWindowService;
import com.batterymonitor.app.utils.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private static final int REQUEST_OVERLAY_PERMISSION = 1001;
    
    private TextView tvPermissionStatus;
    private Button btnStartFloating;
    private Button btnCheckPermissions;
    private Button btnHistory;
    
    // 設定功能UI組件
    private SeekBar seekbarTestDuration;
    private TextView tvDurationValue;
    private Button btnDuration30;
    private Button btnDuration60;
    private Button btnBatteryOptimization;
    private Button btnExportData;
    private Button btnClearData;
    private Button btnBrightnessCalibration;
    private Button btnAbout;
    
    private BatteryMonitor batteryMonitor;
    private TestManager testManager;
    private PreferenceManager preferenceManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        initializeManagers();
        setupClickListeners();
        updateUI();
        
        // 檢查是否需要顯示測試結果
        checkAndShowTestResult();
    }
    
    private void initializeViews() {
        tvPermissionStatus = findViewById(R.id.tv_permission_status);
        btnStartFloating = findViewById(R.id.btn_start_floating);
        btnCheckPermissions = findViewById(R.id.btn_check_permissions);
        btnHistory = findViewById(R.id.btn_history);
        
        // 設定功能UI組件
        seekbarTestDuration = findViewById(R.id.seekbar_test_duration);
        tvDurationValue = findViewById(R.id.tv_duration_value);
        btnDuration30 = findViewById(R.id.btn_duration_30);
        btnDuration60 = findViewById(R.id.btn_duration_60);
        btnBatteryOptimization = findViewById(R.id.btn_battery_optimization);
        btnExportData = findViewById(R.id.btn_export_data);
        btnClearData = findViewById(R.id.btn_clear_data);
        btnBrightnessCalibration = findViewById(R.id.btn_brightness_calibration);
        btnAbout = findViewById(R.id.btn_about);
    }
    
    private void initializeManagers() {
        batteryMonitor = new BatteryMonitor(this);
        testManager = new TestManager(this, batteryMonitor);
        preferenceManager = new PreferenceManager(this);
    }
    
    private void setupClickListeners() {
        btnStartFloating.setOnClickListener(v -> startFloatingWindow());
        btnCheckPermissions.setOnClickListener(v -> checkAndRequestPermissions());
        btnHistory.setOnClickListener(v -> openHistory());
        
        // 設定功能監聽器
        setupSettingsListeners();
    }
    
    private void setupSettingsListeners() {
        // 測試時長設定
        // 測試時長調整
        seekbarTestDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) progress = 1;
                updateDurationText(progress);
                preferenceManager.setTestDuration(progress);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        

        
        // 快速選擇30分鐘
        btnDuration30.setOnClickListener(v -> {
            seekbarTestDuration.setProgress(30);
            updateDurationText(30);
            preferenceManager.setTestDuration(30);
        });
        
        // 快速選擇60分鐘
        btnDuration60.setOnClickListener(v -> {
            seekbarTestDuration.setProgress(60);
            updateDurationText(60);
            preferenceManager.setTestDuration(60);
        });
        
        // 電池優化設定
        btnBatteryOptimization.setOnClickListener(v -> openBatteryOptimizationSettings());
        
        // 導出數據
        btnExportData.setOnClickListener(v -> exportData());
        
        // 清除數據
        btnClearData.setOnClickListener(v -> showClearDataDialog());
        
        // 校正亮度
        btnBrightnessCalibration.setOnClickListener(v -> openBrightnessCalibration());
        
        // 關於資訊
        btnAbout.setOnClickListener(v -> showAboutDialog());
    }
    
    private void checkAndShowTestResult() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("show_test_result", false)) {
            // 從Intent中獲取測試結果數據
            long startTime = intent.getLongExtra("test_start_time", 0);
            long endTime = intent.getLongExtra("test_end_time", 0);
            int startBattery = intent.getIntExtra("test_start_battery", 0);
            int endBattery = intent.getIntExtra("test_end_battery", 0);
            long duration = intent.getLongExtra("test_duration", 0);
            String testSubject = intent.getStringExtra("test_subject");
            
            if (startTime > 0 && endTime > 0) {
                // 創建TestResult對象
                TestResult result = new TestResult();
                result.setStartTime(startTime);
                result.setEndTime(endTime);
                result.setStartBatteryLevel(startBattery);
                result.setEndBatteryLevel(endBattery);
                result.setDuration(duration);
                result.setTestSubject(testSubject);
                
                // 顯示測試結果對話框
                TestResultDialog dialog = new TestResultDialog(this, result);
                dialog.show();
            }
        }
    }
    
    private void startFloatingWindow() {
        if (checkFloatingPermission()) {
            Intent intent = new Intent(this, FloatingWindowService.class);
            startService(intent);
            Toast.makeText(this, "浮動窗口已啟動", Toast.LENGTH_SHORT).show();
        } else {
            requestFloatingPermission();
        }
    }
    
    private boolean checkFloatingPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }
    
    private void requestFloatingPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        }
    }
    
    private void checkAndRequestPermissions() {
        if (!checkFloatingPermission()) {
            requestFloatingPermission();
        } else {
            Toast.makeText(this, "所有權限已獲得", Toast.LENGTH_SHORT).show();
        }
        updatePermissionStatus();
    }
    
    private void openHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
    
    private void updateUI() {
        // 更新權限狀態
        updatePermissionStatus();
        // 載入設定
        loadSettings();
    }
    
    private void loadSettings() {
        // 載入測試時長設定
        int durationMinutes = preferenceManager.getTestDuration();
        seekbarTestDuration.setMax(120); // 最大120分鐘
        seekbarTestDuration.setProgress(Math.max(1, Math.min(120, durationMinutes)));
        updateDurationText(durationMinutes);
        

    }
    
    private void updateDurationText(int minutes) {
        if (minutes < 60) {
            tvDurationValue.setText(minutes + "分鐘");
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                tvDurationValue.setText(hours + "小時");
            } else {
                tvDurationValue.setText(hours + "小時" + remainingMinutes + "分鐘");
            }
        }
    }
    
    private void openBatteryOptimizationSettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "此功能需要Android 6.0或更高版本", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // 如果無法打開特定設定，嘗試打開通用電池設定
            try {
                Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "無法打開電池設定", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void exportData() {
        try {
            String data = preferenceManager.exportSettings();
            if (data != null) {
                new AlertDialog.Builder(this)
                    .setTitle("導出數據")
                    .setMessage("數據導出功能將在後續版本中實現")
                    .setPositiveButton("確定", null)
                    .show();
            } else {
                Toast.makeText(this, "導出失敗", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "導出過程中發生錯誤", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
            .setTitle("清除數據")
            .setMessage("確定要清除所有測試記錄嗎？此操作無法撤銷。")
            .setPositiveButton("確定", (dialog, which) -> clearData())
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void clearData() {
        try {
            preferenceManager.clearTestHistory();
            Toast.makeText(this, "數據已清除", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "清除數據時發生錯誤", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openBrightnessCalibration() {
        try {
            Intent intent = new Intent(this, BrightnessCalibrationActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("MainActivity", "Error opening brightness calibration", e);
            Toast.makeText(this, "無法開啟校正亮度功能", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showAboutDialog() {
        try {
            AboutDialog dialog = new AboutDialog(this);
            dialog.show();
        } catch (Exception e) {
            Log.e("MainActivity", "Error showing about dialog", e);
            Toast.makeText(this, "無法顯示關於資訊", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updatePermissionStatus() {
        if (checkFloatingPermission()) {
            tvPermissionStatus.setText("✓ 浮動窗口權限已獲得");
            btnCheckPermissions.setVisibility(View.GONE);
        } else {
            tvPermissionStatus.setText("✗ 需要浮動窗口權限");
            btnCheckPermissions.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            updatePermissionStatus();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
