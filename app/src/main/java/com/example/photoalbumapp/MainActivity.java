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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.searchEditText);
        FloatingActionButton fabLocalStorage = findViewById(R.id.fabLocalStorage);

        appDatabase = AppDatabase.getInstance(this);
        photoAdapter = new PhotoAdapter(this, photoList, appDatabase);
        recyclerView.setAdapter(photoAdapter);

        // Load cached photos or fetch from API
        loadCachedPhotos();

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

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
                    photoList.addAll(cachedPhotos);
                    originalPhotoList.addAll(cachedPhotos); // Store original list
                    photoAdapter.notifyDataSetChanged();
                } else {
                    loadPhotosFromAPI(1);
                }
            });
        });
    }

    private void loadPhotosFromAPI(int page) {
        PhotoRepository photoRepository = new PhotoRepository();
        photoRepository.fetchPhotos(page, 20, new PhotoRepository.PhotoFetchCallback() {
            @Override
            public void onSuccess(List<Photo> photos) {
                for (Photo photo : photos) {
                    Log.d("API Response", "Photo ID: " + photo.getPhotoId() + " URL: " + photo.getDownloadUrl());
                }
                photoList.addAll(photos);
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("API Error", errorMessage);
            }
        });
    }

    private void filterPhotos(String query) {
        if (query.isEmpty()) {
            photoAdapter.updateList(originalPhotoList); // Reset to full list
            return;
        }

        List<Photo> filteredList = new ArrayList<>();
        for (Photo photo : originalPhotoList) {
            if (photo.getAuthor().toLowerCase().contains(query.toLowerCase()) ||
                    photo.getPhotoId().contains(query)) {
                filteredList.add(photo);
            }
        }
        photoAdapter.updateList(filteredList);
    }
}
