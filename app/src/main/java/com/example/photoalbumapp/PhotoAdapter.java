package com.example.photoalbumapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyViewHolder> {

    private final Context context;
    private List<Photo> photoList;
    private final AppDatabase appDatabase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final boolean isLocalStorage; // ✅ Flag to check if it's Local Storage

    public PhotoAdapter(Context context, List<Photo> photoList, AppDatabase appDatabase, boolean isLocalStorage) {
        this.context = context;
        this.photoList = photoList;
        this.appDatabase = appDatabase;
        this.isLocalStorage = isLocalStorage;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Photo photo = photoList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(photo.getDownloadUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.imageView);

        holder.authorTextView.setText(photo.getAuthor());

        // Set like button icon
        holder.likeButton.setImageResource(photo.isLiked() ? R.drawable.liked : R.drawable.unliked);

        // Like button click listener
        holder.likeButton.setOnClickListener(v -> {
            boolean isLiked = photo.toggleLike();
            holder.likeButton.setImageResource(isLiked ? R.drawable.liked : R.drawable.unliked);

            // Show toast message
            Toast.makeText(context, isLiked ? "Saved to Favorites" : "Removed from Favorites", Toast.LENGTH_SHORT).show();

            executorService.execute(() -> {
                if (isLiked) {
                    appDatabase.photoDao().insert(photo);
                } else {
                    appDatabase.photoDao().deletePhotoById(photo.getApiId());

                    // ✅ Remove from UI instantly in LikedPhotosActivity
                    if (isLocalStorage && context instanceof LikedPhotosActivity) {
                        ((LikedPhotosActivity) context).runOnUiThread(() -> {
                            ((LikedPhotosActivity) context).removePhotoFromList(photo);
                        });
                    }
                }
            });

            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    // ✅ Fix: Add updateList() method to update UI properly
    public void updateList(List<Photo> newList) {
        this.photoList.clear();
        this.photoList.addAll(newList);
        notifyDataSetChanged(); // ✅ Ensure UI refreshes
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, likeButton;
        TextView authorTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            likeButton = itemView.findViewById(R.id.likeButton);
            authorTextView = itemView.findViewById(R.id.authorTextView);
        }
    }
}
