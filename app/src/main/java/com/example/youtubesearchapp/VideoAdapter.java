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
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @Override public long getItemId(int position) {
        String vid = data.get(position).getVideoId();
        return vid == null ? position : vid.hashCode();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos){
        VideoItem it = data.get(pos);
        h.title.setText(it.getTitle());
        h.channel.setText(it.getChannelTitle());
        h.desc.setText(it.getDescription());
        String published = it.getPublishedAt();
        if (published != null && published.length() >= 10) h.publishedAt.setText(published.substring(0,10));
        else h.publishedAt.setText(published != null ? published : "");
        Glide.with(h.thumb.getContext())
                .load(it.getThumbnailUrl())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(h.thumb);
        h.thumb.setContentDescription(it.getTitle());
        h.itemView.setOnClickListener(v -> {
            String vid = it.getVideoId();
            if (vid != null && !vid.isEmpty()) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + vid));
                v.getContext().startActivity(i);
            }
        });
    }

    @Override public int getItemCount(){ return data.size(); }

    static class VH extends RecyclerView.ViewHolder{
        ImageView thumb;
        TextView title, channel, publishedAt, desc;
        VH(@NonNull View v){
            super(v);
            thumb = v.findViewById(R.id.thumb);
            title = v.findViewById(R.id.title);
            channel = v.findViewById(R.id.channel);
            publishedAt = v.findViewById(R.id.publishedAt);
            desc = v.findViewById(R.id.desc);
        }
    }
}
