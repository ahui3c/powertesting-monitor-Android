package com.batterymonitor.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.batterymonitor.app.R;
import com.batterymonitor.app.utils.PreferenceManager;

public class TestSubjectDialog {
    
    public interface OnTestSubjectSelectedListener {
        void onTestSubjectSelected(String testSubject);
        void onCancelled();
    }
    
    private Context context;
    private WindowManager windowManager;
    private View dialogView;
    private EditText etTestSubject;
    private Button btnRecording, btnVideo, btnGaming;
    private Button btnCancel, btnStartTest;
    private OnTestSubjectSelectedListener listener;
    private PreferenceManager preferenceManager;
    
    public TestSubjectDialog(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.preferenceManager = new PreferenceManager(context);
        initializeDialog();
    }
    
    private void initializeDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogView = inflater.inflate(R.layout.dialog_test_subject_selection, null);
        
        // 初始化UI組件
        etTestSubject = dialogView.findViewById(R.id.et_test_subject);
        btnRecording = dialogView.findViewById(R.id.btn_recording);
        btnVideo = dialogView.findViewById(R.id.btn_video);
        btnGaming = dialogView.findViewById(R.id.btn_gaming);
        btnCancel = dialogView.findViewById(R.id.btn_cancel);
        btnStartTest = dialogView.findViewById(R.id.btn_start_test);
        
        // 載入上次的測試主題
        String lastTestSubject = preferenceManager.getTestSubject();
        if (!lastTestSubject.isEmpty()) {
            etTestSubject.setText(lastTestSubject);
        }
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        // 快速選擇按鈕
        btnRecording.setOnClickListener(v -> {
            etTestSubject.setText(context.getString(R.string.recording));
        });
        
        btnVideo.setOnClickListener(v -> {
            etTestSubject.setText(context.getString(R.string.video));
        });
        
        btnGaming.setOnClickListener(v -> {
            etTestSubject.setText(context.getString(R.string.gaming));
        });
        
        // 取消按鈕
        btnCancel.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onCancelled();
            }
        });
        
        // 開始測試按鈕
        btnStartTest.setOnClickListener(v -> {
            String testSubject = etTestSubject.getText().toString().trim();
            
            // 保存測試主題
            preferenceManager.setTestSubject(testSubject);
            
            dismiss();
            if (listener != null) {
                listener.onTestSubjectSelected(testSubject);
            }
        });
    }
    
    public void show(OnTestSubjectSelectedListener listener) {
        this.listener = listener;
        
        // 設定視窗參數
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                      WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                      WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        
        params.format = PixelFormat.TRANSLUCENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        
        // 添加到視窗管理器
        try {
            windowManager.addView(dialogView, params);
            
            // 設定焦點到輸入框
            etTestSubject.requestFocus();
            
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onCancelled();
            }
        }
    }
    
    public void dismiss() {
        try {
            if (dialogView != null && windowManager != null) {
                windowManager.removeView(dialogView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean isShowing() {
        return dialogView != null && dialogView.getParent() != null;
    }
}
