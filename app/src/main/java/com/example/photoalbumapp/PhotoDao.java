package com.example.photoalbumapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PhotoDao {

    @Insert
    void insert(Photo photo);

    @Update
    void update(Photo photo);

    @Query("SELECT * FROM photos WHERE isLiked = 1")
    List<Photo> getFavoritePhotos();

    @Query("SELECT * FROM photos")
    List<Photo> getAllPhotos();

    // ✅ Fix: Correct type for photoId (String, not int)
    @Query("SELECT * FROM photos WHERE ApiId = :photoId LIMIT 1")
    Photo getPhotoById(String photoId);  // Use String if photoId is a string

    @Query("DELETE FROM photos WHERE ApiId = :photoId")
    void deletePhotoById(String photoId);  // ✅ Add this method to fix the error

    @Query("DELETE FROM photos")
    void deleteAllPhotos();
}
