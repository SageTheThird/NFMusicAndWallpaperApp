package com.homie.nf.Models;

public class Song {


    private String id;
    private String song_name;
    private String download_url;
    private String genius_url;

    public Song(String id, String song_name, String download_url, String genius_url) {
        this.id = id;
        this.song_name = song_name;
        this.download_url = download_url;
        this.genius_url = genius_url;
    }

    public Song() {
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

    @Override
    public String toString() {
        return "Song{" +
                "id='" + id + '\'' +
                ", song_name='" + song_name + '\'' +
                ", download_url='" + download_url + '\'' +
                ", genius_url='" + genius_url + '\'' +
                '}';
    }
}
