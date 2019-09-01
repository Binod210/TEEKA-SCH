package com.lazycoderz.childaid;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegisterAcitivy extends AppCompatActivity implements LocationListener {

    private TextInputLayout motherNameInput, fathernameInput, dobInput, emailInput, phoneInput, passwordInput;
    private ImageView registerBtn;
    private FirebaseAuth mAuth;
    double lat, lng;
    public static final int pCode = 99;

    Spinner spinner;
    Long dobTimestamp =Long.valueOf(0);
    private LocationManager locationManager;

    private DatabaseReference mDatabase;
    Date d;

    ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_acitivy);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkrequest();
            return;
        }

        mProgress = new ProgressDialog(this);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        onLocationChanged(location);


        spinner = findViewById(R.id.bloodGroup);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.BloodGroup, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        motherNameInput = findViewById(R.id.childNameInput);
        fathernameInput = findViewById(R.id.WeightInput);
        dobInput = findViewById(R.id.DOBInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.pYearInput);


        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                d=myCalendar.getTime();
                dobTimestamp=myCalendar.getTimeInMillis();
                dobInput.getEditText().setText(sdf.format(myCalendar.getTime()));


            }

        };
        dobInput.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DatePickerDialog(RegisterAcitivy.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                checkrequest();
                startRegistering();








            }
        });





    }



    private void startRegistering() {
        final String motherName = motherNameInput.getEditText().getText().toString();
        final String fathername = fathernameInput.getEditText().getText().toString();
        String motherDob = dobInput.getEditText().getText().toString();
        final String email = emailInput.getEditText().getText().toString();
        final String phone = phoneInput.getEditText().getText().toString();
        String password = passwordInput.getEditText().getText().toString();
        final String bloodgroup = spinner.getSelectedItem().toString();

        if(TextUtils.isEmpty(motherName) | TextUtils.isEmpty(fathername) | TextUtils.isEmpty(motherDob) | TextUtils.isEmpty(phone)){
            Toast.makeText(this, "All the field are mandatory", Toast.LENGTH_SHORT).show();
        }
        else{
            if(bloodgroup.equals("Choose Blood Group") | dobTimestamp ==0){
                Toast.makeText(this, "Please select the blood group or double click on the mother dob to choose the date", Toast.LENGTH_SHORT).show();

            }
            else{
                mProgress.setTitle("Registering..");
                mProgress.setMessage("Please wait a while.....");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            final String c_uid = mAuth.getCurrentUser().getUid();

                           DatabaseReference uDatabase= mDatabase.child("users").child(c_uid);
                            uDatabase.child("u_id").setValue(ServerValue.TIMESTAMP);

                            uDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String u_id = dataSnapshot.child("u_id").getValue().toString();
                                    Long diff = new Date().getTime()-dobTimestamp;

                                    DatabaseReference userdetail = mDatabase.child("userdata").child(u_id);
                                    userdetail.child("motherName").setValue(motherName);
                                    userdetail.child("fatherName").setValue(fathername);
                                    userdetail.child("dobm").setValue(dobTimestamp);
                                    userdetail.child("Age").setValue(diff);
                                    userdetail.child("bloodgroup").setValue(bloodgroup);
                                    userdetail.child("email").setValue(email);
                                    userdetail.child("phone").setValue(phone);
                                    userdetail.child("uid").setValue(c_uid);
                                    userdetail.child("id").setValue(u_id);
                                    userdetail.child("lat").setValue(lat);
                                    userdetail.child("situation").setValue("Delivered");
                                    userdetail.child("lng").setValue(lng).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterAcitivy.this);
                                                builder.setTitle("Pregnancy Status");
                                                String[] ps = {"Pregnant", "Delivered"};
                                                builder.setItems(ps, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                        switch(i){
                                                            case 0:
                                                                startActivity(new Intent(RegisterAcitivy.this,PregnancyActivity.class));
                                                                break;
                                                            case 1:
                                                                startActivity(new Intent(RegisterAcitivy.this, AddChildActivity.class));
                                                                break;
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                            else {
                                                mProgress.hide();
                                                String error = "";
                                                try {
                                                    throw (task.getException());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    error = e.toString();
                                                }
                                                Toast.makeText(RegisterAcitivy.this, "error:" + error, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });







                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            /*Intent mainIntent = new Intent(RegisterAcitivy.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainIntent);*/
                        }
                        else{
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

                            Toast.makeText(RegisterAcitivy.this,error,Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
            }
        }



    private void checkrequest() {

        if (ContextCompat.checkSelfPermission(RegisterAcitivy.this, Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED){

            //Toast.makeText(RegisterAcitivy.this, "already grandted", Toast.LENGTH_SHORT).show();
        }
        else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Permission needed").setMessage("needed").setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(RegisterAcitivy.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, pCode);

                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
            }
            else{
                ActivityCompat.requestPermissions(RegisterAcitivy.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, pCode);
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
