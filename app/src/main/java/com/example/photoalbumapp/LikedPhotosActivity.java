package com.example.photoalbumapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class LikedPhotosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private AppDatabase appDatabase;
    private List<Photo> likedPhotos = new ArrayList<>();
    private ProgressBar progressBarMain;
    private TextView noLikedPhotosText; // ✅ TextView to show when no photos are left

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_photos);

        recyclerView = findViewById(R.id.recyclerView);
        progressBarMain = findViewById(R.id.progressBarMain);
        noLikedPhotosText = findViewById(R.id.noLikedPhotosText); // ✅ Reference to the TextView

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        appDatabase = AppDatabase.getInstance(this);
        photoAdapter = new PhotoAdapter(this, likedPhotos, appDatabase, true); // ✅ Pass 'true' for local storage mode
        recyclerView.setAdapter(photoAdapter);

        loadLikedPhotos();

        // ✅ Prevent jumping to MainActivity when pressing Back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // ✅ Simply close the activity, don't jump to MainActivity
            }
        });
    }

    private void loadLikedPhotos() {
        progressBarMain.setVisibility(View.VISIBLE); // ✅ Show progress bar while loading
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Photo> photos = appDatabase.photoDao().getFavoritePhotos();
            runOnUiThread(() -> {
                progressBarMain.setVisibility(View.GONE); // ✅ Hide progress bar

                likedPhotos.clear();
                likedPhotos.addAll(photos);
                photoAdapter.notifyDataSetChanged();

                // ✅ If no photos are left, show "No Liked Photos" message
                if (likedPhotos.isEmpty()) {
                    noLikedPhotosText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noLikedPhotosText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    public void removePhotoFromList(Photo photo) {
        likedPhotos.remove(photo); // ✅ Remove from the list
        photoAdapter.notifyDataSetChanged(); // ✅ Refresh UI

        if (likedPhotos.isEmpty()) {
            noLikedPhotosText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLikedPhotos(); // ✅ Refresh liked photos when returning
    }
}
