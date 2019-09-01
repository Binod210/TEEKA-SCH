package com.lazycoderz.childaiddoctors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText emailInput, passwordInput;
    ImageView LoginBtn;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setContentView(R.layout.activity_login);
        emailInput= findViewById(R.id.pMonthInput);
        passwordInput = findViewById(R.id.pYearInput);
        LoginBtn = findViewById(R.id.loginBtn);
        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);
        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLogin();
            }
        });


    }
    private void startLogin() {

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if(TextUtils.isEmpty(email) | TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter all the field", Toast.LENGTH_SHORT).show();
        }
        else{
            mProgress.setTitle("Logging In");
            mProgress.setMessage("Please be Patience......");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "You Cannot Login Here", Toast.LENGTH_SHORT).show();
                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainIntent);
                        finish();
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Login Credential are wrong", Toast.LENGTH_SHORT).show();
                    }
                    mProgress.dismiss();

                }
            });


        }
    }
}
