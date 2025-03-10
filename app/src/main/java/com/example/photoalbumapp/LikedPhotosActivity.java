package com.example.photoalbumapp;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Reusing main layout

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        appDatabase = AppDatabase.getInstance(this);
        photoAdapter = new PhotoAdapter(this, new ArrayList<>(), appDatabase);
        recyclerView.setAdapter(photoAdapter);

        // ✅ Load liked images when the activity starts
        loadLikedPhotos();
    }

    // ✅ Add this method inside `LikedPhotosActivity`
    private void loadLikedPhotos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Photo> likedPhotos = appDatabase.photoDao().getFavoritePhotos();
            runOnUiThread(() -> {
                photoAdapter.updateList(likedPhotos); // ✅ Ensures UI updates when photos are removed
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLikedPhotos(); // ✅ Refresh the list every time the user returns to this activity
    }
}
