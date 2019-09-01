package com.lazycoderz.childaid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ScheduleActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    ArrayList<Schedule> dataList = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);


        recyclerView = (RecyclerView) findViewById(R.id.listdata);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        String cuid =mAuth.getCurrentUser().getUid();
        Toast.makeText(this, cuid, Toast.LENGTH_SHORT).show();

        mDatabase.child("users").child(cuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Toast.makeText(ScheduleActivity.this, dataSnapshot.toString(), Toast.LENGTH_SHORT).show();

                String uid=dataSnapshot.child("u_id").getValue().toString();
                mDatabase.child("userdata").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String situation = dataSnapshot.child("situation").getValue().toString();
                        if(situation.equals("Delivered")){
                            //userdatabase.setValue();
                            mDatabase.child("schedule").child("child").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot maindata: dataSnapshot.getChildren()){
                                        Schedule schedule = maindata.getValue(Schedule.class);
                                        if(schedule.getTime()>1){
                                            dataList.add(schedule);

                                            for (Schedule schedule1: dataList){
                                                Toast.makeText(ScheduleActivity.this, schedule.getDesc(), Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                        else{
                            //userdatabase.setValue();
                            mDatabase.child("schedule").child("pregnancy").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot maindata: dataSnapshot.getChildren()){
                                        Schedule schedule = maindata.getValue(Schedule.class);
                                        if(schedule.getTime()>1){
                                            dataList.add(schedule);
                                            mAdapter = new MyAdapter(dataList);

                                            recyclerView.setAdapter(mAdapter);


                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private ArrayList<Schedule> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public View view;

            TextView name, desc, dose,time;
            public MyViewHolder(View v) {
                super(v);
                view = v;
                name = view.findViewById(R.id.VaccineName);
                desc = view.findViewById(R.id.VaccineDesc);
                dose = view.findViewById(R.id.VaccineDose);
                time = view.findViewById(R.id.vaccineTime);





            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(ArrayList<Schedule> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            // create a new view
            View v =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_schedule_layout, parent, false);

            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {


            holder.name.setText(mDataset.get(position).getName());
            holder.desc.setText(mDataset.get(position).getDesc());
            holder.time.setText(String.valueOf(mDataset.get(position).getTime()));
            holder.dose.setText(String.valueOf(mDataset.get(position).getQuantity()));




        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}
