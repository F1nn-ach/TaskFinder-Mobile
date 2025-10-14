package com.lab.taskfinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lab.taskfinder.manager.WSManager;
import com.lab.taskfinder.model.UserModel;

public class ProfileActivity extends AppCompatActivity {

    private EditText txtUsername, txtFullName, txtEmail, txtPhone;
    private Button btnEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initializeViews();
        UserModel userModel = (UserModel) getIntent().getSerializableExtra("user");

        ImageView imgBackButton = findViewById(R.id.imgBackButton);
        imgBackButton.setOnClickListener(v -> finish());

        if (userModel != null) {
            setUserData(userModel);
        } else {
            Log.e("ProfileActivity", "UserModel is null!");
            Toast.makeText(this, "Could not load user data", Toast.LENGTH_SHORT).show();
        }

        btnEditProfile.setOnClickListener(v -> saveUserProfile(userModel));
    }

    private void initializeViews() {
        txtUsername = findViewById(R.id.edtUsername);
        txtFullName = findViewById(R.id.edtFullName);
        txtEmail = findViewById(R.id.edtEmail);
        txtPhone = findViewById(R.id.edtPhone);
        btnEditProfile = findViewById(R.id.btnEditProfile);
    }

    public void setUserData(UserModel userModel) {
        try {
            String fullname = userModel.getUser().getFirstname() + " " + userModel.getUser().getLastname();
            txtUsername.setText(userModel.getUser().getUsername());
            txtFullName.setText(fullname);
            txtEmail.setText(userModel.getUser().getEmail());
            txtPhone.setText(userModel.getUser().getPhoneNumber());
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error setting user data: " + e.getMessage());
            Toast.makeText(this, "Error displaying user data", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserProfile(UserModel userModel) {
        EditText edtFullName = findViewById(R.id.edtFullName);
        EditText edtPhone = findViewById(R.id.edtPhone);

        String fullName = edtFullName.getText().toString();
        String phone = edtPhone.getText().toString();


        String[] nameParts = fullName.split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        UserModel newUser = new UserModel();
        newUser.getUser().setUsername(userModel.getUser().getUsername());
        newUser.getUser().setFirstname(firstName);
        newUser.getUser().setLastname(lastName);
        newUser.getUser().setEmail(userModel.getUser().getEmail());
        newUser.getUser().setPassword(userModel.getUser().getPassword());
        newUser.getUser().setGender(userModel.getUser().getGender());
        newUser.getUser().setBirthday(userModel.getUser().getBirthday());
        newUser.getUser().setPhoneNumber(phone);

        WSManager manager = WSManager.getWsManager(this);
        manager.doUpdateUser(newUser, new WSManager.WSManagerListener(){
            @Override
            public void onComplete(Object response) {
                Toast.makeText(ProfileActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onError(String err) {
                Toast.makeText(ProfileActivity.this, err, Toast.LENGTH_SHORT).show();
            }
        });
    }

}