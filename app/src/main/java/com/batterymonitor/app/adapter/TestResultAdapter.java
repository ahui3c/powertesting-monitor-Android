package com.batterymonitor.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.batterymonitor.app.R;
import com.batterymonitor.app.model.TestResult;
import com.batterymonitor.app.utils.ClipboardManager;

import java.util.ArrayList;
import java.util.List;

public class TestResultAdapter extends RecyclerView.Adapter<TestResultAdapter.ViewHolder> {
    
    private List<TestResult> testResults = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private OnCopyClickListener onCopyClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(TestResult testResult);
    }
    
    public interface OnCopyClickListener {
        void onCopyClick(TestResult testResult);
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_test_result, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestResult result = testResults.get(position);
        holder.bind(result);
        
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(result);
            }
        });
        
        holder.btnCopyResult.setOnClickListener(v -> {
            if (onCopyClickListener != null) {
                onCopyClickListener.onCopyClick(result);
            } else {
                // 默認複製行為
                ClipboardManager clipboardManager = new ClipboardManager(v.getContext());
                clipboardManager.copyTestResult(result);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return testResults.size();
    }
    
    public void setTestResults(List<TestResult> testResults) {
        this.testResults = testResults != null ? testResults : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setOnCopyClickListener(OnCopyClickListener listener) {
        this.onCopyClickListener = listener;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTestDate;
        private TextView tvTestSubject;
        private TextView tvBatteryConsumed;
        private TextView tvDuration;
        private TextView tvBatteryRange;
        private TextView tvConsumptionRate;
        private TextView tvTimeRange;
        private Button btnCopyResult;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvTestDate = itemView.findViewById(R.id.tv_test_date);
            tvTestSubject = itemView.findViewById(R.id.tv_test_subject);
            tvBatteryConsumed = itemView.findViewById(R.id.tv_battery_consumed);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvBatteryRange = itemView.findViewById(R.id.tv_battery_range);
            tvConsumptionRate = itemView.findViewById(R.id.tv_consumption_rate);
            tvTimeRange = itemView.findViewById(R.id.tv_time_range);
            btnCopyResult = itemView.findViewById(R.id.btn_copy_result);
        }
        
        public void bind(TestResult result) {
            // 設置測試日期
            tvTestDate.setText(result.getFormattedStartTime().substring(0, 10)); // 只顯示日期部分
            
            // 設置測試主題
            String testSubject = result.getTestSubject();
            if (testSubject != null && !testSubject.trim().isEmpty()) {
                tvTestSubject.setText(testSubject);
                tvTestSubject.setVisibility(View.VISIBLE);
            } else {
                tvTestSubject.setVisibility(View.GONE);
            }
            
            // 設置電量消耗
            tvBatteryConsumed.setText("-" + result.getBatteryConsumed() + "%");
            
            // 設置持續時間
            tvDuration.setText(result.getFormattedDuration());
            
            // 設置電量變化範圍
            tvBatteryRange.setText(String.format("%d%% → %d%%", 
                result.getStartBatteryLevel(), result.getEndBatteryLevel()));
            
            // 設置消耗率
            tvConsumptionRate.setText(result.getFormattedConsumptionRate());
            
            // 設置時間範圍
            tvTimeRange.setText(String.format("%s - %s", 
                result.getFormattedStartTimeShort(), result.getFormattedEndTimeShort()));
        }
    }
}

