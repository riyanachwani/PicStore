package com.example.photoalbumapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoRepository {

    private PhotoApiService apiService;

    // Constructor to initialize the API service
    public PhotoRepository() {
        apiService = ApiClient.getApiService();
    }

    // Method to fetch photos from the API
    public void fetchPhotos(int page, int limit, PhotoFetchCallback callback) {
        Call<List<Photo>> call = apiService.getPhotos(page, limit);
        call.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());  // Pass the data to the UI if successful
                } else {
                    callback.onFailure("Failed to load photos!");
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());  // Handle API errors
            }
        });
    }

    // Interface to communicate API results back to the UI
    public interface PhotoFetchCallback {
        void onSuccess(List<Photo> photos);
        void onFailure(String errorMessage);
    }
}
