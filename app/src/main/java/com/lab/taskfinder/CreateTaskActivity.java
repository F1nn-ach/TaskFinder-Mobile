package com.lab.taskfinder;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lab.taskfinder.model.Task;
import com.lab.taskfinder.model.UserModel;

import java.util.Calendar;

public class CreateTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_task);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // รับข้อมูลผู้ใช้จาก Intent
        UserModel userModel = (UserModel) getIntent().getSerializableExtra("user");

        // ตัวอย่างการใช้ Create Task ฟังก์ชัน
        findViewById(R.id.btnCreateTask).setOnClickListener(v -> createTask(userModel));
    }

    public void createTask(UserModel userModel) {
        // สร้าง Firebase database reference
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://taskfinder-97e63-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("tasks");

        Task task = new Task();

        // หา EditText
        EditText taskTitleEditText = findViewById(R.id.edtTaskTitle);
        EditText taskDescriptionEditText = findViewById(R.id.edtTaskDescription);
        EditText taskPriceEditText = findViewById(R.id.edtTaskPrice);
        EditText taskAddressEditText = findViewById(R.id.edtTaskAddress);

        // ดึงข้อมูลจาก EditText
        String taskTitle = taskTitleEditText.getText().toString();
        String taskDescription = taskDescriptionEditText.getText().toString();
        float taskPrice = Float.parseFloat(taskPriceEditText.getText().toString());
        String taskAddress = taskAddressEditText.getText().toString();

        Calendar calendar = Calendar.getInstance();
        String currentDate = String.format("%02d-%02d-%d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));

        task.setTaskTitle(taskTitle);
        task.setTaskDescription(taskDescription);
        task.setTaskPrice(taskPrice);
        task.setTaskAddress(taskAddress);
        task.setTaskStatus("open");
        task.setTaskClient(userModel.getUser().getUsername());
        task.setTaskCreatedAt(currentDate);

        String taskId = databaseRef.push().getKey();
        task.setTaskId(taskId);

        // บันทึกลง Firebase
        if (taskId != null) {
            databaseRef.child(taskId).setValue(task)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateTaskActivity.this, "Task created successfully", Toast.LENGTH_SHORT).show();
                        finish(); // กลับไปหน้าหลักหลังจากบันทึกเสร็จ
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateTaskActivity.this, "Failed to create task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
