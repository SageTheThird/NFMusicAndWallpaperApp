package com.homie.nf.Songs;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.homie.nf.R;

import java.io.IOException;
import java.io.InputStream;


public class lyrics_frag extends Fragment {
    private static final String TAG = "lyrics_frag";
    private String url;

    private NestedScrollView nestedScrollView;
    private TextView lyrics_textView;
    String lyrics_text="";
    private Context mContext=getActivity();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout. fragment_lyrics_frag, container, false);
        //genius_back_btn=view.findViewById(R.id.genius_back);
        nestedScrollView=view.findViewById(R.id.nestScrollView);
        lyrics_textView=view.findViewById(R.id.lyricstextView);


        Bundle bundle = null;
        try
        {
            bundle=getArguments();
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException"+e.getMessage() );
        }

        String lyrics_file_name=bundle.getString(getString(R.string.lyrics_file_name));
        try {

            Log.d(TAG, "onCreateView: URL: "+lyrics_file_name);
            InputStream inputStream = getActivity().getAssets().open(lyrics_file_name);
            int size = inputStream.available();
            byte[] buffer = new byte[size];

            inputStream.read(buffer);
            inputStream.close();
            lyrics_text = new String(buffer);
            Log.d(TAG, "onCreateView: lyrics_text : "+lyrics_text);


        } catch (IOException ex) {

            ex.printStackTrace();
        }
        //lyrics_textView.setText(R.string.lyrics);

        lyrics_textView.setText(lyrics_text);



        return view;
    }


}
