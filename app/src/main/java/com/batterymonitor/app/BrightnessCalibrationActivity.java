package com.batterymonitor.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 校正亮度Activity
 * 提供全白色畫面供亮度計使用
 */
public class BrightnessCalibrationActivity extends Activity {
    
    private LinearLayout llInstruction;
    private LinearLayout llBottomButtons;
    private TextView tvTapHint;
    private Button btnHideInstruction;
    private Button btnCloseCalibration;
    
    private boolean isInstructionVisible = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 設置全螢幕模式
        setupFullScreen();
        
        setContentView(R.layout.activity_brightness_calibration);
        
        initializeViews();
        setupClickListeners();
        
        // 保持螢幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    private void setupFullScreen() {
        // 隱藏標題欄
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // 設置全螢幕
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        // 隱藏導航欄和狀態欄（Android 4.4+）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
    
    private void initializeViews() {
        llInstruction = findViewById(R.id.ll_instruction);
        llBottomButtons = findViewById(R.id.ll_bottom_buttons);
        tvTapHint = findViewById(R.id.tv_tap_hint);
        btnHideInstruction = findViewById(R.id.btn_hide_instruction);
        btnCloseCalibration = findViewById(R.id.btn_close_calibration);
    }
    
    private void setupClickListeners() {
        // 隱藏說明按鈕
        btnHideInstruction.setOnClickListener(v -> hideInstruction());
        
        // 關閉校正按鈕 - 唯一的退出方式
        btnCloseCalibration.setOnClickListener(v -> finish());
        
        // 移除點擊空白畫面的退出功能，只能通過關閉按鈕退出
        // 不再設置螢幕點擊監聽器
    }
    
    private void hideInstruction() {
        try {
            llInstruction.setVisibility(View.GONE);
            llBottomButtons.setVisibility(View.GONE);
            // 不顯示點擊提示，因為不再支援點擊恢復
            tvTapHint.setVisibility(View.GONE);
            isInstructionVisible = false;
            
            // 重新設置全螢幕模式
            setupFullScreen();
        } catch (Exception e) {
            // 如果出現錯誤，至少隱藏主要元素
            llInstruction.setVisibility(View.GONE);
            tvTapHint.setVisibility(View.GONE);
            isInstructionVisible = false;
        }
    }
    
    // 移除showInstruction方法，因為不再支援點擊恢復控制項
    // 用戶只能通過關閉按鈕退出校正模式
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isInstructionVisible) {
            // 重新設置全螢幕模式
            setupFullScreen();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 確保螢幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 移除螢幕常亮標誌
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
