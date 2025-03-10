package com.example.photoalbumapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private List<Photo> photoList = new ArrayList<>();
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final String BASE_URL = "https://picsum.photos/";
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        photoAdapter = new PhotoAdapter(this, photoList);
        recyclerView.setAdapter(photoAdapter);

        appDatabase = AppDatabase.getInstance(this);

        loadCachedPhotos();  // Load cached photos if available

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1) && !isLoading && !isLastPage) {
                    loadPhotosFromAPI(++currentPage);
                }
            }
        });
    }

    // 🟢 Step 1: Load cached photos from Room using Executors
    private void loadCachedPhotos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Photo> cachedPhotos = appDatabase.photoDao().getAllPhotos();
            runOnUiThread(() -> {
                if (cachedPhotos != null && !cachedPhotos.isEmpty()) {
                    photoList.addAll(cachedPhotos);
                    photoAdapter.notifyDataSetChanged();
                } else {
                    loadPhotosFromAPI(currentPage);  // Fetch from API if no cache
                }
            });
        });
    }

    // 🟢 Step 2: Fetch photos from API and cache them
    private void loadPhotosFromAPI(int page) {
        isLoading = true;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getPhotos(page, 20).enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Photo> newPhotos = response.body();
                    photoList.addAll(newPhotos);
                    photoAdapter.notifyDataSetChanged();
                    savePhotosToCache(newPhotos);  // Save to cache
                    if (newPhotos.size() < 20) {
                        isLastPage = true;  // No more pages
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No more photos available", Toast.LENGTH_SHORT).show();
                    isLastPage = true;
                }
                isLoading = false;
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                isLoading = false;
                Toast.makeText(MainActivity.this, "Failed to load photos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🟢 Step 3: Save fetched photos to Room database using Executors
    private void savePhotosToCache(List<Photo> photos) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (Photo photo : photos) {
                if (appDatabase.photoDao().getPhotoById(photo.getPhotoId()) == null) {  // Prevent duplicates
                    appDatabase.photoDao().insert(photo);
                }
            }
        });
    }
}
