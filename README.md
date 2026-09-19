# YouTube Search App

A lightweight Android application built with Java that searches YouTube videos using the YouTube Data API v3 and displays the results in a clean, scrollable interface.

## Features

- Search YouTube videos by keyword.
- Display results in a RecyclerView using CardView-based items.
- Show video title, channel, publish date, description, and thumbnail.
- Load thumbnails with Glide and a fallback placeholder.
- Open a selected video directly in YouTube.
- Handle empty input, missing internet connection, no-result states, and API errors.

## Tech Stack

- Java
- Android SDK
- RecyclerView
- CardView
- Glide
- HttpURLConnection
- YouTube Data API v3
- Gradle

## Requirements

- JDK 21 recommended for the current Gradle setup
- Android SDK Platform 36
- Android SDK Build Tools
- A YouTube Data API v3 key

## Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/Mohamed-Issam-1/YouTubeSearchApp.git
   cd YouTubeSearchApp
   ```

2. Create a `local.properties` file in the project root. You can use `local.properties.example` as a reference.

3. Add your Android SDK path and YouTube API key:

   ```properties
   sdk.dir=C:/Users/YourName/AppData/Local/Android/Sdk
   YOUTUBE_API_KEY=YOUR_API_KEY
   ```

4. Keep `local.properties` out of version control. Restrict the API key in Google Cloud to the required Android application and the YouTube Data API v3.

5. Build the debug APK:

   ```powershell
   .\gradlew.bat clean assembleDebug
   ```

6. The generated APK will be available at:

   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

## Project Structure

```text
app/src/main/
|-- java/com/example/youtubesearchapp/
|   |-- MainActivity.java
|   |-- NetworkUtils.java
|   |-- VideoAdapter.java
|   `-- VideoItem.java
|
`-- res/
    |-- layout/
    |   |-- activity_main.xml
    |   `-- item_video.xml
    `-- drawable/
```

## Security

The YouTube API key is read from the local `local.properties` file and is not stored in the Git repository.

API keys embedded in Android applications can still be extracted from the built APK, so Google Cloud restrictions should always be configured for the key.

## Technical Note

The current implementation retains `AsyncTask` from the original Java implementation. `AsyncTask` is deprecated in modern Android development; a future refactor could replace it with a more current background-execution approach.

## Status

Completed.
