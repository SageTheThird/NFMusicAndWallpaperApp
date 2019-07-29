package com.obcomdeveloper.realmusic.Models;


import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {


    private String id;
    private String song_name;
    private String download_url;
    private String genius_url;
    private String thumbnail;
    private String quote;
    private String artist_name;
    private String lyrics_url;

    public Song() {
    }

    protected Song(Parcel in) {
        id = in.readString();
        song_name = in.readString();
        download_url = in.readString();
        genius_url = in.readString();
        thumbnail = in.readString();
        quote = in.readString();
        artist_name = in.readString();
        lyrics_url = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public String toString() {
        return "Song{" +
                "id='" + id + '\'' +
                ", song_name='" + song_name + '\'' +
                ", download_url='" + download_url + '\'' +
                ", genius_url='" + genius_url + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", quote='" + quote + '\'' +
                ", artist_name='" + artist_name + '\'' +
                ", lyrics_url='" + lyrics_url + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getLyrics_url() {
        return lyrics_url;
    }

    public void setLyrics_url(String lyrics_url) {
        this.lyrics_url = lyrics_url;
    }

    public Song(String id, String song_name, String download_url,
                String genius_url, String thumbnail, String quote, String artist_name, String lyrics_url) {
        this.id = id;
        this.song_name = song_name;
        this.download_url = download_url;
        this.genius_url = genius_url;
        this.thumbnail = thumbnail;
        this.quote = quote;
        this.artist_name = artist_name;
        this.lyrics_url = lyrics_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(song_name);
        dest.writeString(download_url);
        dest.writeString(genius_url);
        dest.writeString(thumbnail);
        dest.writeString(quote);
        dest.writeString(artist_name);
        dest.writeString(lyrics_url);
    }
}
