package com.lab.taskfinder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lab.taskfinder.R;
import com.lab.taskfinder.TaskAcceptanceAdapter;
import com.lab.taskfinder.model.Task;
import com.lab.taskfinder.model.TaskAcceptance;
import com.lab.taskfinder.model.UserModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskListFragment extends Fragment implements TaskAcceptanceAdapter.TaskItemClickListener {

    private static final String TAG = "TaskListFragment";
    private static final String ARG_STATUS = "status";
    private static final String FIREBASE_URL = "https://taskfinder-97e63-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private RecyclerView recyclerViewTasks;
    private LinearLayout emptyView;
    private TextView txtEmptyMessage;
    private TaskAcceptanceAdapter adapter;
    private String status;
    private UserModel currentUser;
    private List<TaskAcceptance> taskAcceptances = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private Map<String, Task> taskMap = new HashMap<>(); // เพิ่มแม็พเพื่อจับคู่ Task กับ TaskAcceptance

    public static TaskListFragment newInstance(String status) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString(ARG_STATUS);
        }

        // ดึงข้อมูลผู้ใช้ปัจจุบัน
        currentUser = getUserFromSession();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ค้นหา View
        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);
        emptyView = view.findViewById(R.id.emptyView);
        txtEmptyMessage = view.findViewById(R.id.txtEmptyMessage);

        // ตั้งค่าข้อความเมื่อไม่มีข้อมูล
        setEmptyMessage();

        // ตั้งค่า RecyclerView
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAcceptanceAdapter(getContext(), tasks, taskAcceptances, this);
        recyclerViewTasks.setAdapter(adapter);

        // โหลดข้อมูลงาน
        loadAcceptedTasks();
    }

    private void setEmptyMessage() {
        String message;
        switch (status) {
            case "in progress":
                message = "ไม่มีงานที่กำลังดำเนินการ";
                break;
            case "completed":
                message = "ไม่มีงานที่เสร็จสิ้นแล้ว";
                break;
            case "cancelled":
                message = "ไม่มีงานที่ยกเลิก";
                break;
            default:
                message = "ไม่มีงานในสถานะนี้";
        }

        if (txtEmptyMessage != null) {
            txtEmptyMessage.setText(message);
        }
    }

    private UserModel getUserFromSession() {
        // โค้ดดึงข้อมูลผู้ใช้จาก Session หรือ SharedPreferences
        // ตัวอย่างเช่น
        if (getActivity() != null && getActivity().getIntent() != null) {
            return (UserModel) getActivity().getIntent().getSerializableExtra("user");
        }
        return null;
    }

    private void loadAcceptedTasks() {
        if (currentUser == null || currentUser.getUser() == null) {
            showEmptyView(true);
            return;
        }

        String username = currentUser.getUser().getUsername();

        DatabaseReference taskAcceptancesRef = FirebaseDatabase.getInstance(FIREBASE_URL)
                .getReference("task_acceptances");

        Query query = taskAcceptancesRef.orderByChild("acceptance").equalTo(username);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskAcceptances.clear();
                tasks.clear();

                // สร้างตัวแปรเพื่อนับจำนวนงานที่พบ
                final int[] taskCount = {0};
                final int[] loadedTaskCount = {0};

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    showEmptyView(true);
                    return;
                }

                // นับจำนวนงานทั้งหมดที่ตรงกับเงื่อนไขสถานะ
                for (DataSnapshot acceptanceSnapshot : snapshot.getChildren()) {
                    try {
                        // ตรวจสอบรูปแบบข้อมูลก่อนแปลง
                        if (acceptanceSnapshot.getValue() instanceof String) {
                            Log.e(TAG, "พบข้อมูลผิดรูปแบบ (String): " + acceptanceSnapshot.getValue());
                            continue; // ข้ามข้อมูลที่ไม่ถูกต้อง
                        }

                        // พยายามแปลงข้อมูลเป็น TaskAcceptance
                        TaskAcceptance acceptance = acceptanceSnapshot.getValue(TaskAcceptance.class);

                        if (acceptance != null && acceptance.getStatus() != null) {
                            // เพิ่ม taskAcceptanceId ถ้าไม่มี
                            if (acceptance.getTaskAcceptanceId() == null) {
                                acceptance.setTaskAcceptanceId(acceptanceSnapshot.getKey());
                            }

                            // กรองตามสถานะที่เลือก
                            if (status.equals(acceptance.getStatus())) {
                                taskCount[0]++;
                                // เก็บ TaskAcceptance ที่ผ่านการกรอง
                                taskAcceptances.add(acceptance);
                                // ดึงข้อมูลงานตาม taskId
                                loadTaskDetails(acceptance.getTaskId(), taskCount, loadedTaskCount);
                            }
                        }
                    } catch (DatabaseException e) {
                        Log.e(TAG, "ข้อผิดพลาดในการแปลงข้อมูล: " + e.getMessage());
                        // บันทึกข้อมูลที่มีปัญหาเพื่อการตรวจสอบ
                        Log.e(TAG, "ข้อมูลที่มีปัญหา: " + acceptanceSnapshot.getValue());
                    }
                }

                // ถ้าไม่มีงานที่ตรงตามเงื่อนไข
                if (taskCount[0] == 0) {
                    showEmptyView(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading task acceptances: " + error.getMessage());
                showEmptyView(true);
            }
        });
    }

    private void loadTaskDetails(String taskId, final int[] taskCount, final int[] loadedTaskCount) {
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "taskId เป็นค่าว่างหรือ null");
            return;
        }

        DatabaseReference tasksRef = FirebaseDatabase.getInstance(FIREBASE_URL)
                .getReference("tasks")
                .child(taskId);

        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadedTaskCount[0]++;

                try {
                    Task task = snapshot.getValue(Task.class);
                    if (task != null) {
                        // กำหนด taskId ถ้าไม่มี
                        if (task.getTaskId() == null) {
                            task.setTaskId(snapshot.getKey());
                        }

                        // เก็บ Task ลงในลิสต์
                        tasks.add(task);
                        // เก็บใน Map ด้วยเพื่อการอ้างอิง
                        taskMap.put(task.getTaskId(), task);

                        // อัปเดตรายการเมื่อข้อมูลพร้อม
                        adapter.notifyDataSetChanged();

                        // ถ้าโหลดข้อมูลครบแล้ว แสดงข้อมูลหรือแสดงว่าไม่มีข้อมูล
                        if (loadedTaskCount[0] >= taskCount[0]) {
                            showEmptyView(tasks.isEmpty());
                        }
                    } else {
                        Log.e(TAG, "ไม่พบข้อมูลงานสำหรับ taskId: " + taskId);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "เกิดข้อผิดพลาดในการโหลดข้อมูลงาน: " + e.getMessage());
                }

                // ตรวจสอบถ้าโหลดข้อมูลครบทั้งหมดแล้ว
                if (loadedTaskCount[0] >= taskCount[0]) {
                    showEmptyView(tasks.isEmpty());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadedTaskCount[0]++;
                Log.e(TAG, "Error loading task details: " + error.getMessage());

                // ตรวจสอบถ้าโหลดข้อมูลครบทั้งหมดแล้ว
                if (loadedTaskCount[0] >= taskCount[0]) {
                    showEmptyView(tasks.isEmpty());
                }
            }
        });
    }

    private void showEmptyView(boolean isEmpty) {
        if (getActivity() == null || getView() == null) return;

        if (isEmpty) {
            recyclerViewTasks.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewTasks.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    // ส่วนที่เหลือของคลาสยังคงเหมือนเดิม (onViewDetailsClick, onCompleteClick, onCancelClick, ฯลฯ)
    @Override
    public void onViewDetailsClick(int position) {
        // ตรวจสอบข้อมูลก่อนใช้งาน
        if (position < 0 || position >= tasks.size() || position >= taskAcceptances.size()) {
            Log.e(TAG, "ตำแหน่งข้อมูลไม่ถูกต้อง: " + position);
            return;
        }

        Task task = tasks.get(position);
        if (task != null) {
            // เปิดหน้าจอรายละเอียดงาน
            // คำสั่งเปิดหน้าจอที่มีอยู่เดิม...
        }
    }

    @Override
    public void onCompleteClick(int position) {
        // ตรวจสอบข้อมูลก่อนใช้งาน
        if (position < 0 || position >= tasks.size() || position >= taskAcceptances.size()) {
            Log.e(TAG, "ตำแหน่งข้อมูลไม่ถูกต้อง: " + position);
            return;
        }

        Task task = tasks.get(position);
        TaskAcceptance acceptance = taskAcceptances.get(position);

        if (task != null && acceptance != null) {
            showCompleteTaskDialog(task, acceptance);
        }
    }

    @Override
    public void onCancelClick(int position) {
        // ตรวจสอบข้อมูลก่อนใช้งาน
        if (position < 0 || position >= tasks.size() || position >= taskAcceptances.size()) {
            Log.e(TAG, "ตำแหน่งข้อมูลไม่ถูกต้อง: " + position);
            return;
        }

        Task task = tasks.get(position);
        TaskAcceptance acceptance = taskAcceptances.get(position);

        if (task != null && acceptance != null) {
            showCancelTaskDialog(task, acceptance);
        }
    }

    private void showCompleteTaskDialog(Task task, TaskAcceptance acceptance) {
        // ตรวจสอบการทำงาน
        if (task == null || acceptance == null) {
            Log.e(TAG, "Task หรือ TaskAcceptance เป็น null");
            return;
        }

        // สร้าง dialog จาก layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_submit_task, null);

        TextView txtTaskName = dialogView.findViewById(R.id.txtTaskName);
        EditText edtCompletionNote = dialogView.findViewById(R.id.edtCompletionNote);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        // กำหนดข้อความชื่องาน
        txtTaskName.setText("ชื่องาน: " + task.getTaskTitle());

        // สร้าง AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // กำหนดการทำงานของปุ่มยกเลิก
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // กำหนดการทำงานของปุ่มส่งงาน
        btnSubmit.setOnClickListener(v -> {
            // ดึงข้อความหมายเหตุ
            String completionNote = edtCompletionNote.getText().toString().trim();

            // แสดง progress dialog
            AlertDialog progressDialog = new AlertDialog.Builder(getContext())
                    .setTitle("กำลังส่งงาน")
                    .setMessage("กรุณารอสักครู่...")
                    .setCancelable(false)
                    .create();
            progressDialog.show();

            // ดำเนินการส่งงาน
            completeTask(task, acceptance, completionNote, progressDialog);

            // ปิด dialog ส่งงาน
            dialog.dismiss();
        });

        // แสดง dialog
        dialog.show();
    }

    private void completeTask(Task task, TaskAcceptance acceptance, String completionNote, AlertDialog progressDialog) {
        // อัปเดตสถานะงานเป็น "completed"
        DatabaseReference taskRef = FirebaseDatabase.getInstance(FIREBASE_URL)
                .getReference("tasks")
                .child(task.getTaskId());

        taskRef.child("taskStatus").setValue("completed");

        // อัปเดตข้อมูลการรับงาน
        DatabaseReference acceptanceRef = FirebaseDatabase.getInstance(FIREBASE_URL)
                .getReference("task_acceptances")
                .child(acceptance.getTaskAcceptanceId());

        // กำหนดเวลาส่งงาน
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String completionDate = dateFormat.format(new Date());

        // สร้าง Map เพื่อป้องกันปัญหาการแปลงข้อมูล
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        updates.put("completionNote", completionNote);
        updates.put("completedAt", completionDate);

        // บันทึกข้อมูลลง Firebase
        acceptanceRef.updateChildren(updates)
                .addOnCompleteListener(task1 -> {
                    // ปิด progress dialog
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    if (task1.isSuccessful()) {
                        Toast.makeText(getContext(), "ส่งงานสำเร็จ!", Toast.LENGTH_SHORT).show();
                        // โหลดข้อมูลใหม่
                        loadAcceptedTasks();
                    } else {
                        // กรณีเกิดข้อผิดพลาด ให้เปลี่ยนสถานะงานกลับเป็น in progress
                        taskRef.child("taskStatus").setValue("in progress");

                        Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการส่งงาน", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error completing task: " + task1.getException().getMessage());
                    }
                });
    }

    private void showCancelTaskDialog(Task task, TaskAcceptance acceptance) {
        // ตรวจสอบการทำงาน
        if (task == null || acceptance == null) {
            Log.e(TAG, "Task หรือ TaskAcceptance เป็น null");
            return;
        }

        // สร้าง dialog ยืนยันการยกเลิกงาน
        new AlertDialog.Builder(getContext())
                .setTitle("ยกเลิกงาน")
                .setMessage("คุณต้องการยกเลิกงาน \"" + task.getTaskTitle() + "\" ใช่หรือไม่?\n\nหากยกเลิก งานจะกลับไปเป็นสถานะเปิดรับและผู้ใช้คนอื่นสามารถรับงานนี้ได้")
                .setPositiveButton("ยกเลิกงาน", (dialog, which) -> {
                    // แสดง progress dialog
                    AlertDialog progressDialog = new AlertDialog.Builder(getContext())
                            .setTitle("กำลังยกเลิกงาน")
                            .setMessage("กรุณารอสักครู่...")
                            .setCancelable(false)
                            .create();
                    progressDialog.show();

                    // ดำเนินการยกเลิกงาน
                    cancelTask(task, acceptance, progressDialog);
                })
                .setNegativeButton("ไม่", null)
                .show();
    }

    private void cancelTask(Task task, TaskAcceptance acceptance, AlertDialog progressDialog) {
        // อัปเดตสถานะงานกลับเป็น "open"
        DatabaseReference taskRef = FirebaseDatabase.getInstance(FIREBASE_URL)
                .getReference("tasks")
                .child(task.getTaskId());

        taskRef.child("taskStatus").setValue("open");

        // อัปเดตข้อมูลการรับงาน
        DatabaseReference acceptanceRef = FirebaseDatabase.getInstance(FIREBASE_URL)
                .getReference("task_acceptances")
                .child(acceptance.getTaskAcceptanceId());

        // กำหนดเวลายกเลิกงาน
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String cancelDate = dateFormat.format(new Date());

        // สร้าง Map เพื่อป้องกันปัญหาการแปลงข้อมูล
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "cancelled");
        updates.put("cancelledAt", cancelDate);

        // บันทึกข้อมูลลง Firebase
        acceptanceRef.updateChildren(updates)
                .addOnCompleteListener(task1 -> {
                    // ปิด progress dialog
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    if (task1.isSuccessful()) {
                        Toast.makeText(getContext(), "ยกเลิกงานสำเร็จ", Toast.LENGTH_SHORT).show();
                        // โหลดข้อมูลใหม่
                        loadAcceptedTasks();
                    } else {
                        // กรณีเกิดข้อผิดพลาด ให้เปลี่ยนสถานะงานกลับเป็น in progress
                        taskRef.child("taskStatus").setValue("in progress");

                        Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการยกเลิกงาน", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error cancelling task: " + task1.getException().getMessage());
                    }
                });
    }
}