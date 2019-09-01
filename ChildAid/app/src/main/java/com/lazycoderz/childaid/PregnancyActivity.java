package com.lazycoderz.childaid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PregnancyActivity extends AppCompatActivity {

    EditText monthInput, yearInput;
    Button Submit;
    DatabaseReference userDatabase;
    FirebaseAuth mAuth;
    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregnancy);
        monthInput = findViewById(R.id.pMonthInput);
        yearInput = findViewById(R.id.pYearInput);
        mProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference();
        Submit = findViewById(R.id.SubmitBtn);
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String month = monthInput.getText().toString();
                final String year = yearInput.getText().toString();

                if(TextUtils.isEmpty(month) | TextUtils.isEmpty(year)){

                }else{
                    mProgress.setTitle("Adding Data");
                    mProgress.setMessage("Please wait while");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                    String cuid = mAuth.getCurrentUser().getUid();
                    userDatabase.child("users").child(cuid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String uid = dataSnapshot.child("u_id").getValue().toString();

                            DatabaseReference pDatabase= userDatabase.child("pregnancy").child(uid).push();

                            userDatabase.child("userdata").child(uid).child("situation").setValue("Pregnancy");
                            pDatabase.child("pregnancymonth").setValue(month);
                            pDatabase.child("pregnancyyear").setValue(year);
                            pDatabase.child("deliverydate").setValue("default").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(PregnancyActivity.this, "Successfully added pregnancy details", Toast.LENGTH_SHORT).show();
                                    }
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
        });
    }
}
