package com.obcomdeveloper.realmusic.Models;

public class Quote {


    private String array;

    public Quote() {
    }

    public Quote(String array) {
        this.array = array;
    }

    public String getArray() {
        return array;
    }

    public void setArray(String array) {
        this.array = array;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "array='" + array + '\'' +
                '}';
    }
}
