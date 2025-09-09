package com.batterymonitor.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.batterymonitor.app.adapter.TestResultAdapter;
import com.batterymonitor.app.model.TestResult;
import com.batterymonitor.app.utils.PreferenceManager;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    
    private static final String TAG = "HistoryActivity";
    
    // UI 組件
    private RecyclerView recyclerHistory;
    private LinearLayout layoutNoData;
    
    // 適配器和數據
    private TestResultAdapter adapter;
    private PreferenceManager preferenceManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        // 設置標題欄
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.history_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initializeComponents();
        initializeViews();
        setupRecyclerView();
        loadData();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    private void initializeComponents() {
        preferenceManager = new PreferenceManager(this);
    }
    
    private void initializeViews() {
        recyclerHistory = findViewById(R.id.recycler_history);
        layoutNoData = findViewById(R.id.layout_no_data);
    }
    
    private void setupRecyclerView() {
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TestResultAdapter();
        recyclerHistory.setAdapter(adapter);
    }
    
    private void loadData() {
        // 載入測試歷史
        List<TestResult> history = preferenceManager.getTestHistory();
        
        if (history.isEmpty()) {
            // 顯示無數據提示
            recyclerHistory.setVisibility(View.GONE);
            layoutNoData.setVisibility(View.VISIBLE);
        } else {
            // 顯示歷史記錄
            recyclerHistory.setVisibility(View.VISIBLE);
            layoutNoData.setVisibility(View.GONE);
            
            // 更新適配器數據
            adapter.setTestResults(history);
        }
    }
}

