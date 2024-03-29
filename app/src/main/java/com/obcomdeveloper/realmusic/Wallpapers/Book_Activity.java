package com.obcomdeveloper.realmusic.Wallpapers;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.obcomdeveloper.realmusic.Adapters.UltraPagerAdapter;
import com.obcomdeveloper.realmusic.Models.Wallpaper;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Utils.Ads;
import com.obcomdeveloper.realmusic.Utils.UniversalImageLoader;
import com.obcomdeveloper.realmusic.Utils.ZoomOutTransformation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import needle.Needle;


import static com.obcomdeveloper.realmusic.Wallpapers.Book_Activity.showSnackbar;


public class Book_Activity extends AppCompatActivity {
    private static final String TAG = "Book_Activity";

    // android:centerColor="#203A43"
    private List<Wallpaper> full_walls_list;
    private Button back_viewPager;
    private FloatingActionButton downloadBtn, setBtn;
    private int urlPosition;

    private Context mContext = Book_Activity.this;

    private static CoordinatorLayout coordinatorLayout;
    private ViewPager viewpager;

    private List<Wallpaper> small_walls_list;

    //ads
    private Ads ads;
    private AdView adView;
    private InterstitialAd interstitialAd;

    private ImageView blurred_iv;
    private int timesClicked_download;
    private int timesClicked_set;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_wallpaper);

        //methods
        initWidgets();
        initImageLoader();
        getIncomingIntent();
        adapterSetup();
        adsSetup();

        //listeners
        downloadBtn.setOnClickListener(downloadBtnClickListener);
        setBtn.setOnClickListener(setWallClickListener);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("setCountToZero_timesClicked", true)) {
            timesClicked_download =0;
            timesClicked_set=0;
            saveIntPref(getString(R.string.timesClicked), timesClicked_download,this);
            saveIntPref(getString(R.string.timesClicked_set), timesClicked_set,this);
            prefs.edit().putBoolean("setCountToZero_timesClicked", false).commit();
        }
    }

    private void adsSetup(){
        //ads
        ads=new Ads();
        ads.initAdMob(this);
        ads.setupBanner(adView);
        ads.setupInterstitial(this,getString(R.string.interstitial_ad_test_unit_id),interstitialAd);

    }

    private void initWidgets() {
        back_viewPager = findViewById(R.id.tool_back_viewPager);
        downloadBtn = findViewById(R.id.actionBtn1);
        setBtn = findViewById(R.id.actionBtn2);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        blurred_iv = findViewById(R.id.blurImageView);
        small_walls_list=new ArrayList<>();

        adView =findViewById(R.id.adView);
        interstitialAd=new InterstitialAd(this);
        full_walls_list= new ArrayList<>();


    }
    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSplit(mContext);
    }

    private void getIncomingIntent() {
        Intent intent = getIntent();
        full_walls_list = intent.getParcelableArrayListExtra(getString(R.string.full_walls_ist));
        urlPosition = intent.getIntExtra(getString(R.string.position), 5);
        small_walls_list=intent.getParcelableArrayListExtra(getString(R.string.small_walls_list));

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

        viewpager = findViewById(R.id.ultraViewPager);
        PagerAdapter adapter = new UltraPagerAdapter( full_walls_list, Book_Activity.this,
                                                      urlPosition,small_walls_list);
        viewpager.setAdapter(adapter);
        viewpager.setCurrentItem(urlPosition);



        UniversalImageLoader.setBlurredImage(small_walls_list.get(urlPosition).getDownload_url(),blurred_iv,
                null,"",null,null,null);





        ZoomOutTransformation zoomOutTransformation=new ZoomOutTransformation();
        viewpager.setPageTransformer(true,zoomOutTransformation);

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int i, float v, int i1) {


                Log.d(TAG, "onPageScrolled: called");
            }

            @Override
            public void onPageSelected(int i) {

                if (i%4==0 && i>0){
                    //Toast.makeText(mContext, "4th Position Reached : ", Toast.LENGTH_LONG).show();

                }

                UniversalImageLoader.setBlurredImage(small_walls_list.get(i).getDownload_url(),blurred_iv,
                        null,"",null,null,null);

                Log.d(TAG, "onPageSelected: called");
            }

            @Override
            public void onPageScrollStateChanged(int i) {

                Log.d(TAG, "onPageScrollStateChanged: called");

            }
        });

    }

    public static void showSnackbar(String msg) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    View.OnClickListener downloadBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            showToast("Downloading Image");

            loadAdIfDownloadClickedTimes(3);

            DownloadImage(full_walls_list.get(viewpager.getCurrentItem()).getDownload_url());

        }
    };

    View.OnClickListener setWallClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            showToast("Setting Wallpaper");

            loadAdIfSetClickedTimes(3);

            setImageAsBackground(full_walls_list.get(viewpager.getCurrentItem()).getDownload_url());
        }
    };

    private void loadAdIfDownloadClickedTimes(int times){

        if(getIntPref(getString(R.string.timesClicked),mContext)==times){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ads.loadinterstitial(interstitialAd);
                }
            });

            timesClicked_download=0;
            saveIntPref(getString(R.string.timesClicked), timesClicked_download,mContext);
        }

        int temp=getIntPref(getString(R.string.timesClicked),mContext);
        temp++;
        Log.d(TAG, "loadAdIfDownloadClickedTimes: timesClicked_download : "+temp);
        saveIntPref(getString(R.string.timesClicked),temp,mContext);

    }
    private void loadAdIfSetClickedTimes(int times){

        if(getIntPref(getString(R.string.timesClicked_set),mContext)==times){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ads.loadinterstitial(interstitialAd);
                }
            });

            timesClicked_set=0;
            saveIntPref(getString(R.string.timesClicked_set), timesClicked_set,mContext);
        }
        int temp=getIntPref(getString(R.string.timesClicked_set),mContext);
        temp++;
        saveIntPref(getString(R.string.timesClicked_set),temp,mContext);


    }
    public void setImageAsBackground(final String imageUrl) {

        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                Bitmap myBitmap = null;
                final WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(Book_Activity.this);
                try {

                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    myBitmap = BitmapFactory.decodeStream(input);
                    myWallpaperManager.setBitmap(myBitmap);
                    showSnackbar("Done!");
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        });



    }

    public static void saveIntPref(String key, int index, Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, index);
        editor.commit();
    }
    public static int getIntPref(String key, Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int myIntValue = prefs.getInt(key, -1);
        return myIntValue;
    }



    public Bitmap getBitmapFromURL(final String src) {
        final Bitmap[] bm = {null};
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                // something cpu-intensive and/or not UI-related
                URL url = null;
                try {
                    url = new URL(src);
                    bm[0] = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        return bm[0];
    }
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


