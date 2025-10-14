package com.lab.taskfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lab.taskfinder.model.Task;
import com.lab.taskfinder.model.TaskAcceptance;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAcceptanceAdapter extends RecyclerView.Adapter<TaskAcceptanceAdapter.TaskViewHolder> {

    private final Context context;
    private final List<Task> tasks;
    private final List<TaskAcceptance> acceptances;
    private final TaskItemClickListener listener;

    // Interface สำหรับ click listener
    public interface TaskItemClickListener {
        void onViewDetailsClick(int position);
        void onCompleteClick(int position);
        void onCancelClick(int position);
    }

    public TaskAcceptanceAdapter(Context context, List<Task> tasks, List<TaskAcceptance> acceptances, TaskItemClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.acceptances = acceptances;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_acceptance, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        if (position < tasks.size() && position < acceptances.size()) {
            Task task = tasks.get(position);
            TaskAcceptance acceptance = acceptances.get(position);

            // ตั้งค่าข้อมูลงาน
            holder.txtTaskTitle.setText(task.getTaskTitle());

            // แสดงราคางาน
            DecimalFormat formatter = new DecimalFormat("$#,##0.00");
            holder.txtTaskPrice.setText(formatter.format(task.getTaskPrice()));

            // แสดงชื่อผู้โพสต์
            holder.txtTaskClient.setText("โพสต์โดย: " + task.getTaskClient());

            // แสดงรายละเอียดงาน
            holder.txtTaskDescription.setText(task.getTaskDescription());

            // แสดงวันที่รับงาน
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date date = inputFormat.parse(acceptance.getAccetpAt());
                holder.txtAcceptedDate.setText("รับงานเมื่อ: " + outputFormat.format(date));
            } catch (ParseException e) {
                holder.txtAcceptedDate.setText("รับงานเมื่อ: " + acceptance.getAccetpAt());
            }

            // ตั้งค่าสีแสดงสถานะงาน
            switch (acceptance.getStatus()) {
                case "in progress":
                    holder.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.orange));
                    holder.btnComplete.setVisibility(View.VISIBLE);
                    holder.btnCancel.setVisibility(View.VISIBLE);
                    break;
                case "completed":
                    holder.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                    holder.btnComplete.setVisibility(View.GONE);
                    holder.btnCancel.setVisibility(View.GONE);
                    break;
                case "cancelled":
                    holder.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                    holder.btnComplete.setVisibility(View.GONE);
                    holder.btnCancel.setVisibility(View.GONE);
                    break;
                default:
                    holder.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.grey));
                    break;
            }

            holder.btnViewDetails.setOnClickListener(v -> listener.onViewDetailsClick(position));
            holder.btnComplete.setOnClickListener(v -> listener.onCompleteClick(position));
            holder.btnCancel.setOnClickListener(v -> listener.onCancelClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(tasks.size(), acceptances.size());
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTaskTitle, txtTaskPrice, txtTaskClient, txtTaskDescription, txtAcceptedDate;
        Button btnViewDetails, btnComplete, btnCancel;
        View viewStatusIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTaskTitle = itemView.findViewById(R.id.txtTaskTitle);
            txtTaskPrice = itemView.findViewById(R.id.txtTaskPrice);
            txtTaskClient = itemView.findViewById(R.id.txtTaskClient);
            txtTaskDescription = itemView.findViewById(R.id.txtTaskDescription);
            txtAcceptedDate = itemView.findViewById(R.id.txtAcceptedDate);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);
        }
    }
}