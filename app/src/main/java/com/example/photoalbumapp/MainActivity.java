package com.example.photoalbumapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private List<Photo> originalPhotoList = new ArrayList<>();
    private AppDatabase appDatabase;
    private EditText searchEditText;
    private ProgressBar progressBarMain, progressBarBottom;
    private LinearLayout errorLayout;
    private TextView errorText;
    private Button retryButton;
    private int currentPage = 1;
    private boolean isLoading = false;
    private static final int PAGE_SIZE = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        FloatingActionButton fabLocalStorage = findViewById(R.id.fabLocalStorage);
        progressBarMain = findViewById(R.id.progressBarMain);
        progressBarBottom = findViewById(R.id.progressBarBottom);
        errorLayout = findViewById(R.id.errorLayout);
        errorText = findViewById(R.id.errorText);
        retryButton = findViewById(R.id.retryButton);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        appDatabase = AppDatabase.getInstance(this);
        photoAdapter = new PhotoAdapter(this, photoList, appDatabase, true);
        recyclerView.setAdapter(photoAdapter);

        // Load cached photos or fetch from API
        loadCachedPhotos();

        // Retry Button Click - Reloads data
        retryButton.setOnClickListener(v -> {
            errorLayout.setVisibility(View.GONE);
            progressBarMain.setVisibility(View.VISIBLE);
            loadPhotosFromAPI(currentPage);
        });

        // Enable pagination when scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    loadMorePhotos();
                }
            }
        });

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
        progressBarMain.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Photo> cachedPhotos = appDatabase.photoDao().getAllPhotos();
            runOnUiThread(() -> {
                progressBarMain.setVisibility(View.GONE);
                if (cachedPhotos != null && !cachedPhotos.isEmpty()) {
                    photoList.clear();
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
        progressBarBottom.setVisibility(View.VISIBLE);
        loadPhotosFromAPI(currentPage);
    }

    private void loadPhotosFromAPI(int page) {
        PhotoRepository photoRepository = new PhotoRepository();
        photoRepository.fetchPhotos(page, PAGE_SIZE, new PhotoRepository.PhotoFetchCallback() {
            @Override
            public void onSuccess(List<Photo> photos) {
                runOnUiThread(() -> {
                    progressBarMain.setVisibility(View.GONE);
                    progressBarBottom.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.GONE);

                    if (!photos.isEmpty()) {
                        photoList.addAll(photos);
                        originalPhotoList.addAll(photos);
                        photoAdapter.notifyDataSetChanged();
                        currentPage++;
                    }
                    isLoading = false;
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    progressBarMain.setVisibility(View.GONE);
                    progressBarBottom.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                    errorText.setText("Network Error: " + errorMessage);
                });
                Log.e("API Error", errorMessage);
                isLoading = false;
            }
        });
    }

    private void filterPhotos(String query) {
        if (query.isEmpty()) {
            photoAdapter.updateList(new ArrayList<>(originalPhotoList));
            return;
        }

        List<Photo> filteredList = new ArrayList<>();
        for (Photo photo : originalPhotoList) {
            if ((photo.getAuthor() != null && photo.getAuthor().toLowerCase().contains(query.toLowerCase())) ||
                    (photo.getApiId() != null && photo.getApiId().contains(query))) {
                filteredList.add(photo);
            }
        }

        photoAdapter.updateList(filteredList);
    }
}
