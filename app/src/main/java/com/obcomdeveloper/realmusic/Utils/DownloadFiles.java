package com.obcomdeveloper.realmusic.Utils;

import android.app.AlertDialog;
import android.app.DownloadManager;

import android.content.Context;
import android.net.Uri;
import android.os.Build;


import java.io.File;


public class DownloadFiles {

    private Context context;
    private String fileExtension;
    private String destinationDirectory;
    private String fileName;
    private AlertDialog dialog;



    public DownloadFiles(Context mContext, String fileExtension, String downloadDirectory,
                         String fileNameInto,  android.app.AlertDialog dialog) {
        this.context = mContext;
        this.fileExtension = fileExtension;
        this.destinationDirectory = downloadDirectory;
        this.fileName = fileNameInto;
        this.dialog=dialog;
    }


    public long downloadingFiles(String url) {
        if(dialog != null){
            dialog.show();
        }

        long mDownloadId = 0;

        DownloadManager downloadManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            downloadManager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
        }
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            request = new DownloadManager.Request(uri);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);


        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            request.setDestinationUri(Uri.fromFile(new File(destinationDirectory, fileName + fileExtension)));
        }

        if (downloadManager != null) {
            mDownloadId = downloadManager.enqueue(request);
        }

        return mDownloadId;


    }

}