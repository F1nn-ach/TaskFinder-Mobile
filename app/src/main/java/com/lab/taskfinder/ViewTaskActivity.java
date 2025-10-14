package com.lab.taskfinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lab.taskfinder.model.Task;
import com.lab.taskfinder.model.TaskAcceptance;
import com.lab.taskfinder.model.UserModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewTaskActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "ViewTaskActivity";
    private static final String FIREBASE_URL = "https://taskfinder-97e63-default-rtdb.asia-southeast1.firebasedatabase.app/";

    // UI Components
    private TextView txtTaskTitle;
    private TextView txtTaskDescription;
    private TextView txtTaskPrice;
    private TextView txtTaskDate;
    private TextView txtTaskLocation;
    private TextView txtTaskStatus;
    private TextView txtTaskClient;
    private TextView txtAcceptedBy;
    private TextView txtAcceptedTime;
    private View viewStatusIndicator;
    private Button btnAcceptTask;
    private Button btnBackToList;
    private Toolbar toolbar;

    // Data
    private Task currentTask;
    private UserModel currentUser;
    private DatabaseReference databaseRef;
    private TaskAcceptance taskAcceptance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_task);

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance(FIREBASE_URL).getReference("tasks");

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the UI components
        initializeViews();

        // Get the task and user data from the intent
        loadDataFromIntent();

        // Set up the toolbar
        setupToolbar();

        // Display the task details
        displayTaskDetails();

        // Load task acceptance information
        loadTaskAcceptanceInfo();

        // Set up button click listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        // Find all the views
        txtTaskTitle = findViewById(R.id.txtTaskTitle);
        txtTaskDescription = findViewById(R.id.txtTaskDescription);
        txtTaskPrice = findViewById(R.id.txtTaskPrice);
        txtTaskDate = findViewById(R.id.txtTaskDate);
        txtTaskLocation = findViewById(R.id.txtTaskLocation);
        txtTaskStatus = findViewById(R.id.txtTaskStatus);
        txtTaskClient = findViewById(R.id.txtTaskClient);
        txtAcceptedBy = findViewById(R.id.txtAcceptedBy);
        txtAcceptedTime = findViewById(R.id.txtAcceptedTime);
        viewStatusIndicator = findViewById(R.id.viewStatusIndicator);
        btnAcceptTask = findViewById(R.id.btnAcceptTask);
        btnBackToList = findViewById(R.id.btnBackToList);
        toolbar = findViewById(R.id.toolbar);
    }

    private void loadDataFromIntent() {
        // Get the task data
        currentTask = (Task) getIntent().getSerializableExtra("TASK");

        // Get the user data
        currentUser = (UserModel) getIntent().getSerializableExtra("user");

        // Validate data
        if (currentTask == null) {
            Log.e(TAG, "No task data provided");
            Toast.makeText(this, "Error: Task data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentUser == null) {
            Log.e(TAG, "No user data provided");
            Toast.makeText(this, "Error: User data not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Task Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void displayTaskDetails() {
        // Set task details
        txtTaskTitle.setText(currentTask.getTaskTitle());
        txtTaskDescription.setText(currentTask.getTaskDescription());
        txtTaskPrice.setText(String.format("$%.2f", (double)currentTask.getTaskPrice()));

        // Set location if available
        if (currentTask.getTaskAddress() != null && !currentTask.getTaskAddress().isEmpty()) {
            txtTaskLocation.setText(currentTask.getTaskAddress());
        } else {
            txtTaskLocation.setText("Location not specified");
        }

        // Set client information
        txtTaskClient.setText(currentTask.getTaskClient());

        // Format date correctly
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            if (currentTask.getTaskCreatedAt() != null) {
                // Try parsing as timestamp in milliseconds
                try {
                    long timestamp = Long.parseLong(currentTask.getTaskCreatedAt());
                    Date date = new Date(timestamp);
                    txtTaskDate.setText(outputFormat.format(date));
                } catch (NumberFormatException e) {
                    // If not a number, try parsing as formatted date string
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date date = inputFormat.parse(currentTask.getTaskCreatedAt());
                        txtTaskDate.setText(outputFormat.format(date));
                    } catch (ParseException pe) {
                        Log.e(TAG, "Error parsing date string", pe);
                        txtTaskDate.setText(currentTask.getTaskCreatedAt()); // Just show the raw string
                    }
                }
            } else {
                txtTaskDate.setText("No date");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing date", e);
            txtTaskDate.setText("Invalid date");
        }

        // Set task status and color
        if (currentTask.getTaskStatus() != null) {
            txtTaskStatus.setText(currentTask.getTaskStatus());

            switch (currentTask.getTaskStatus().toLowerCase()) {
                case "open":
                    viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                    // Only show accept button for open tasks
                    btnAcceptTask.setVisibility(View.VISIBLE);
                    break;
                case "in progress":
                    viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                    btnAcceptTask.setVisibility(View.GONE);
                    break;
                case "completed":
                    viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
                    btnAcceptTask.setVisibility(View.GONE);
                    break;
                default:
                    viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
                    btnAcceptTask.setVisibility(View.GONE);
                    break;
            }
        } else {
            txtTaskStatus.setText("Unknown");
            viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
        }

        // Hide the accept button if this is the user's own task
        if (currentUser != null && currentUser.getUser() != null &&
                currentUser.getUser().getUsername().equals(currentTask.getTaskClient())) {
            btnAcceptTask.setVisibility(View.GONE);
        }
    }

    private void loadTaskAcceptanceInfo() {
        // อ่านข้อมูลการรับงานจาก Firebase
        FirebaseDatabase.getInstance(FIREBASE_URL)
                .getReference("task_acceptances")
                .orderByChild("taskId")
                .equalTo(currentTask.getTaskId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot acceptanceSnapshot : snapshot.getChildren()) {
                                TaskAcceptance acceptance = acceptanceSnapshot.getValue(TaskAcceptance.class);
                                if (acceptance != null && "accepted".equals(acceptance.getStatus())) {
                                    taskAcceptance = acceptance;

                                    // แสดงข้อมูลผู้รับงาน
                                    txtAcceptedBy.setText("รับงานโดย: " + acceptance.getAcceptance());
                                    txtAcceptedBy.setVisibility(View.VISIBLE);

                                    // แสดงเวลาที่รับงาน
                                    txtAcceptedTime.setText("เวลารับงาน: " + acceptance.getAccetpAt());
                                    txtAcceptedTime.setVisibility(View.VISIBLE);

                                    break; // ใช้ข้อมูลล่าสุดเท่านั้น
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading task acceptance info: " + error.getMessage());
                    }
                });
    }

    private void setupButtonListeners() {
        // Back button click listener
        btnBackToList.setOnClickListener(v -> finish());

        // Accept Task button click listener
        btnAcceptTask.setOnClickListener(v -> showAcceptTaskConfirmation());
    }

    private void showAcceptTaskConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("รับงาน");
        builder.setMessage("คุณต้องการรับงานนี้ใช่หรือไม่?");

        // Add the buttons
        builder.setPositiveButton("รับงาน", (dialog, id) -> acceptTask());
        builder.setNegativeButton("ยกเลิก", (dialog, id) -> dialog.dismiss());

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // เฉพาะส่วนเมธอด acceptTask() ที่ต้องแก้ไข

    private void acceptTask() {
        // แสดงไดอะล็อกแสดงความคืบหน้า
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("กำลังดำเนินการ");
        builder.setMessage("กรุณารอสักครู่...");
        builder.setCancelable(false);
        AlertDialog progressDialog = builder.create();
        progressDialog.show();

        // ตรวจสอบว่างานยังคงมีอยู่และยังเปิดรับอยู่
        DatabaseReference taskRef = databaseRef.child(currentTask.getTaskId());
        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();

                // ตรวจสอบว่างานยังคงมีอยู่
                if (!snapshot.exists()) {
                    showErrorDialog("งานนี้ไม่มีอยู่ในระบบแล้ว");
                    return;
                }

                // ดึงข้อมูลงานล่าสุด
                Task latestTask = snapshot.getValue(Task.class);

                // ตรวจสอบว่างานยังคงเปิดรับอยู่
                if (latestTask == null || !"open".equalsIgnoreCase(latestTask.getTaskStatus())) {
                    showErrorDialog("งานนี้ไม่สามารถรับได้แล้ว");
                    return;
                }

                // อัปเดตสถานะงาน
                taskRef.child("taskStatus").setValue("in progress");

                // สร้างรหัสสำหรับ TaskAcceptance
                String acceptanceId = FirebaseDatabase.getInstance(FIREBASE_URL)
                        .getReference("task_acceptances").push().getKey();

                // ข้อมูลผู้ใช้ปัจจุบัน
                String currentUsername = currentUser.getUser().getUsername();

                // วันเวลาปัจจุบัน
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentDateTime = dateFormat.format(new Date());

                // !! เปลี่ยนการบันทึกข้อมูลเป็นการใช้ Map แทนการใช้ TaskAcceptance โดยตรง
                // เพื่อป้องกันปัญหาการแปลงข้อมูล
                Map<String, Object> acceptanceData = new HashMap<>();
                acceptanceData.put("taskAcceptanceId", acceptanceId);
                acceptanceData.put("taskId", currentTask.getTaskId());
                acceptanceData.put("acceptance", currentUsername);
                acceptanceData.put("accetpAt", currentDateTime);
                acceptanceData.put("status", "in progress");

                // บันทึกข้อมูลการรับงานลงใน Firebase
                FirebaseDatabase.getInstance(FIREBASE_URL)
                        .getReference("task_acceptances")
                        .child(acceptanceId)
                        .setValue(acceptanceData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // แสดงข้อความแจ้งเตือนว่าสำเร็จ
                                Toast.makeText(ViewTaskActivity.this,
                                        "รับงานสำเร็จ!", Toast.LENGTH_SHORT).show();

                                // อัปเดตข้อมูลในออบเจ็กต์ปัจจุบัน
                                currentTask.setTaskStatus("in progress");

                                // อัปเดต UI
                                displayTaskDetails();

                                // แสดงข้อมูลผู้รับงาน
                                txtAcceptedBy.setText("รับงานโดย: " + currentUsername);
                                txtAcceptedBy.setVisibility(View.VISIBLE);

                                // แสดงเวลาที่รับงาน
                                txtAcceptedTime.setText("เวลารับงาน: " + currentDateTime);
                                txtAcceptedTime.setVisibility(View.VISIBLE);
                            } else {
                                // กรณีบันทึกไม่สำเร็จ ให้เปลี่ยนสถานะงานกลับเป็น open
                                taskRef.child("taskStatus").setValue("open");

                                // แสดงข้อความแจ้งเตือนว่าล้มเหลว
                                showErrorDialog("เกิดข้อผิดพลาดในการบันทึกข้อมูล กรุณาลองใหม่อีกครั้ง");
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Log.e(TAG, "Database error: " + error.getMessage());
                showErrorDialog("เกิดข้อผิดพลาดในการเข้าถึงฐานข้อมูล กรุณาลองใหม่อีกครั้ง");
            }
        });
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ข้อผิดพลาด");
        builder.setMessage(message);
        builder.setPositiveButton("ตกลง", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void finishWithResult() {
        // You could use this to return the updated task to the calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("UPDATED_TASK", currentTask);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}