package com.example.youtubesearchapp;

public class VideoItem {
    private final String videoId;
    private final String title;
    private final String description;
    private final String publishedAt;
    private final String channelTitle;
    private final String thumbnailUrl;
    private final int thumbnailResId;

    public VideoItem(String videoId, String title, String description,
                     String publishedAt, String channelTitle, String thumbnailUrl) {
        this(videoId, title, description, publishedAt, channelTitle, thumbnailUrl, 0);
    }

    public VideoItem(String videoId, String title, String description,
                     String publishedAt, String channelTitle, int thumbnailResId) {
        this(videoId, title, description, publishedAt, channelTitle, null, thumbnailResId);
    }

    private VideoItem(String videoId, String title, String description,
                      String publishedAt, String channelTitle,
                      String thumbnailUrl, int thumbnailResId) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.publishedAt = publishedAt;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailResId = thumbnailResId;
    }

    public String getVideoId() { return videoId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPublishedAt() { return publishedAt; }
    public String getChannelTitle() { return channelTitle; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public int getThumbnailResId() { return thumbnailResId; }
}
