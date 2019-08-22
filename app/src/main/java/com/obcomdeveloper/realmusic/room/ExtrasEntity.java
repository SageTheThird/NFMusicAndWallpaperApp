package com.obcomdeveloper.realmusic.room;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Extras")
public class ExtrasEntity implements Parcelable {

    @NonNull
    @ColumnInfo(name = "song_id")
    @PrimaryKey(autoGenerate = true)
    private long song_id;

    @ColumnInfo(name = "song_name")
    private String song_name;

    @ColumnInfo(name = "thumbnail")
    private int thumbnail;

    @ColumnInfo(name = "artist_name")
    private String artist_name;

    @ColumnInfo(name = "download_url")
    private String download_url;

    @ColumnInfo(name = "genius_url")
    private String genius_url;

    @ColumnInfo(name = "lyrics_url")
    private String lyrics_url;

    public ExtrasEntity(long song_id, String song_name, int thumbnail, String artist_name, String download_url, String genius_url, String lyrics_url) {
        this.song_id = song_id;
        this.song_name = song_name;
        this.thumbnail = thumbnail;
        this.artist_name = artist_name;
        this.download_url = download_url;
        this.genius_url = genius_url;
        this.lyrics_url = lyrics_url;
    }

    public ExtrasEntity() {
    }

    protected ExtrasEntity(Parcel in) {
        song_id = in.readLong();
        song_name = in.readString();
        thumbnail = in.readInt();
        artist_name = in.readString();
        download_url = in.readString();
        genius_url = in.readString();
        lyrics_url = in.readString();
    }

    public static final Creator<ExtrasEntity> CREATOR = new Creator<ExtrasEntity>() {
        @Override
        public ExtrasEntity createFromParcel(Parcel in) {
            return new ExtrasEntity(in);
        }

        @Override
        public ExtrasEntity[] newArray(int size) {
            return new ExtrasEntity[size];
        }
    };

    public long getSong_id() {
        return song_id;
    }

    public void setSong_id(long song_id) {
        this.song_id = song_id;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getGenius_url() {
        return genius_url;
    }

    public void setGenius_url(String genius_url) {
        this.genius_url = genius_url;
    }

    public String getLyrics_url() {
        return lyrics_url;
    }

    public void setLyrics_url(String lyrics_url) {
        this.lyrics_url = lyrics_url;
    }

    @Override
    public String toString() {
        return "ExtrasEntity{" +
                "song_id=" + song_id +
                ", song_name='" + song_name + '\'' +
                ", thumbnail=" + thumbnail +
                ", artist_name='" + artist_name + '\'' +
                ", download_url='" + download_url + '\'' +
                ", genius_url='" + genius_url + '\'' +
                ", lyrics_url='" + lyrics_url + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(song_id);
        dest.writeString(song_name);
        dest.writeInt(thumbnail);
        dest.writeString(artist_name);
        dest.writeString(download_url);
        dest.writeString(genius_url);
        dest.writeString(lyrics_url);
    }
}