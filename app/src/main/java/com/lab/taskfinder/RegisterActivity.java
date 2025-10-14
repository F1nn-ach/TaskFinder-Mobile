package com.lab.taskfinder;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.lab.taskfinder.manager.WSManager;
import com.lab.taskfinder.model.UserModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout inputLayoutUsername, inputLayoutFirstName, inputLayoutLastName;
    private TextInputLayout inputLayoutEmail, inputLayoutPassword, inputLayoutPhone, inputLayoutBirthday;
    private TextInputEditText edtUsername, edtFirstName, edtLastName;
    private TextInputEditText edtEmail, edtPassword, edtPhone, edtBirthday;
    private RadioGroup radioGroupGender;
    private RadioButton radioMale, radioFemale, radioOther;
    private Button btnRegister;
    private ImageButton btnBack;
    private TextView txtLogin;


    // Calendar for birthday selection
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        initUI();

        // Setup listeners
        setupListeners();

        // Initialize date picker
        initDatePicker();
    }

    private void initUI() {
        // TextInputLayout references
        inputLayoutUsername = findViewById(R.id.inputLayoutUsername);
        inputLayoutFirstName = findViewById(R.id.inputLayoutFirstName);
        inputLayoutLastName = findViewById(R.id.inputLayoutLastName);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        inputLayoutPhone = findViewById(R.id.inputLayoutPhone);
        inputLayoutBirthday = findViewById(R.id.inputLayoutBirthday);

        // EditText references
        edtUsername = findViewById(R.id.edtUsername);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtPhone = findViewById(R.id.edtPhone);
        edtBirthday = findViewById(R.id.edtBirthday);

        // Gender selection
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioOther = findViewById(R.id.radioOther);

        // Buttons and text views
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);
        txtLogin = findViewById(R.id.txtLogin);

    }

    private void setupListeners() {
        btnRegister.setOnClickListener(this::doRegister);
        btnBack.setOnClickListener(this::doBack);
        edtBirthday.setOnClickListener(v -> showDatePickerDialog());
        txtLogin = findViewById(R.id.txtLogin);
        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void initDatePicker() {
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateBirthdayField();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Show dialog
        datePickerDialog.show();
    }

    private void updateBirthdayField() {
        edtBirthday.setText(dateFormatter.format(calendar.getTime()));
    }

    public void doRegister(View view) {
        clearErrors();

        if (!validateFields()) {
            return;
        }

        UserModel userModel = new UserModel();

        userModel.getUser().setUsername(edtUsername.getText().toString().trim());
        userModel.getUser().setFirstname(edtFirstName.getText().toString().trim());
        userModel.getUser().setLastname(edtLastName.getText().toString().trim());
        userModel.getUser().setEmail(edtEmail.getText().toString().trim());
        userModel.getUser().setPassword(edtPassword.getText().toString().trim());
        userModel.getUser().setPhoneNumber(edtPhone.getText().toString().trim());

        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        userModel.getUser().setBirthday(apiDateFormat.format(calendar.getTime()));

        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.radioMale) {
            userModel.getUser().setGender("male");
        } else if (selectedGenderId == R.id.radioFemale) {
            userModel.getUser().setGender("female");
        } else {
            userModel.getUser().setGender("other");
        }

        WSManager manager = WSManager.getWsManager(this);
        manager.doRegister(userModel, new WSManager.WSManagerListener() {
            @Override
            public void onComplete(Object response) {
                Toast.makeText(RegisterActivity.this, "Register Success..", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String err) {
                Toast.makeText(RegisterActivity.this, err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validate username
        String username = edtUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            inputLayoutUsername.setError("Username is required");
            isValid = false;
        } else if (username.length() < 4) {
            inputLayoutUsername.setError("Username must be at least 4 characters");
            isValid = false;
        }

        // Validate first name
        String firstName = edtFirstName.getText().toString().trim();
        if (TextUtils.isEmpty(firstName)) {
            inputLayoutFirstName.setError("First name is required");
            isValid = false;
        }

        // Validate last name
        String lastName = edtLastName.getText().toString().trim();
        if (TextUtils.isEmpty(lastName)) {
            inputLayoutLastName.setError("Last name is required");
            isValid = false;
        }

        // Validate email
        String email = edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            inputLayoutEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayoutEmail.setError("Invalid email format");
            isValid = false;
        }

        // Validate password
        String password = edtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            inputLayoutPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            inputLayoutPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        // Validate phone
        String phone = edtPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            inputLayoutPhone.setError("Phone number is required");
            isValid = false;
        } else if (!Patterns.PHONE.matcher(phone).matches()) {
            inputLayoutPhone.setError("Invalid phone number format");
            isValid = false;
        }

        // Validate birthday
        String birthday = edtBirthday.getText().toString().trim();
        if (TextUtils.isEmpty(birthday)) {
            inputLayoutBirthday.setError("Birthday is required");
            isValid = false;
        }

        // Validate gender
        if (radioGroupGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        inputLayoutUsername.setError(null);
        inputLayoutFirstName.setError(null);
        inputLayoutLastName.setError(null);
        inputLayoutEmail.setError(null);
        inputLayoutPassword.setError(null);
        inputLayoutPhone.setError(null);
        inputLayoutBirthday.setError(null);
    }

    public void doBack(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}