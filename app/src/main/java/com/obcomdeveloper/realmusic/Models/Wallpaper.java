package com.obcomdeveloper.realmusic.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Wallpaper implements Parcelable {

    private String download_url;
    private String id;

    public Wallpaper() {

    }

    protected Wallpaper(Parcel in) {
        download_url = in.readString();
        id = in.readString();
    }

    public static final Creator<Wallpaper> CREATOR = new Creator<Wallpaper>() {
        @Override
        public Wallpaper createFromParcel(Parcel in) {
            return new Wallpaper(in);
        }

        @Override
        public Wallpaper[] newArray(int size) {
            return new Wallpaper[size];
        }
    };

    @Override
    public String toString() {
        return "Wallpaper{" +
                "download_url='" + download_url + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    public Wallpaper(String download_url, String id) {
        this.download_url = download_url;
        this.id = id;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(download_url);
        dest.writeString(id);
    }
}
