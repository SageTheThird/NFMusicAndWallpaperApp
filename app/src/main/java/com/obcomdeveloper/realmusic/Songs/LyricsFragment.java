package com.obcomdeveloper.realmusic.Songs;

import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.obcomdeveloper.realmusic.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class LyricsFragment extends Fragment {
    private static final String TAG = "LyricsFragment";

    private NestedScrollView nestedScrollView;
    private TextView lyrics_textView;
    String lyrics_text="";
    private String lyrics_directory;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout. fragment_lyrics_frag, container, false);
        //genius_back_btn=view.findViewById(R.id.genius_back);
        nestedScrollView=view.findViewById(R.id.nestScrollView);
        lyrics_textView=view.findViewById(R.id.lyricstextView);
        lyrics_directory= Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + getActivity().getPackageName() + "/files/Lyrics/";


        Bundle bundle = null;
        try
        {
            bundle=getArguments();
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException"+e.getMessage() );
        }


        String lyrics_file_name=bundle.getString(getString(R.string.lyrics_file_name));

        Log.d(TAG, "onCreateView: lyrics_file_name "+lyrics_file_name);

            String lyrics_path=lyrics_directory+ lyrics_file_name;

            File file = new File(lyrics_path.toString());

            if(!file.exists()){
                try {
                    InputStream inputStream = getActivity().getAssets().open(lyrics_file_name);
                    int size = inputStream.available();
                    byte[] buffer = new byte[size];

                    inputStream.read(buffer);
                    inputStream.close();
                    lyrics_text = new String(buffer);
                    Log.d(TAG, "onCreateView: lyrics_text : "+lyrics_text);

                    lyrics_textView.setText(lyrics_text);

                } catch (IOException ex) {

                    ex.printStackTrace();
                }catch(NullPointerException e){
                    Log.d(TAG, "onCreateView: NullPointerException "+e.getMessage());
                }


            }else {
                try {
                    FileInputStream inputStream=new FileInputStream(file);
                    int size = inputStream.available();
                    byte[] buffer = new byte[size];

                    inputStream.read(buffer);
                    inputStream.close();
                    lyrics_text = new String(buffer);
                    lyrics_textView.setText(lyrics_text);


                } catch (IOException ex) {

                    ex.printStackTrace();
                }catch(NullPointerException e){

                    e.printStackTrace();
                }

            }

            //lyrics_textView.setText(R.string.lyrics);




        return view;
    }


}
