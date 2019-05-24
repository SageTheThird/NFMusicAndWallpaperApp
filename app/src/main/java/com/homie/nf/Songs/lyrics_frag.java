package com.homie.nf.Songs;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.homie.nf.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link lyrics_frag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link lyrics_frag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class lyrics_frag extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String lyrics_file = "param1";


    // TODO: Rename and change types of parameters
    private String url;


    private OnFragmentInteractionListener mListener;

    private NestedScrollView nestedScrollView;
    private TextView lyrics_textView;
    String lyrics_text="";
    private Context mContext;


    public lyrics_frag() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static lyrics_frag newInstance(String param1) {
        lyrics_frag fragment = new lyrics_frag();
        Bundle args = new Bundle();
        args.putString(lyrics_file, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            url = getArguments().getString(lyrics_file);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout. fragment_lyrics_frag, container, false);
        //genius_back_btn=view.findViewById(R.id.genius_back);
        nestedScrollView=view.findViewById(R.id.nestScrollView);
        lyrics_textView=view.findViewById(R.id.lyricstextView);


       /* try {

            InputStream inputStream = getActivity().getAssets().open(lyrics_file);
            int size = inputStream.available();
            byte[] buffer = new byte[size];

            inputStream.read(buffer);
            inputStream.close();
            lyrics_text = new String(buffer);


        } catch (IOException ex) {

            ex.printStackTrace();
        }*/
        lyrics_textView.setText(R.string.lyrics);




        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext=context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
