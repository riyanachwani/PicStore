package com.example.photoalbumapp;

public class Photo {
    private String id;
    private String author;
    private String download_url;

    // Constructor
    public Photo(String id, String author, String download_url) {
        this.id = id;
        this.author = author;
        this.download_url = download_url;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getDownloadUrl() {
        return download_url;
    }
}
