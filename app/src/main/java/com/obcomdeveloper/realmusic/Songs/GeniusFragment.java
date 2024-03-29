package com.obcomdeveloper.realmusic.Songs;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.obcomdeveloper.realmusic.R;

public class GeniusFragment extends Fragment {
    private static final String TAG = "GeniusFragment";
    private WebView geniusWebView;
    private String genius_url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_genius_fragment, container, false);
        geniusWebView = view.findViewById(R.id.geniusWebView);

        Bundle bundle = null;
        try
        {
            bundle=getArguments();
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException"+e.getMessage() );
        }

        genius_url=bundle.getString(getString(R.string.genius_url));

        geniusWebView.getSettings().setJavaScriptEnabled(true);
        //geniusWebView.setWebViewClient(new WebViewClient());

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading Data...");

        geniusWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {

                if (progress < 100) {
                    progressDialog.show();
                }
                if (progress == 100) {
                    progressDialog.dismiss();
                }
            }
        });
        geniusWebView.loadUrl(genius_url);
        progressDialog.dismiss();


        return view;
    }


}