package com.example.photoalbumapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "photos")
public class Photo {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String photoId;         // Unique ID from API
    private String author;
    private String downloadUrl;
    private boolean isLiked = false;  // Track like status

    // Constructor
    public Photo(String photoId, String author, String downloadUrl) {
        this.photoId = photoId;
        this.author = author;
        this.downloadUrl = downloadUrl;
    }

    // Toggle like status
    public boolean toggleLike() {
        isLiked = !isLiked;
        return isLiked;
    }

    // Getters
    public int getId() { return id; }
    public String getPhotoId() { return photoId; }
    public String getAuthor() { return author; }
    public String getDownloadUrl() { return downloadUrl; }
    public boolean isLiked() { return isLiked; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setLiked(boolean liked) { isLiked = liked; }
}
