package com.homie.nf.Wallpapers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.homie.nf.Adapters.UltraPagerAdapter;
import com.homie.nf.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.homie.nf.Wallpapers.Book_Activity.showSnackbar;


public class Book_Activity extends AppCompatActivity {
    private static final String TAG = "Book_Activity";

    // android:centerColor="#203A43"
    ArrayList<String> imagesUrlLis = new ArrayList<>();
    private BottomNavigationView bottomNavigationView;
    private Button back_viewPager;
    private String downloadUrl;
    private ImageView img;
    private FloatingActionButton downloadBtn, setBtn;
    private int urlPosition;

    private static CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_book_);
        back_viewPager = findViewById(R.id.tool_back_viewPager);
        downloadBtn = findViewById(R.id.actionBtn1);
        setBtn = findViewById(R.id.actionBtn2);
        coordinatorLayout=findViewById(R.id.coordinatorLayout);



        Intent intent = getIntent();
        downloadUrl = intent.getStringExtra("downloadUrl");
        imagesUrlLis = intent.getStringArrayListExtra("imagesUrl");
        urlPosition = intent.getIntExtra("position", 5);

        Log.d(TAG, "onCreate: Position : " + urlPosition);
        for (int i = 0; i < imagesUrlLis.size(); i++) {
            String url = imagesUrlLis.get(i);
            Log.d(TAG, "imageUrlsArrayList: " + url);
        }

        adapterSetup();


        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadImage(downloadUrl);

                //Toast.makeText(Book_Activity.this, "Downloading", Toast.LENGTH_LONG).show();
            }
        });
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSnackbar("Setting Wallpaper");
            }
        });

    }

    public void showToast(String msg) {
        Toast.makeText(Book_Activity.this, msg, Toast.LENGTH_LONG).show();

    }


    void DownloadImage(String ImageUrl) {

        if (ContextCompat.checkSelfPermission(Book_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(Book_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Book_Activity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            ActivityCompat.requestPermissions(Book_Activity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
            showSnackbar("Need Permissions");
        } else {

            showSnackbar("Downloading Image..");
            //Asynctask to create a thread to downlaod image in the background

            DownloadsImage downloadsImage = new DownloadsImage(Book_Activity.this);
            downloadsImage.execute(ImageUrl);

        }
    }


    private void adapterSetup() {

        ViewPager ultraViewPager = findViewById(R.id.ultraViewPager);
        ////ultraViewPager.setScrollMode(UltraViewPager.ScrollMode.VERTICAL);
        //initialize UltraPagerAdapter，and add child view to UltraViewPager
        PagerAdapter adapter = new UltraPagerAdapter(false, imagesUrlLis, Book_Activity.this, urlPosition);
        ultraViewPager.setAdapter(adapter);
        ultraViewPager.setCurrentItem(urlPosition);


        //initialize built-in indicator
        ////ultraViewPager.initIndicator();
        //set style of indicators
        //// ultraViewPager.getIndicator()
        //.setOrientation(UltraViewPager.Orientation.VERTICAL)
        //.setFocusColor(Color.WHITE)
        //.setNormalColor(R.color.IndicatorsNormal)
        // .setRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        //set the alignment
        ////ultraViewPager.getIndicator().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.START);
        //construct built-in indicator, and add it to  UltraViewPager
        ////ultraViewPager.getIndicator().build();

        //set an infinite loop
        ////ultraViewPager.setInfiniteLoop(true);
        //enable auto-scroll mode
        //ultraViewPager.setAutoScroll(2500);
    }

    public static void showSnackbar(String msg){
        Snackbar snackbar=Snackbar.make(coordinatorLayout,msg,Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    View.OnClickListener downloadBtnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    View.OnClickListener setWallClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
}

class DownloadsImage extends AsyncTask<String, Void, Void> {

    Context context;

    public DownloadsImage(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... strings) {
        URL url = null;
        try {
            url = new URL(strings[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Create Path to save Image
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/NF"); //Creates app specific folder

        if (!path.exists()) {
            path.mkdirs();
        }

        File imageFile = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg"); // Imagename.png
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
            out.flush();
            out.close();
            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(context, new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    // Log.i("ExternalStorage", "Scanned " + path + ":");
                    //    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void imageFile) {
        super.onPostExecute(imageFile);

       // Toast.makeText(context, "Image Saved to Gallery ..", Toast.LENGTH_LONG).show();
        showSnackbar(" Image Saved to Gallery");



    }


}


