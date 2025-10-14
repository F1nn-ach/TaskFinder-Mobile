package com.lab.taskfinder;

import static android.app.PendingIntent.getActivity;
import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lab.taskfinder.asyn_task.UserProfileTask;
import com.lab.taskfinder.model.Task;
import com.lab.taskfinder.model.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements CallBackService {
    private ArrayList<UserModel> listuser;
    private LinearLayout linearLayoutTasks;
    private UserModel currentUser; // Store logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listuser = new ArrayList<>();
        linearLayoutTasks = findViewById(R.id.linearLayoutTasks);

        FirebaseApp.initializeApp(this);

        String root = getString(R.string.root_url);
        String url = getString(R.string.getprofile);
        String username = getIntent().getStringExtra("username");

        if (username != null) {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", username);
                new UserProfileTask(this).execute(root + url, requestBody.toString());
            } catch (JSONException e) {
                Log.e("JSON Error", "Failed to create request body", e);
            }
        } else {
            Log.e("MainActivity", "Username is null");
        }

        getAllTask();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> BottomNavigationView(item));
    }

    @Override
    public void onPreCallService() {}

    @Override
    public void onCallService() {}

    @Override
    public void onRequestCompleteListener(ArrayList<UserModel> userModelArrayList) {
        if (userModelArrayList != null && !userModelArrayList.isEmpty()) {
            listuser = userModelArrayList;
            currentUser = listuser.get(0);

            if (currentUser != null && currentUser.getUser() != null) {
                Log.d("User Profile", "Username: " + currentUser.getUser().getUsername());
                Log.d("User Profile", "Name: " + currentUser.getUser().getFirstname() + " " + currentUser.getUser().getLastname());
                Log.d("User Profile", "Email: " + currentUser.getUser().getEmail());
            } else {
                Log.e("Error", "User object is null or incomplete");
            }
        } else {
            Log.e("Error", "Failed to retrieve user data - userModelArrayList is empty or null");
        }

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabbAddTask();
            }
        });
    }

    @Override
    public void onRequestFailed(String result) {
        Log.e("Error", "API request failed: " + result);
    }

    private void getAllTask() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance(
                        "https://taskfinder-97e63-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("tasks");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linearLayoutTasks.removeAllViews();

                if (!snapshot.hasChildren()) {
                    TextView emptyMessage = new TextView(linearLayoutTasks.getContext());
                    emptyMessage.setText("No tasks available.");
                    emptyMessage.setTextSize(16);
                    emptyMessage.setGravity(Gravity.CENTER);
                    emptyMessage.setPadding(0, 50, 0, 0);
                    linearLayoutTasks.addView(emptyMessage);
                    return;
                }

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        View taskCard = LayoutInflater.from(linearLayoutTasks.getContext())
                                .inflate(R.layout.task_card, linearLayoutTasks, false);

                        TextView taskTitle = taskCard.findViewById(R.id.txtTaskTitle);
                        TextView taskDescription = taskCard.findViewById(R.id.txtTaskDescription);
                        TextView taskPrice = taskCard.findViewById(R.id.txtTaskPrice);
                        TextView taskDate = taskCard.findViewById(R.id.txtTaskDate);
                        TextView taskLocation = taskCard.findViewById(R.id.txtTaskLocation);
                        View categoryIndicator = taskCard.findViewById(R.id.viewCategoryIndicator);
                        Button btnViewTask = taskCard.findViewById(R.id.btnViewTask);

                        taskTitle.setText(task.getTaskTitle());
                        taskDescription.setText(task.getTaskDescription());
                        taskPrice.setText(String.format("$%.2f", task.getTaskPrice()));

                        if (task.getTaskAddress() != null && !task.getTaskAddress().isEmpty()) {
                            taskLocation.setText(task.getTaskAddress());
                        } else {
                            taskLocation.setText("Location not specified");
                        }

                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        try {
                            if (task.getTaskCreatedAt() != null) {
                                try {
                                    long timestamp = Long.parseLong(task.getTaskCreatedAt());
                                    Date date = new Date(timestamp);
                                    taskDate.setText(outputFormat.format(date));
                                } catch (NumberFormatException e) {
                                    try {
                                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                        Date date = inputFormat.parse(task.getTaskCreatedAt());
                                        taskDate.setText(outputFormat.format(date));
                                    } catch (ParseException pe) {
                                        Log.e("DateParsing", "Error parsing date string", pe);
                                        taskDate.setText(task.getTaskCreatedAt());
                                    }
                                }
                            } else {
                                taskDate.setText("No date");
                            }
                        } catch (Exception e) {
                            Log.e("DateParsing", "Error processing date", e);
                            taskDate.setText("Invalid date");
                        }

                        if (task.getTaskStatus() != null) {
                            switch (task.getTaskStatus().toLowerCase()) {
                                case "open":
                                    categoryIndicator.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.green));
                                    break;
                                case "in progress":
                                    categoryIndicator.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.orange));
                                    break;
                                case "completed":
                                    categoryIndicator.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue));
                                    break;
                                default:
                                    categoryIndicator.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.grey));
                                    break;
                            }
                        }

                        btnViewTask.setOnClickListener(v -> {
                            Intent intent = new Intent(MainActivity.this, ViewTaskActivity.class);
                            intent.putExtra("user", currentUser);
                            intent.putExtra("TASK", task);
                            startActivity(intent);
                        });

                        linearLayoutTasks.addView(taskCard);
                    } else {
                        Log.e("Firebase", "Null task object encountered");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error reading data", databaseError.toException());
            }
        });
    }

    @Override
    public boolean BottomNavigationView(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            if (currentUser != null) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            } else {
                Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.nav_my_tasks) {
            if (currentUser != null) {
                Intent intent = new Intent(this, MyTaskActivity.class);
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
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
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

    public boolean fabbAddTask(){
        Intent intent = new Intent(MainActivity.this, CreateTaskActivity.class);
        intent.putExtra("user", currentUser);
        startActivity(intent);
        return true;
    }
}
