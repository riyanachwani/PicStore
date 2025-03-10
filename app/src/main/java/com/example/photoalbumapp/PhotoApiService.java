package com.example.photoalbumapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PhotoApiService {
    @GET("v2/list")
    Call<List<Photo>> getPhotos(@Query("page") int page, @Query("limit") int limit);
}
