package com.batterymonitor.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.batterymonitor.app.R;

/**
 * 關於資訊對話框
 * 顯示應用程式版本、作者資訊和聯絡方式
 */
public class AboutDialog extends Dialog {
    
    private static final String TAG = "AboutDialog";
    
    private Context context;
    
    // UI組件
    private TextView tvVersionName;
    private TextView tvVersionCode;
    private TextView tvEmail;
    private TextView tvWebsite;
    private Button btnContactEmail;
    private Button btnVisitWebsite;
    private Button btnCloseAbout;
    
    // 聯絡資訊
    private static final String AUTHOR_NAME = "廖阿輝";
    private static final String AUTHOR_EMAIL = "chehui@gmail.com";
    private static final String AUTHOR_WEBSITE = "https://ahui3c.com";
    
    public AboutDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 移除標題欄
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.dialog_about);
        
        initializeViews();
        loadVersionInfo();
        setupClickListeners();
        
        Log.d(TAG, "AboutDialog created");
    }
    
    private void initializeViews() {
        tvVersionName = findViewById(R.id.tv_version_name);
        tvVersionCode = findViewById(R.id.tv_version_code);
        tvEmail = findViewById(R.id.tv_email);
        tvWebsite = findViewById(R.id.tv_website);
        btnContactEmail = findViewById(R.id.btn_contact_email);
        btnVisitWebsite = findViewById(R.id.btn_visit_website);
        btnCloseAbout = findViewById(R.id.btn_close_dialog);
    }
    
    private void loadVersionInfo() {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            
            // 設定版本資訊
            tvVersionName.setText("版本：" + packageInfo.versionName);
            tvVersionCode.setText("版本代碼：" + packageInfo.versionCode);
            
            Log.d(TAG, "Version info loaded: " + packageInfo.versionName + " (" + packageInfo.versionCode + ")");
            
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error loading version info", e);
            tvVersionName.setText("版本：未知");
            tvVersionCode.setText("版本代碼：未知");
        }
    }
    
    private void setupClickListeners() {
        // 聯絡作者按鈕
        btnContactEmail.setOnClickListener(v -> openEmailClient());
        
        // 訪問網站按鈕
        btnVisitWebsite.setOnClickListener(v -> openWebsite());
        
        // 關閉按鈕
        btnCloseAbout.setOnClickListener(v -> dismiss());
        
        // 信箱文字點擊
        tvEmail.setOnClickListener(v -> openEmailClient());
        
        // 網站文字點擊
        tvWebsite.setOnClickListener(v -> openWebsite());
    }
    
    /**
     * 開啟郵件客戶端
     */
    private void openEmailClient() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + AUTHOR_EMAIL));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "關於電力監控應用程式");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "您好 " + AUTHOR_NAME + "，\\n\\n我想詢問關於電力監控應用程式的問題：\\n\\n");
            
            // 檢查是否有郵件應用程式可以處理此Intent
            if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(emailIntent, "選擇郵件應用程式"));
                Log.d(TAG, "Email client opened");
            } else {
                // 如果沒有郵件應用程式，嘗試複製郵件地址到剪貼簿
                copyEmailToClipboard();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error opening email client", e);
            copyEmailToClipboard();
        }
    }
    
    /**
     * 複製郵件地址到剪貼簿
     */
    private void copyEmailToClipboard() {
        try {
            android.content.ClipboardManager clipboard = 
                (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("作者信箱", AUTHOR_EMAIL);
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(context, "作者信箱已複製到剪貼簿：" + AUTHOR_EMAIL, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Email copied to clipboard");
            
        } catch (Exception e) {
            Log.e(TAG, "Error copying email to clipboard", e);
            Toast.makeText(context, "無法開啟郵件應用程式，請手動聯絡：" + AUTHOR_EMAIL, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 開啟網站
     */
    private void openWebsite() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AUTHOR_WEBSITE));
            
            // 檢查是否有瀏覽器可以處理此Intent
            if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(browserIntent);
                Log.d(TAG, "Website opened: " + AUTHOR_WEBSITE);
            } else {
                // 如果沒有瀏覽器，複製網址到剪貼簿
                copyWebsiteToClipboard();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error opening website", e);
            copyWebsiteToClipboard();
        }
    }
    
    /**
     * 複製網站地址到剪貼簿
     */
    private void copyWebsiteToClipboard() {
        try {
            android.content.ClipboardManager clipboard = 
                (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("作者網站", AUTHOR_WEBSITE);
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(context, "網站地址已複製到剪貼簿：" + AUTHOR_WEBSITE, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Website URL copied to clipboard");
            
        } catch (Exception e) {
            Log.e(TAG, "Error copying website to clipboard", e);
            Toast.makeText(context, "無法開啟瀏覽器，請手動訪問：" + AUTHOR_WEBSITE, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 獲取作者資訊
     */
    public static String getAuthorName() {
        return AUTHOR_NAME;
    }
    
    public static String getAuthorEmail() {
        return AUTHOR_EMAIL;
    }
    
    public static String getAuthorWebsite() {
        return AUTHOR_WEBSITE;
    }
    
    /**
     * 顯示對話框
     */
    public void showAbout() {
        try {
            show();
            
            // 設定對話框寬度為螢幕寬度的85%
            Window window = getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85);
                window.setAttributes(layoutParams);
            }
            
            Log.d(TAG, "About dialog shown with custom width");
        } catch (Exception e) {
            Log.e(TAG, "Error showing about dialog", e);
        }
    }
}
