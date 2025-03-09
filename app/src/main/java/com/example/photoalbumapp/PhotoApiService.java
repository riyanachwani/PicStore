package com.example.photoalbumapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PhotoApiService {

    // Define an API call to get a list of photos with pagination support
    @GET("/v2/list")
    Call<List<Photo>> getPhotos(
            @Query("page") int page,
            @Query("limit") int limit
    );
}
