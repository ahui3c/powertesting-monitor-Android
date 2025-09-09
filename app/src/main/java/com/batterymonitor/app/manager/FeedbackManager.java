package com.batterymonitor.app.manager;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

/**
 * 音效和震動反饋管理器
 * 負責在測試開始和結束時提供聲音和震動反饋
 */
public class FeedbackManager {
    
    private static final String TAG = "FeedbackManager";
    
    private Context context;
    private Vibrator vibrator;
    private ToneGenerator toneGenerator;
    
    // 震動模式定義
    private static final long[] START_VIBRATION_PATTERN = {0, 200, 100, 200}; // 開始：短-停-短
    private static final long[] END_VIBRATION_PATTERN = {0, 500, 200, 300, 200, 300}; // 結束：長-停-中-停-中
    
    // 音效定義
    private static final int START_TONE = ToneGenerator.TONE_PROP_BEEP;
    private static final int END_TONE = ToneGenerator.TONE_PROP_BEEP2;
    
    public FeedbackManager(Context context) {
        this.context = context;
        initializeComponents();
    }
    
    private void initializeComponents() {
        try {
            // 初始化震動器
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ 使用 VibratorManager
                VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    vibrator = vibratorManager.getDefaultVibrator();
                }
            } else {
                // Android 11 及以下使用傳統方式
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }
            
            // 初始化音效產生器
            try {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80);
            } catch (RuntimeException e) {
                Log.w(TAG, "Failed to create ToneGenerator: " + e.getMessage());
                toneGenerator = null;
            }
            
            Log.d(TAG, "FeedbackManager initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FeedbackManager", e);
        }
    }
    
    /**
     * 播放測試開始的反饋（音效 + 震動）
     */
    public void playStartFeedback() {
        try {
            Log.d(TAG, "Playing start feedback");
            
            // 播放開始音效
            playStartSound();
            
            // 觸發開始震動
            vibrateStart();
            
        } catch (Exception e) {
            Log.e(TAG, "Error playing start feedback", e);
        }
    }
    
    /**
     * 播放測試結束的反饋（音效 + 震動）
     */
    public void playEndFeedback() {
        try {
            Log.d(TAG, "Playing end feedback");
            
            // 播放結束音效
            playEndSound();
            
            // 觸發結束震動
            vibrateEnd();
            
        } catch (Exception e) {
            Log.e(TAG, "Error playing end feedback", e);
        }
    }
    
    /**
     * 播放開始音效
     */
    private void playStartSound() {
        try {
            if (toneGenerator != null) {
                // 播放簡短的提示音（開始）
                toneGenerator.startTone(START_TONE, 200);
                Log.d(TAG, "Start sound played");
            } else {
                Log.w(TAG, "ToneGenerator not available for start sound");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing start sound", e);
        }
    }
    
    /**
     * 播放結束音效
     */
    private void playEndSound() {
        try {
            if (toneGenerator != null) {
                // 播放雙音提示（結束）
                toneGenerator.startTone(END_TONE, 300);
                Log.d(TAG, "End sound played");
            } else {
                Log.w(TAG, "ToneGenerator not available for end sound");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing end sound", e);
        }
    }
    
    /**
     * 觸發開始震動
     */
    private void vibrateStart() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0+ 使用 VibrationEffect
                    VibrationEffect effect = VibrationEffect.createWaveform(START_VIBRATION_PATTERN, -1);
                    vibrator.vibrate(effect);
                } else {
                    // Android 7.1 及以下使用傳統方式
                    vibrator.vibrate(START_VIBRATION_PATTERN, -1);
                }
                Log.d(TAG, "Start vibration triggered");
            } else {
                Log.w(TAG, "Vibrator not available");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error triggering start vibration", e);
        }
    }
    
    /**
     * 觸發結束震動
     */
    private void vibrateEnd() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0+ 使用 VibrationEffect
                    VibrationEffect effect = VibrationEffect.createWaveform(END_VIBRATION_PATTERN, -1);
                    vibrator.vibrate(effect);
                } else {
                    // Android 7.1 及以下使用傳統方式
                    vibrator.vibrate(END_VIBRATION_PATTERN, -1);
                }
                Log.d(TAG, "End vibration triggered");
            } else {
                Log.w(TAG, "Vibrator not available");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error triggering end vibration", e);
        }
    }
    
    /**
     * 播放簡單的通知反饋（用於其他操作）
     */
    public void playNotificationFeedback() {
        try {
            // 簡短震動
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect effect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE);
                    vibrator.vibrate(effect);
                } else {
                    vibrator.vibrate(100);
                }
            }
            
            // 簡短提示音
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 100);
            }
            
            Log.d(TAG, "Notification feedback played");
            
        } catch (Exception e) {
            Log.e(TAG, "Error playing notification feedback", e);
        }
    }
    
    /**
     * 檢查震動功能是否可用
     */
    public boolean isVibrationAvailable() {
        return vibrator != null && vibrator.hasVibrator();
    }
    
    /**
     * 檢查音效功能是否可用
     */
    public boolean isSoundAvailable() {
        return toneGenerator != null;
    }
    
    /**
     * 釋放資源
     */
    public void release() {
        try {
            if (toneGenerator != null) {
                toneGenerator.release();
                toneGenerator = null;
                Log.d(TAG, "ToneGenerator released");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error releasing FeedbackManager", e);
        }
    }
    
    /**
     * 獲取反饋狀態資訊
     */
    public String getFeedbackStatus() {
        StringBuilder status = new StringBuilder();
        status.append("震動: ").append(isVibrationAvailable() ? "可用" : "不可用");
        status.append(", 音效: ").append(isSoundAvailable() ? "可用" : "不可用");
        return status.toString();
    }
}
