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
        
        // 關閉校正按鈕
        btnCloseCalibration.setOnClickListener(v -> finish());
        
        // 點擊螢幕顯示控制項
        findViewById(android.R.id.content).setOnClickListener(v -> {
            if (!isInstructionVisible) {
                showInstruction();
            }
        });
        
        // 點擊提示文字顯示控制項
        tvTapHint.setOnClickListener(v -> showInstruction());
    }
    
    private void hideInstruction() {
        try {
            llInstruction.setVisibility(View.GONE);
            llBottomButtons.setVisibility(View.GONE);
            tvTapHint.setVisibility(View.VISIBLE);
            isInstructionVisible = false;
            
            // 重新設置全螢幕模式
            setupFullScreen();
        } catch (Exception e) {
            // 如果出現錯誤，至少隱藏主要元素
            llInstruction.setVisibility(View.GONE);
            tvTapHint.setVisibility(View.VISIBLE);
            isInstructionVisible = false;
        }
    }
    
    private void showInstruction() {
        try {
            llInstruction.setVisibility(View.VISIBLE);
            llBottomButtons.setVisibility(View.VISIBLE);
            tvTapHint.setVisibility(View.GONE);
            isInstructionVisible = true;
        } catch (Exception e) {
            // 如果出現錯誤，至少顯示主要元素
            llInstruction.setVisibility(View.VISIBLE);
            tvTapHint.setVisibility(View.GONE);
            isInstructionVisible = true;
        }
    }
    
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
