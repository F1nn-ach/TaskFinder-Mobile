package com.lab.taskfinder;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lab.taskfinder.manager.WSManager;
import com.lab.taskfinder.model.LoginModel;
import com.lab.taskfinder.model.ResponseModel;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private TextInputEditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initUI();
        setupListeners();
    }

    private void initUI() {
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);

    }

    private void setupListeners() {
        btnLogin.setOnClickListener(this::doLogin);
        txtRegister.setOnClickListener(this::navigateToRegister);
    }

    private void navigateToRegister(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void doLogin(View view) {
        inputLayoutEmail.setError(null);
        inputLayoutPassword.setError(null);

        String usernameOrEmail = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(usernameOrEmail)) {
            inputLayoutEmail.setError("กรุณากรอกอีเมลหรือชื่อผู้ใช้");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            inputLayoutPassword.setError("กรุณากรอกรหัสผ่าน");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        LoginModel loginModel = new LoginModel();
        loginModel.getLogin().setUsername(usernameOrEmail);
        loginModel.getLogin().setEmail(usernameOrEmail);
        loginModel.getLogin().setPassword(password);

        WSManager manager = WSManager.getWsManager(this);
        manager.doLogin(loginModel, new WSManager.WSManagerListener() {
            @Override
            public void onComplete(Object response) {
                Gson gson = new GsonBuilder().create();
                ResponseModel model = gson.fromJson(response.toString(), ResponseModel.class);
                if("1".equals(model.getResult())){
                    Toast.makeText(LoginActivity.this, "เข้าสู่ระบบสำเร็จ", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", usernameOrEmail);
                    startActivity(intent);
                }else{
                    Toast.makeText(LoginActivity.this, "username หรือ password ผิด", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String err) {
                Toast.makeText(LoginActivity.this, err, Toast.LENGTH_SHORT).show();
            }
        });
    }
}