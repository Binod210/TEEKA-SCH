package com.lazycoderz.childaiddoctors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity implements LocationListener {

    TextInputLayout nameInput, emailInput, passwordInput, phoneInput, qualificationInput, experienceInput;

    private FirebaseAuth mAuth;
    double lat, lng;
    public static final int pCode = 99;

    private LocationManager locationManager;

    private DatabaseReference mDatabase;
    Date d;

    ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkrequest();
            return;
        }
        mAuth = FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();

        mProgress = new ProgressDialog(this);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        onLocationChanged(location);

        nameInput = findViewById(R.id.doctorNameInput);
        emailInput = findViewById(R.id.EmailInput);
        passwordInput= findViewById(R.id.PasswordInput);
        phoneInput= findViewById(R.id.phoneInput);
        qualificationInput = findViewById(R.id.QualificationInput);
        experienceInput = findViewById(R.id.workingExperience);

        ImageView registerBtn = findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkrequest();

                StartRegistering();

            }
        });


    }

    private void StartRegistering() {

        final String name =nameInput.getEditText().getText().toString();
        final String email =emailInput.getEditText().getText().toString();
        String password =passwordInput.getEditText().getText().toString();
        final String phone =phoneInput.getEditText().getText().toString();
        final String qualification =qualificationInput.getEditText().getText().toString();
        final String experience =experienceInput.getEditText().getText().toString();

        if(TextUtils.isEmpty(name) | TextUtils.isEmpty(email) | TextUtils.isEmpty(password) | TextUtils.isEmpty(phone) | TextUtils.isEmpty(qualification) | TextUtils.isEmpty(experience)){


        }
        else{
            mProgress.setTitle("Registering....");
            mProgress.setMessage("Wait while....");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String c_uid = mAuth.getCurrentUser().getUid();
                        DatabaseReference doctorRef = mDatabase.child("doctors").child(c_uid);
                        doctorRef.child("name").setValue(name);
                        doctorRef.child("qualification").setValue(qualification);
                        doctorRef.child("experience").setValue(experience);
                        doctorRef.child("email").setValue(email);
                        doctorRef.child("phone").setValue(phone);
                        doctorRef.child("lat").setValue(lat);
                        doctorRef.child("lng").setValue(lng);
                        doctorRef.child("uid").setValue(c_uid).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(mainIntent);
                                }
                                else{
                                    Toast.makeText(RegisterActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                                mProgress.dismiss();
                            }
                        });
                    }
                    else{
                        mProgress.dismiss();

                        mProgress.hide();
                        String error ="";
                        try {
                            throw (task.getException());
                        }catch (FirebaseAuthWeakPasswordException e){
                            error ="Weak Password";
                        }catch (FirebaseAuthUserCollisionException e){
                            error = "User Already Exists";
                        }catch (FirebaseAuthInvalidCredentialsException e){
                            error = "Invalid Email Type";
                        } catch (Exception e) {
                            error = "Internal Error! Check your connection";
                            e.printStackTrace();
                        }

                        Toast.makeText(RegisterActivity.this,error,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void checkrequest() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED){

            //Toast.makeText(RegisterActivity.this, "already grandted", Toast.LENGTH_SHORT).show();
        }
        else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Permission needed").setMessage("needed").setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, pCode);

                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
            }
            else{
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, pCode);
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        Toast.makeText(this, String.valueOf(lat), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
