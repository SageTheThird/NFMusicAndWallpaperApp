package com.homie.nf;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MyRecyclerViewActivity extends AppCompatActivity {
    RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    setContentView(R.layout.my_recycle_view);

    final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
    databaseReference.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String fileName=dataSnapshot.getKey();//return the filename
            String url=dataSnapshot.getValue(String.class);//return url for our


            ((myAdapter)recyclerView.getAdapter()).update(fileName,url);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
         recyclerView=findViewById(R.id.recyclerView);


        recyclerView.setLayoutManager(new LinearLayoutManager(MyRecyclerViewActivity.this));
        myAdapter myAdapter=new myAdapter(recyclerView,
                MyRecyclerViewActivity.this,
                new ArrayList<String>(),new ArrayList<String>());

        recyclerView.setAdapter(myAdapter);







    }
}
