package com.obcomdeveloper.realmusic.Models;

public class Wallpaper {

    private String download_url;
    private String id;

    public Wallpaper() {

    }

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
}
