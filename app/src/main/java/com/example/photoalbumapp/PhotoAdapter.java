package com.example.photoalbumapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyViewHolder> {

    private final Context context;
    private List<Photo> photoList;
    private final AppDatabase appDatabase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PhotoAdapter(Context context, List<Photo> photoList, AppDatabase appDatabase) {
        this.context = context;
        this.photoList = photoList;
        this.appDatabase = appDatabase;
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

        // Debugging - Check if the image URL is null or empty
        Log.d("PhotoAdapter", "Loading Image URL: " + photo.getDownloadUrl());

        // Use a default placeholder if `downloadUrl` is null or empty
        String imageUrl = (photo.getDownloadUrl() != null && !photo.getDownloadUrl().isEmpty())
                ? photo.getDownloadUrl()
                : "https://via.placeholder.com/300"; // Default placeholder image

        // Load image using Glide with proper error handling
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)  // Ensure this drawable exists
                .error(R.drawable.error)              // Ensure this drawable exists
                .into(holder.imageView);

        // Set author name
        holder.authorTextView.setText(photo.getAuthor());

        // Set like button icon
        holder.likeButton.setImageResource(photo.isLiked() ? R.drawable.liked : R.drawable.unliked);

        // Like button click listener
        holder.likeButton.setOnClickListener(v -> {
            boolean isLiked = photo.toggleLike();
            holder.likeButton.setImageResource(isLiked ? R.drawable.liked : R.drawable.unliked);

            // Show toast message
            Toast.makeText(context, isLiked ? "Saved to Favorites" : "Removed from Favorites", Toast.LENGTH_SHORT).show();

            // Save or remove from local storage using background thread
            executorService.execute(() -> {
                if (isLiked) {
                    appDatabase.photoDao().insert(photo); // Save liked photo
                } else {
                    appDatabase.photoDao().deletePhotoById(photo.getPhotoId()); // Remove if unliked
                }
            });

            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    // Method to update list when filtering
    public void updateList(List<Photo> newList) {
        this.photoList = new ArrayList<>(newList);
        notifyDataSetChanged();
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
