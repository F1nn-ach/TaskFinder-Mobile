package com.lab.taskfinder;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lab.taskfinder.model.Task;
import com.lab.taskfinder.model.UserModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyTaskActivity extends AppCompatActivity {

    private LinearLayout linearLayoutTasks;
    private Button btnBack;
    private ImageButton btnProfile;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private UserModel currentUser;
    private static final String TAG = "MyTaskActivity";
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_task_acctivity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseRef = FirebaseDatabase.getInstance(
                        "https://taskfinder-97e63-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("tasks");

        linearLayoutTasks = findViewById(R.id.linearLayoutTasks);
        btnBack = findViewById(R.id.btnBack);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Set up toolbar if it exists in your layout
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("My Tasks");

            // Set up profile button if it exists
            btnProfile = toolbar.findViewById(R.id.btnProfile);
            if (btnProfile != null) {
                btnProfile.setOnClickListener(v -> {
                    // Handle profile button click
                    Intent intent = new Intent(MyTaskActivity.this, ProfileActivity.class);
                    intent.putExtra("user", currentUser);
                    startActivity(intent);
                });
            }
        }
        currentUser = (UserModel) getIntent().getSerializableExtra("user");

        if (currentUser == null) {
            Toast.makeText(this, "Error: User data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_my_tasks);
            bottomNavigation.setOnItemSelectedListener(this::onNavigationItemSelected);
        }

        // Load tasks
        getAllMyTasks(currentUser);
    }

    private void getAllMyTasks(UserModel user) {
        if (user == null || user.getUser() == null) {
            Log.e(TAG, "User data is null or incomplete");
            return;
        }

        String username = user.getUser().getUsername();
        Log.d(TAG, "Fetching tasks for user: " + username);

        // Show loading indicator
        linearLayoutTasks.removeAllViews();
        TextView loadingText = new TextView(this);
        loadingText.setText("Loading tasks...");
        loadingText.setTextSize(16);
        loadingText.setGravity(Gravity.CENTER);
        loadingText.setPadding(0, 50, 0, 0);
        linearLayoutTasks.addView(loadingText);

        // Query tasks where the current user is the client
        databaseRef.orderByChild("taskClient").equalTo(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linearLayoutTasks.removeAllViews();

                if (!snapshot.hasChildren()) {
                    TextView emptyMessage = new TextView(linearLayoutTasks.getContext());
                    emptyMessage.setText("You don't have any tasks yet.");
                    emptyMessage.setTextSize(16);
                    emptyMessage.setGravity(Gravity.CENTER);
                    emptyMessage.setPadding(0, 50, 0, 0);
                    linearLayoutTasks.addView(emptyMessage);
                    return;
                }

                Log.d(TAG, "Found " + snapshot.getChildrenCount() + " tasks");

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        // Inflate the task card layout
                        View taskCard = LayoutInflater.from(linearLayoutTasks.getContext())
                                .inflate(R.layout.task_card_with_delete, linearLayoutTasks, false);

                        // Find views in the card
                        TextView taskTitle = taskCard.findViewById(R.id.txtTaskTitle);
                        TextView taskDescription = taskCard.findViewById(R.id.txtTaskDescription);
                        TextView taskPrice = taskCard.findViewById(R.id.txtTaskPrice);
                        TextView taskDate = taskCard.findViewById(R.id.txtTaskDate);
                        TextView taskLocation = taskCard.findViewById(R.id.txtTaskLocation);
                        View categoryIndicator = taskCard.findViewById(R.id.viewCategoryIndicator);
                        Button btnViewTask = taskCard.findViewById(R.id.btnViewTask);
                        Button btnDeleteTask = taskCard.findViewById(R.id.btnDeleteTask);

                        // Set task details
                        taskTitle.setText(task.getTaskTitle());
                        taskDescription.setText(task.getTaskDescription());
                        taskPrice.setText(String.format("$%.2f", (double)task.getTaskPrice()));

                        // Set location if available
                        if (task.getTaskAddress() != null && !task.getTaskAddress().isEmpty()) {
                            taskLocation.setText(task.getTaskAddress());
                        } else {
                            taskLocation.setText("Location not specified");
                        }

                        // Format date correctly
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        try {
                            if (task.getTaskCreatedAt() != null) {
                                // Try parsing as timestamp in milliseconds
                                try {
                                    long timestamp = Long.parseLong(task.getTaskCreatedAt());
                                    Date date = new Date(timestamp);
                                    taskDate.setText(outputFormat.format(date));
                                } catch (NumberFormatException e) {
                                    // If not a number, try parsing as formatted date string
                                    try {
                                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                        Date date = inputFormat.parse(task.getTaskCreatedAt());
                                        taskDate.setText(outputFormat.format(date));
                                    } catch (ParseException pe) {
                                        Log.e(TAG, "Error parsing date string", pe);
                                        taskDate.setText(task.getTaskCreatedAt()); // Just show the raw string
                                    }
                                }
                            } else {
                                taskDate.setText("No date");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing date", e);
                            taskDate.setText("Invalid date");
                        }

                        // Set task status color
                        if (task.getTaskStatus() != null) {
                            switch (task.getTaskStatus().toLowerCase()) {
                                case "open":
                                    categoryIndicator.setBackgroundColor(ContextCompat.getColor(MyTaskActivity.this, R.color.green));
                                    btnDeleteTask.setVisibility(View.VISIBLE); // Only allow deletion of open tasks
                                    break;
                                case "in progress":
                                    categoryIndicator.setBackgroundColor(ContextCompat.getColor(MyTaskActivity.this, R.color.orange));
                                    btnDeleteTask.setVisibility(View.GONE); // Can't delete in-progress tasks
                                    break;
                                case "completed":
                                    categoryIndicator.setBackgroundColor(ContextCompat.getColor(MyTaskActivity.this, R.color.blue));
                                    btnDeleteTask.setVisibility(View.GONE); // Can't delete completed tasks
                                    break;
                                default:
                                    categoryIndicator.setBackgroundColor(ContextCompat.getColor(MyTaskActivity.this, R.color.grey));
                                    btnDeleteTask.setVisibility(View.VISIBLE); // Allow deletion of tasks with unknown status
                                    break;
                            }
                        }

                        // View Task button click event
                        btnViewTask.setOnClickListener(v -> {
                            Intent intent = new Intent(MyTaskActivity.this, ViewTaskActivity.class);
                            intent.putExtra("TASK", task);
                            intent.putExtra("user", currentUser);
                            startActivity(intent);
                        });

                        // Delete Task button click event
                        btnDeleteTask.setOnClickListener(v -> {
                            showDeleteConfirmationDialog(task);
                        });

                        linearLayoutTasks.addView(taskCard);
                    } else {
                        Log.e(TAG, "Null task object encountered");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching tasks: " + error.getMessage());
                linearLayoutTasks.removeAllViews();

                TextView errorMessage = new TextView(linearLayoutTasks.getContext());
                errorMessage.setText("Error loading tasks. Please try again.");
                errorMessage.setTextSize(16);
                errorMessage.setGravity(Gravity.CENTER);
                errorMessage.setPadding(0, 50, 0, 0);
                linearLayoutTasks.addView(errorMessage);

                Toast.makeText(MyTaskActivity.this, "Failed to load tasks: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showDeleteConfirmationDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task? This action cannot be undone.");

        // Add the buttons
        builder.setPositiveButton("Delete", (dialog, id) -> {
            deleteTask(task);
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            dialog.dismiss();
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void deleteTask(Task task) {
        // Show a progress dialog while deleting
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deleting Task");
        builder.setMessage("Please wait...");
        builder.setCancelable(false);
        AlertDialog progressDialog = builder.create();
        progressDialog.show();

        // Make sure we have a valid task ID
        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: Task ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delete the task from Firebase
        databaseRef.child(task.getTaskId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(MyTaskActivity.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                    // The UI will automatically update due to the ValueEventListener in getAllMyTasks
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Error deleting task: " + e.getMessage());
                    Toast.makeText(MyTaskActivity.this, "Failed to delete task: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh tasks when returning to this activity
        if (currentUser != null) {
            getAllMyTasks(currentUser);
        }

        // Make sure the correct navigation item is selected
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_my_tasks);
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_my_tasks) {
            return true;
        }

        if (itemId == R.id.nav_home) {
            if (currentUser != null) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            } else {
                Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.nav_accept) {
            if (currentUser != null) {
                Intent intent = new Intent(this, TaskAcceptanceActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            } else {
                Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.nav_profile) {
            if (currentUser != null) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            } else {
                Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            return false;
        }

        return true;
    }
}