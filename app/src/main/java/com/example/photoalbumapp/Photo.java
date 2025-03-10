package com.example.photoalbumapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "photos")
public class Photo {

    @PrimaryKey(autoGenerate = true)
    @Expose(serialize = false, deserialize = false)  // 🚀 Ensures Room's ID is ignored by Gson
    private int localId;  // Local database ID (does not conflict with API)

    @SerializedName("id")  // ✅ Correctly maps API JSON "id"
    private String apiId;

    @SerializedName("author")
    private String author;

    @SerializedName("download_url")
    private String downloadUrl;

    private boolean isLiked = false;

    // Constructor
    public Photo(String apiId, String author, String downloadUrl) {
        this.apiId = apiId;
        this.author = author;
        this.downloadUrl = downloadUrl;
    }

    // Toggle like status
    public boolean toggleLike() {
        isLiked = !isLiked;
        return isLiked;
    }

    // Getters
    public int getLocalId() { return localId; }  // No longer mapped to JSON
    public String getApiId() { return apiId; }  // API ID correctly mapped
    public String getAuthor() { return author; }
    public String getDownloadUrl() { return downloadUrl; }
    public boolean isLiked() { return isLiked; }

    // Setters
    public void setLocalId(int localId) { this.localId = localId; }
    public void setLiked(boolean liked) { isLiked = liked; }
}
