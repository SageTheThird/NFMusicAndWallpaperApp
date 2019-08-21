package com.obcomdeveloper.realmusic.room;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Songs")
public class SongDB implements Parcelable {

    @NonNull
    @ColumnInfo(name = "song_id")
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "song_name")
    private String song_name;

    @ColumnInfo(name = "thumbnail")
    private int thumbnail;

    @ColumnInfo(name = "artist_name")
    private String artist_name;

    protected SongDB(Parcel in) {
        id = in.readLong();
        song_name = in.readString();
        thumbnail = in.readInt();
        artist_name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(song_name);
        dest.writeInt(thumbnail);
        dest.writeString(artist_name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SongDB> CREATOR = new Creator<SongDB>() {
        @Override
        public SongDB createFromParcel(Parcel in) {
            return new SongDB(in);
        }

        @Override
        public SongDB[] newArray(int size) {
            return new SongDB[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public SongDB() {
    }

    public SongDB(long id, String song_name, int thumbnail, String artist_name) {
        this.id = id;
        this.song_name = song_name;
        this.thumbnail = thumbnail;
        this.artist_name = artist_name;
    }
}