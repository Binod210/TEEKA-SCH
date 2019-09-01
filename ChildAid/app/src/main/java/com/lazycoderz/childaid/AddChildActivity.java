package com.lazycoderz.childaid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddChildActivity extends AppCompatActivity {

    Spinner bg,gs;

    TextInputLayout nameInput, weightInput, dobInput;
    long dobTimestamp;
    Button submitBtn;
    DatabaseReference mDatabase;
    ProgressDialog mProgress;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        bg = findViewById(R.id.bloodGroup);
        gs = findViewById(R.id.gender);

        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        submitBtn = findViewById(R.id.SubmitData);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.BloodGroup, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bg.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.Gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gs.setAdapter(adapter1);

        nameInput = findViewById(R.id.childNameInput);
        weightInput = findViewById(R.id.WeightInput);
        dobInput = findViewById(R.id.DOBInput);

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
                //d=myCalendar.getTime();
                dobTimestamp=myCalendar.getTimeInMillis();
                dobInput.getEditText().setText(sdf.format(myCalendar.getTime()));


            }

        };
        dobInput.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DatePickerDialog(AddChildActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitData();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void submitData() {

        final String name = nameInput .getEditText().getText().toString();
        String weight = weightInput.getEditText().getText().toString();
        String dob = dobInput .getEditText().getText().toString();

        String bloodgrp = bg.getSelectedItem().toString();
        String gender = gs.getSelectedItem().toString();

        if(TextUtils.isEmpty(name) | TextUtils.isEmpty(weight) |TextUtils.isEmpty(dob)){
            Toast.makeText(this, "All the field must be filled", Toast.LENGTH_SHORT).show();
        }
        else{
            if(bloodgrp.equals("Choose Blood Group") | gender.equals("Choose Gender")){
                Toast.makeText(this, "gender or blood group should be selected", Toast.LENGTH_SHORT).show();
            }else{
                mProgress.setTitle("Adding Data");
                mProgress.setMessage("Please wait for while");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();
                String cuid = mAuth.getCurrentUser().getUid();

                mDatabase.child("users").child(cuid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       final String uid = dataSnapshot.child("u_id").getValue().toString();
                        DatabaseReference cuserRef = mDatabase.child("child").child(uid).push();
                        cuserRef.child("childuid").setValue(ServerValue.TIMESTAMP);
                        mDatabase.child("userdata").child(uid).child("situation").setValue("Delivered");

                        cuserRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String childuid = dataSnapshot.child("childuid").getValue().toString();
                                DatabaseReference childdata = mDatabase.child("childinfo").child(uid).child(childuid);
                                childdata.child("name").setValue(name);
                                childdata.child("weight").setValue(name);
                                childdata.child("dob").setValue(name);
                                childdata.child("bloodgroup").setValue(name);
                                childdata.child("gender").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            DatabaseReference pregnancyRef = mDatabase.child("pregnancy").child(uid).push();
                                            pregnancyRef.child("pregnancymonth").setValue("default");
                                            pregnancyRef.child("pregnancyyear").setValue("default");
                                            pregnancyRef.child("deliverydate").setValue("default").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(AddChildActivity.this, "Child Added Successfully", Toast.LENGTH_SHORT).show();
                                                        Intent mainIntent = new Intent(AddChildActivity.this, MainActivity.class);
                                                        startActivity(mainIntent);
                                                        mProgress.dismiss();
                                                    }
                                                }
                                            });

                                        }
                                        mProgress.dismiss();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                mProgress.dismiss();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }
        }
    }
}
