
# Mobile App Development 2
# Assignment 2: 🎥 YouTube Video Search App
An Android application to search for YouTube videos using **YouTube Data API v3**.  
The user enters a search query, and the app displays a list of videos with title, channel, publish date, description, and thumbnail.

---

## 📱 Features
- Search for videos via YouTube API.  
- Display results in a **RecyclerView** with CardView design.  
- Load thumbnails using **Glide** with placeholder.  
- Open videos directly in YouTube when clicking on an item.  
- Handle common errors (no internet, empty input, no results).  
- Lightweight and easy-to-use UI.  

---

## 🛠️ Tools & Technologies
- **RecyclerView** for lists  
- **CardView** for item design  
- **Glide** for image loading  
- **AsyncTask** for background tasks (educational purposes, deprecated in modern apps)  
- **HttpURLConnection** for network requests  

---

## 📂 Project Structure
```

 ├── MainActivity.java       # Main activity (UI + search logic)
 ├── NetworkUtils.java       # Build URLs and perform HTTP requests
 ├── VideoItem.java          # Data model (video info)
 ├── VideoAdapter.java       # RecyclerView Adapter to display videos
```

```
 ├── activity_main.xml       # Main screen UI
 ├── item_video.xml          # Video card layout
```

---

## 🚀 How to Run
1. Clone or download the project and open it in **Android Studio**.  
2. Add your YouTube API key to the project-root `local.properties` file:
   ```properties
   YOUTUBE_API_KEY=YOUR_API_KEY
   ```
   A placeholder is provided in `local.properties.example`.

   Keep the real key out of Git and restrict it to the required Android app and YouTube Data API in Google Cloud.

   > Get or manage API credentials from [Google Cloud Console](https://console.cloud.google.com/).  
3. Run the app on an emulator or real device.  
4. Enter a search query and see results instantly!  

---

## ⚠️ Notes
- Ensure you have an active internet connection.  
- API keys have quota limits; exceeding them may cause API errors.  

---

## 👨‍💻 Developer
- Mohamed Issam Qesht
- Instructor: Ibrahim O.Kaware
