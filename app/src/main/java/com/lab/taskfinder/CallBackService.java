package com.lab.taskfinder;

import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.lab.taskfinder.model.UserModel;

import java.util.ArrayList;

public interface CallBackService {
    void onPreCallService();
    void onCallService();
    void onRequestCompleteListener(ArrayList<UserModel> userModelArrayList);
    void onRequestFailed(String result);
    boolean BottomNavigationView(@NonNull MenuItem item);
}
