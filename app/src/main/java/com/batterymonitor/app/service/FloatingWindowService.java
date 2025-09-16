package com.batterymonitor.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.batterymonitor.app.MainActivity;
import com.batterymonitor.app.R;
import com.batterymonitor.app.dialog.TestResultDialog;
import com.batterymonitor.app.dialog.TestSubjectDialog;
import com.batterymonitor.app.manager.BatteryMonitor;
import com.batterymonitor.app.manager.FeedbackManager;
import com.batterymonitor.app.manager.WakeLockManager;
import com.batterymonitor.app.model.TestResult;
import com.batterymonitor.app.utils.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FloatingWindowService extends Service {
    
    private static final String TAG = "FloatingWindowService";
    
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams layoutParams;
    
    private TextView tvBatteryLevel;
    private TextView tvStatus;
    private Button btnAction;
    private Button btnSettings;
    private Button btnClose;
    
    private BatteryMonitor batteryMonitor;
    private WakeLockManager wakeLockManager;
    private PreferenceManager preferenceManager;
    private FeedbackManager feedbackManager;
    
    private boolean isTestRunning = false;
    private int startBatteryLevel = 100;
    private long startTime = 0;
    private int testDurationMinutes = 30; // 預設30分鐘
    private String currentTestSubject = ""; // 當前測試主題
    private TestSubjectDialog testSubjectDialog;
    
    // UI更新處理器
    private Handler updateHandler;
    private Runnable updateRunnable;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "FloatingWindowService onCreate");
        
        try {
            // 檢查權限
            if (!checkOverlayPermission()) {
                Log.e(TAG, "No overlay permission");
                Toast.makeText(this, "需要浮動窗口權限", Toast.LENGTH_LONG).show();
                stopSelf();
                return;
            }
            
            // 初始化管理器
            batteryMonitor = new BatteryMonitor(this);
            wakeLockManager = new WakeLockManager(this);
            preferenceManager = new PreferenceManager(this);
            feedbackManager = new FeedbackManager(this);
            
            // 讀取測試時長設定
            testDurationMinutes = preferenceManager.getTestDuration();
            
            // 創建浮動窗口
            createFloatingWindow();
            
            // 開始UI更新
            startUIUpdates();
            
            Log.d(TAG, "FloatingWindowService created successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "浮動窗口創建失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }
    
    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }
    
    private void createFloatingWindow() {
        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            
            // 創建浮動視圖
            LayoutInflater inflater = LayoutInflater.from(this);
            floatingView = inflater.inflate(R.layout.floating_window_minimal, null);
            
            // 初始化視圖組件
            initViews();
            
            // 設置窗口參數
            setupWindowParams();
            
            // 添加到窗口管理器
            windowManager.addView(floatingView, layoutParams);
            
            Log.d(TAG, "Floating window added to WindowManager");
            Toast.makeText(this, "浮動窗口已啟動", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating floating window", e);
            Toast.makeText(this, "創建浮動窗口失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
            throw e;
        }
    }
    
    private void initViews() {
        tvBatteryLevel = floatingView.findViewById(R.id.tv_battery_level);
        tvStatus = floatingView.findViewById(R.id.tv_status);
        btnAction = floatingView.findViewById(R.id.btn_action);
        btnSettings = floatingView.findViewById(R.id.btn_settings);
        btnClose = floatingView.findViewById(R.id.btn_close);
        
        // 設置初始值
        updateUI();
        
        // 設置點擊監聽器
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTest();
            }
        });
        
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });
        
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });
        
        // 設置拖拽
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isDragging = false;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = Math.abs(event.getRawX() - initialTouchX);
                        float deltaY = Math.abs(event.getRawY() - initialTouchY);
                        
                        if (deltaX > 10 || deltaY > 10) {
                            isDragging = true;
                            layoutParams.x = (int) (initialX + (event.getRawX() - initialTouchX));
                            layoutParams.y = (int) (initialY + (event.getRawY() - initialTouchY));
                            
                            try {
                                windowManager.updateViewLayout(floatingView, layoutParams);
                            } catch (Exception e) {
                                Log.e(TAG, "Error updating view layout", e);
                            }
                        }
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        return isDragging; // 如果有拖拽，則消費事件
                        
                    default:
                        return false;
                }
            }
        });
    }
    
    private void setupWindowParams() {
        int windowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        
        layoutParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = 100;
        layoutParams.y = 200;
    }
    
    private void toggleTest() {
        try {
            if (!isTestRunning) {
                // 顯示測試主題選擇對話框
                showTestSubjectDialog();
            } else {
                // 停止測試
                stopTest();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping test", e);
            Toast.makeText(this, "停止測試失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showTestSubjectDialog() {
        if (testSubjectDialog == null) {
            testSubjectDialog = new TestSubjectDialog(this);
        }
        
        testSubjectDialog.show(new TestSubjectDialog.OnTestSubjectSelectedListener() {
            @Override
            public void onTestSubjectSelected(String testSubject) {
                currentTestSubject = testSubject;
                startTest();
            }
            
            @Override
            public void onCancelled() {
                // 用戶取消，不執行任何操作
                Log.d(TAG, "Test subject selection cancelled");
            }
        });
    }
    
    private void startTest() {
        try {
            isTestRunning = true;
            
            // 開始測試
            startBatteryLevel = batteryMonitor.getCurrentBatteryLevel();
            startTime = System.currentTimeMillis();
            
            // 重新讀取測試時長設定
            testDurationMinutes = preferenceManager.getTestDuration();
                
            // 獲取WakeLock防止休眠和變暗
            wakeLockManager.acquireWakeLock();
            
            // 播放開始反饋（音效 + 震動）
            if (feedbackManager != null) {
                feedbackManager.playStartFeedback();
            }
            
            String subjectText = currentTestSubject.isEmpty() ? "" : " - " + currentTestSubject;
            Toast.makeText(this, "開始監測 " + testDurationMinutes + " 分鐘" + subjectText, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Test started at battery level: " + startBatteryLevel + "%, duration: " + testDurationMinutes + " minutes, subject: " + currentTestSubject);
            
            updateUI();
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting test", e);
            Toast.makeText(this, "開始測試失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopTest() {
        try {
            isTestRunning = false;
            
            // 停止測試
            wakeLockManager.releaseWakeLock();
            
            // 播放結束反饋（音效 + 震動）
            if (feedbackManager != null) {
                feedbackManager.playEndFeedback();
            }
            
            // 計算測試結果
            int endBatteryLevel = batteryMonitor.getCurrentBatteryLevel();
            long duration = System.currentTimeMillis() - startTime;
            int batteryConsumed = Math.max(0, startBatteryLevel - endBatteryLevel);
            
            // 創建測試結果
            TestResult result = createTestResult(startTime, duration, startBatteryLevel, endBatteryLevel, batteryConsumed);
            
            // 保存到歷史記錄
            preferenceManager.saveTestResult(result);
            
            // 顯示詳細結果
            showTestResult(result);
            
            Log.d(TAG, "Test completed and saved: " + result.toString());
            
            updateUI();
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping test", e);
            Toast.makeText(this, "停止測試失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private TestResult createTestResult(long startTime, long duration, int startBattery, int endBattery, int consumed) {
        TestResult result = new TestResult();
        result.setStartTime(startTime);
        result.setEndTime(startTime + duration);
        result.setDuration(duration);
        result.setStartBatteryLevel(startBattery);
        result.setEndBatteryLevel(endBattery);
        result.setBatteryConsumed(consumed);
        result.setPlannedDuration(testDurationMinutes * 60 * 1000L); // 轉換為毫秒
        result.setTestSubject(currentTestSubject); // 設定測試主題
        return result;
    }
    
    private void showTestResult(TestResult result) {
        try {
            // 啟動MainActivity並顯示結果（不需要重複保存，已在stopTest中保存）
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("show_test_result", true);
            intent.putExtra("test_start_time", result.getStartTime());
            intent.putExtra("test_end_time", result.getEndTime());
            intent.putExtra("test_start_battery", result.getStartBatteryLevel());
            intent.putExtra("test_end_battery", result.getEndBatteryLevel());
            intent.putExtra("test_duration", result.getDuration());
            intent.putExtra("test_subject", result.getTestSubject());
            startActivity(intent);
            
            Log.d(TAG, "Test result activity started: " + result.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing test result", e);
            
            // 如果啟動Activity失敗，回退到Toast
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String startTimeStr = timeFormat.format(new Date(result.getStartTime()));
            String endTimeStr = timeFormat.format(new Date(result.getEndTime()));
            
            long durationMinutes = result.getDuration() / (60 * 1000);
            
            String message = String.format("📊 測試完成\n⏰ %s - %s (%d分鐘)\n🔋 %d%% → %d%% (-%d%%)",
                startTimeStr, endTimeStr, durationMinutes,
                result.getStartBatteryLevel(), result.getEndBatteryLevel(), result.getBatteryConsumed());
            
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
    
    private void openMainActivity() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening main activity", e);
        }
    }
    
    private void startUIUpdates() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    updateUI();
                    
                    // 檢查是否達到設定時間
                    if (isTestRunning && startTime > 0) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        long plannedDuration = testDurationMinutes * 60 * 1000L;
                        
                        if (elapsed >= plannedDuration) {
                            // 自動停止測試
                            toggleTest();
                            Toast.makeText(FloatingWindowService.this, "測試時間到，自動停止", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    updateHandler.postDelayed(this, 1000); // 每1秒更新
                } catch (Exception e) {
                    Log.e(TAG, "Error in UI update", e);
                }
            }
        };
        updateHandler.post(updateRunnable);
    }
    
    private void updateUI() {
        try {
            if (tvBatteryLevel == null || tvStatus == null || btnAction == null) {
                return;
            }
            
            // 更新電量顯示
            int batteryLevel = batteryMonitor.getCurrentBatteryLevel();
            tvBatteryLevel.setText(batteryLevel + "%");
            
            // 更新狀態和按鈕
            if (isTestRunning) {
                long elapsed = (System.currentTimeMillis() - startTime) / (60 * 1000);
                
                // 顯示進度格式: 5/30
                tvStatus.setText(elapsed + "/" + testDurationMinutes);
                btnAction.setText("停止監測");
                
                // 監測中按鈕變紅色
                btnAction.setBackgroundResource(R.drawable.floating_button_running);
                
            } else {
                if (wakeLockManager.isWakeLockHeld()) {
                    tvStatus.setText("螢幕保持明亮");
                } else {
                    tvStatus.setText("待機");
                }
                btnAction.setText("開始監測");
                
                // 待機按鈕恢復藍色
                btnAction.setBackgroundResource(R.drawable.floating_button_primary);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "FloatingWindowService onStartCommand");
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "FloatingWindowService onDestroy");
        
        try {
            // 如果測試正在進行，先保存結果
            if (isTestRunning) {
                int endBatteryLevel = batteryMonitor.getCurrentBatteryLevel();
                long duration = System.currentTimeMillis() - startTime;
                int batteryConsumed = Math.max(0, startBatteryLevel - endBatteryLevel);
                
                TestResult result = createTestResult(startTime, duration, startBatteryLevel, endBatteryLevel, batteryConsumed);
                preferenceManager.saveTestResult(result);
                
                Log.d(TAG, "Test result saved on service destroy");
            }
            
            // 停止UI更新
            if (updateHandler != null && updateRunnable != null) {
                updateHandler.removeCallbacks(updateRunnable);
            }
            
            // 釋放WakeLock
            if (wakeLockManager != null) {
                wakeLockManager.cleanup();
            }
            
            // 釋放FeedbackManager資源
            if (feedbackManager != null) {
                feedbackManager.release();
            }
            
            // 關閉測試主題對話框
            if (testSubjectDialog != null && testSubjectDialog.isShowing()) {
                testSubjectDialog.dismiss();
            }
            
            // 移除浮動窗口
            if (windowManager != null && floatingView != null) {
                windowManager.removeView(floatingView);
                Log.d(TAG, "Floating window removed");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
        
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

