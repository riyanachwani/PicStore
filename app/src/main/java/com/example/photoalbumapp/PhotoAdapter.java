package com.example.photoalbumapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyViewHolder> {

    private final Context context;
    private final List<Photo> photoList;

    public PhotoAdapter(Context context, List<Photo> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new MyViewHolder(view);
    }

    // 🟢 Step 5: Bind data to ViewHolder
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Photo photo = photoList.get(position);
        String imageUrl = photo.getDownloadUrl();  // Fixed this line

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)  // Optional placeholder
                .error(R.drawable.error)              // Optional error image
                .into(holder.imageView);              // ImageView from ViewHolder

        // Handle like button click if present
        holder.imageView.setOnClickListener(v -> {
            boolean isLiked = photo.toggleLike();  // Toggle like status
            Toast.makeText(context, isLiked ? "Liked!" : "Unliked!", Toast.LENGTH_SHORT).show();
            notifyItemChanged(position);  // Refresh item
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);  // Make sure ID matches your layout
        }
    }
}
