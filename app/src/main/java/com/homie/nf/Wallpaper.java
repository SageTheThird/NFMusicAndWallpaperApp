package com.homie.nf;

public class Wallpaper {

    private String downloadUrl;
    private int priority;

    public Wallpaper() {

    }

    public Wallpaper(String downloadUrl, int priority) {
        this.downloadUrl=downloadUrl;
        this.priority=priority;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public int getPriority() {
        return priority;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
