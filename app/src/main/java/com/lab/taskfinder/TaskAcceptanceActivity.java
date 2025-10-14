package com.lab.taskfinder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TaskAcceptanceActivity extends AppCompatActivity {

    private static final String FIREBASE_URL = "https://taskfinder-97e63-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private LinearLayout emptyView;
    private Button btnBack;
    private TaskPageAdapter pagerAdapter; // แก้ไขให้ใช้ชื่อ TaskPageAdapter ให้สอดคล้องกัน

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tack_acceptance_list);

        // จัดการกับ window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // เรียกใช้งานฟังก์ชันเริ่มต้น
        initializeViews();
        setupTabLayout();
        setupListeners();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        emptyView = findViewById(R.id.emptyView);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupTabLayout() {
        // สร้าง adapter สำหรับ ViewPager
        pagerAdapter = new TaskPageAdapter(this); // ใช้ TaskPageAdapter
        viewPager.setAdapter(pagerAdapter);

        // เชื่อม TabLayout กับ ViewPager
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("กำลังดำเนินการ");
                        break;
                    case 1:
                        tab.setText("เสร็จสิ้นแล้ว");
                        break;
                    case 2:
                        tab.setText("ยกเลิก");
                        break;
                }
            }
        }).attach();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    public void showEmptyView(boolean isEmpty) {
        if (isEmpty) {
            emptyView.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
        }
    }
}