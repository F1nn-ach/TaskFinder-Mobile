package com.lab.taskfinder;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.lab.taskfinder.TaskListFragment;

public class TaskPageAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 3;

    public TaskPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public TaskPageAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public TaskPageAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // สร้าง Fragment ตามตำแหน่งแท็บ
        String status;
        switch (position) {
            case 0:
                status = "in progress";
                break;
            case 1:
                status = "completed";
                break;
            case 2:
                status = "cancelled";
                break;
            default:
                status = "in progress";
        }

        return TaskListFragment.newInstance(status);
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}