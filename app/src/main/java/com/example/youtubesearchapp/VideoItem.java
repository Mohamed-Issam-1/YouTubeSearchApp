package com.example.youtubesearchapp;

public class VideoItem {
    private final String videoId, title, description, publishedAt, channelTitle, thumbnailUrl;
    public VideoItem(String videoId, String title, String description,
                     String publishedAt, String channelTitle, String thumbnailUrl) {
        this.videoId = videoId; this.title = title; this.description = description;
        this.publishedAt = publishedAt; this.channelTitle = channelTitle; this.thumbnailUrl = thumbnailUrl;
    }
    public String getVideoId() { return videoId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPublishedAt() { return publishedAt; }
    public String getChannelTitle() { return channelTitle; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}
