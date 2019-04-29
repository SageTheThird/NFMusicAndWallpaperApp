package com.homie.nf;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class WallpaperMain extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=firebaseFirestore.collection("wallpapers");

    ImageRecyclerView imageRecyclerViewAdapter;
    Button tool_back, tool_list, tool_refresh;
    Toolbar toolbar;
    static RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_wallpaper_main);

        setupRecyclerView();
        tool_back =    findViewById(R.id.tool_back);
        tool_list =    findViewById(R.id.tool_list);
        tool_refresh = findViewById(R.id.tool_refresh);
        toolbar =      findViewById(R.id.toolBar);

        //myRecycleView = findViewById(R.id.recyclerview_id);
       /* ExampleRunnable runnable=new ExampleRunnable();
        new Thread(runnable);*/

        setupRecyclerView();

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);




        imageRecyclerViewAdapter.setOnItemClickListener(new ImageRecyclerView.onItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
               /* Wallpaper wallpaper=documentSnapshot.toObject(Wallpaper.class);
                String id=documentSnapshot.getId();
                String path=documentSnapshot.getReference().getPath();*/
                String downloadUrl=documentSnapshot.getString("downloadUrl");

                startActivity(new Intent(WallpaperMain.this,Book_Activity.class)
                        .putExtra("downloadUrl",downloadUrl)
                );



                Toast.makeText(WallpaperMain.this, "Position: "+position, Toast.LENGTH_LONG).show();

            }
        });



    }
    public void setupRecyclerView (){
        Query query=collectionReference.orderBy("priority",Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Wallpaper> firebaseRecyclerOptions=new FirestoreRecyclerOptions.Builder<Wallpaper>()
                .setQuery(query,Wallpaper.class)
                .build();

        imageRecyclerViewAdapter=new ImageRecyclerView(firebaseRecyclerOptions,this);

        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        recyclerView.setAdapter(imageRecyclerViewAdapter);




    }

    @Override
    protected void onStart() {
        super.onStart();
        imageRecyclerViewAdapter.startListening();
    }


    @Override
    protected void onStop() {
        super.onStop();
        imageRecyclerViewAdapter.stopListening();

    }
}
