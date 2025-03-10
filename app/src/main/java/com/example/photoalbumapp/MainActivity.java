package com.example.photoalbumapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private List<Photo> photoList = new ArrayList<>();
    private List<Photo> originalPhotoList = new ArrayList<>(); // Store unfiltered list
    private AppDatabase appDatabase;
    private EditText searchEditText;
    private int currentPage = 1;
    private boolean isLoading = false;
    private static final int PAGE_SIZE = 30;  // Ensures consistent pagination

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        searchEditText = findViewById(R.id.searchEditText);
        FloatingActionButton fabLocalStorage = findViewById(R.id.fabLocalStorage);

        appDatabase = AppDatabase.getInstance(this);
        photoAdapter = new PhotoAdapter(this, photoList, appDatabase);
        recyclerView.setAdapter(photoAdapter);

        // Load cached photos or fetch from API
        loadCachedPhotos();

        // Enable pagination when scrolling
        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    loadMorePhotos();  // Load next batch when reaching the bottom
                }
            }
        });

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterPhotos(s.toString().trim());
            }
        });

        // Open Local Storage Activity
        fabLocalStorage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LikedPhotosActivity.class);
            startActivity(intent);
        });
    }

    private void loadCachedPhotos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Photo> cachedPhotos = appDatabase.photoDao().getAllPhotos();
            runOnUiThread(() -> {
                if (cachedPhotos != null && !cachedPhotos.isEmpty()) {
                    photoList.clear();  // Prevent duplication
                    photoList.addAll(cachedPhotos);
                    originalPhotoList.clear();
                    originalPhotoList.addAll(cachedPhotos);
                    photoAdapter.notifyDataSetChanged();
                } else {
                    loadPhotosFromAPI(currentPage);
                }
            });
        });
    }

    private void loadMorePhotos() {
        if (isLoading) return;
        isLoading = true;

        Log.d("Pagination", "Loading more photos... Page: " + currentPage);
        loadPhotosFromAPI(currentPage);
    }

    private void loadPhotosFromAPI(int page) {
        PhotoRepository photoRepository = new PhotoRepository();
        photoRepository.fetchPhotos(page, PAGE_SIZE, new PhotoRepository.PhotoFetchCallback() {
            @Override
            public void onSuccess(List<Photo> photos) {
                runOnUiThread(() -> {
                    if (!photos.isEmpty()) {
                        Log.d("API Response", "Received " + photos.size() + " photos.");
                        photoList.addAll(photos);
                        originalPhotoList.addAll(photos);
                        photoAdapter.notifyDataSetChanged();
                        currentPage++;  // Increment only if successful
                    } else {
                        Log.d("API Response", "No more photos to load.");
                    }
                    isLoading = false;
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("API Error", errorMessage);
                isLoading = false;
            }
        });
    }

    private void filterPhotos(String query) {
        if (query.isEmpty()) {
            photoAdapter.updateList(new ArrayList<>(originalPhotoList)); // Reset to full list
            return;
        }

        List<Photo> filteredList = new ArrayList<>();
        for (Photo photo : originalPhotoList) {
            if ((photo.getAuthor() != null && photo.getAuthor().toLowerCase().contains(query.toLowerCase())) ||
                    (photo.getApiId() != null && photo.getApiId().contains(query))) {
                filteredList.add(photo);
            }
        }

        Log.d("Search", "Query: " + query + " | Results Found: " + filteredList.size());
        photoAdapter.updateList(filteredList);
    }
}