package com.obcomdeveloper.realmusic.Wallpapers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Utils.TouchImageView;
import com.obcomdeveloper.realmusic.Utils.UniversalImageLoader;

public class ImageClickActivity extends AppCompatActivity {
    private static final String TAG = "ImageClickActivity";

    private Context mContext=ImageClickActivity.this;

    private TouchImageView fullscreen_iv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.image_click_fullscreen);
        fullscreen_iv=findViewById(R.id.image_click_fullscreen);

        initImageLoader();

        Intent intent=getIntent();
        String url=intent.getStringExtra("clickedurl");
        UniversalImageLoader.setImage(url,fullscreen_iv,null,"");
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }
}
