package com.example.youtubesearchapp;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VH> {
    private final List<VideoItem> data = new ArrayList<>();

    public VideoAdapter() {
        setHasStableIds(true);
    }

    public void setData(List<VideoItem> items){
        data.clear();
        if (items != null) {
            data.addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        String videoId = data.get(position).getVideoId();
        return videoId == null || videoId.isEmpty()
                ? position
                : videoId.hashCode();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position){
        VideoItem item = data.get(position);

        holder.title.setText(item.getTitle());
        holder.channel.setText(item.getChannelTitle());
        holder.description.setText(item.getDescription());

        String publishedAt = item.getPublishedAt();
        if (publishedAt != null && publishedAt.length() >= 10) {
            holder.publishedAt.setText(publishedAt.substring(0, 10));
        } else {
            holder.publishedAt.setText(
                    publishedAt != null ? publishedAt : ""
            );
        }

        /*
         * Demo thumbnails are Android drawable resources, not network URLs.
         * Set them directly on the ImageView instead of sending them through
         * Glide. Glide remains responsible only for real YouTube thumbnail URLs.
         */
        if (item.getThumbnailResId() != 0) {
            Glide.with(holder.thumbnail.getContext()).clear(holder.thumbnail);
            holder.thumbnail.setImageResource(item.getThumbnailResId());

        } else if (item.getThumbnailUrl() != null
                && !item.getThumbnailUrl().trim().isEmpty()) {

            Glide.with(holder.thumbnail.getContext())
                    .load(item.getThumbnailUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.thumbnail);

        } else {
            Glide.with(holder.thumbnail.getContext()).clear(holder.thumbnail);
            holder.thumbnail.setImageResource(R.drawable.ic_image_placeholder);
        }

        holder.thumbnail.setContentDescription(item.getTitle());

        String videoId = item.getVideoId();
        boolean hasLiveVideo = videoId != null && !videoId.isEmpty();

        holder.itemView.setClickable(hasLiveVideo);
        holder.itemView.setFocusable(hasLiveVideo);

        holder.itemView.setOnClickListener(hasLiveVideo ? view -> {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + videoId)
            );
            view.getContext().startActivity(intent);
        } : null);
    }

    @Override
    public int getItemCount(){
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView channel;
        TextView publishedAt;
        TextView description;

        VH(@NonNull View view){
            super(view);
            thumbnail = view.findViewById(R.id.thumb);
            title = view.findViewById(R.id.title);
            channel = view.findViewById(R.id.channel);
            publishedAt = view.findViewById(R.id.publishedAt);
            description = view.findViewById(R.id.desc);
        }
    }
}
