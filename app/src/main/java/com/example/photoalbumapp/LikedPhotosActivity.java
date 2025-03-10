package com.example.photoalbumapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.concurrent.Executors;

public class LikedPhotosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Reusing main layout

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        appDatabase = AppDatabase.getInstance(this);

        // Load liked images from local storage
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Photo> likedPhotos = appDatabase.photoDao().getFavoritePhotos();
            runOnUiThread(() -> {
                photoAdapter = new PhotoAdapter(this, likedPhotos, appDatabase);
                recyclerView.setAdapter(photoAdapter);
            });
        });
    }
}
