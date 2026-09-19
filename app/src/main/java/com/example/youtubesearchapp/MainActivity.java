package com.example.youtubesearchapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText searchEditText;
    private ProgressBar progressBar;
    private TextView emptyView;
    private VideoAdapter adapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        RecyclerView videosRecycler = findViewById(R.id.videosRecycler);

        adapter = new VideoAdapter();
        videosRecycler.setLayoutManager(new LinearLayoutManager(this));
        videosRecycler.setAdapter(adapter);

        searchButton.setOnClickListener(v -> startSearch());
        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                startSearch();
                return true;
            }
            return false;
        });
    }

    private void startSearch(){
        String query = searchEditText.getText().toString().trim();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (query.isEmpty()){
            emptyView.setText(R.string.no_search_term);
            emptyView.setVisibility(View.VISIBLE);
            adapter.setData(new ArrayList<>());
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm != null ? cm.getActiveNetworkInfo() : null;
        if (info == null || !info.isConnected()){
            emptyView.setText(R.string.no_network);
            emptyView.setVisibility(View.VISIBLE);
            adapter.setData(new ArrayList<>());
            return;
        }

        new FetchYouTubeTask(progressBar, adapter, emptyView).execute(query);
    }

    static class FetchYouTubeTask extends AsyncTask<String, Void, List<VideoItem>> {
        private final WeakReference<ProgressBar> progRef;
        private final WeakReference<VideoAdapter> adpRef;
        private final WeakReference<TextView> emptyRef;
        private String errorMessage = null;

        FetchYouTubeTask(ProgressBar pb, VideoAdapter adp, TextView empty){
            this.progRef = new WeakReference<>(pb);
            this.adpRef = new WeakReference<>(adp);
            this.emptyRef = new WeakReference<>(empty);
        }

        @Override protected void onPreExecute(){
            ProgressBar pb = progRef.get();
            TextView empty = emptyRef.get();
            if (pb != null) pb.setVisibility(View.VISIBLE);
            if (empty != null) {
                empty.setText(R.string.loading);
                empty.setVisibility(View.VISIBLE);
            }
        }

        @Override protected List<VideoItem> doInBackground(String... params){
            String q = params[0];
            try{
                if (BuildConfig.YOUTUBE_API_KEY.trim().isEmpty()) {
                    errorMessage = "YouTube API key is not configured";
                    return null;
                }

                URL url = NetworkUtils.buildYouTubeUrl(
                        q,
                        20,
                        BuildConfig.YOUTUBE_API_KEY,
                        "relevance"
                );
                String json = NetworkUtils.getHttp(url);
                if (json == null || json.trim().isEmpty()) {
                    errorMessage = "No response from server";
                    return null;
                }

                JSONObject root = new JSONObject(json);
                if (root.has("error")){
                    errorMessage = root.getJSONObject("error").optString("message","API error");
                    return null;
                }

                JSONArray items = root.optJSONArray("items");
                if (items == null || items.length() == 0) return new ArrayList<>();

                List<VideoItem> list = new ArrayList<>();
                for (int i = 0; i < items.length(); i++){
                    JSONObject item = items.getJSONObject(i);
                    String videoId = item.optJSONObject("id") != null ? item.getJSONObject("id").optString("videoId") : "";
                    JSONObject sn = item.optJSONObject("snippet");
                    if (sn == null) continue;
                    String title = sn.optString("title");
                    String desc = sn.optString("description");
                    String publishedAt = sn.optString("publishedAt");
                    String channelTitle = sn.optString("channelTitle");
                    String thumbUrl = null;
                    JSONObject thumbs = sn.optJSONObject("thumbnails");
                    if (thumbs != null){
                        JSONObject high = thumbs.optJSONObject("high");
                        JSONObject medium = thumbs.optJSONObject("medium");
                        JSONObject def = thumbs.optJSONObject("default");
                        if (high != null) thumbUrl = high.optString("url");
                        else if (medium != null) thumbUrl = medium.optString("url");
                        else if (def != null) thumbUrl = def.optString("url");
                    }
                    list.add(new VideoItem(videoId, title, desc, publishedAt, channelTitle, thumbUrl));
                }
                return list;
            } catch (Exception ex){
                errorMessage = ex.getMessage();
                return null;
            }
        }

        @Override protected void onPostExecute(List<VideoItem> result){
            ProgressBar pb = progRef.get();
            TextView empty = emptyRef.get();
            VideoAdapter adp = adpRef.get();
            if (pb != null) pb.setVisibility(View.GONE);

            if (result == null){
                if (empty != null){
                    empty.setText(errorMessage != null ? errorMessage : (empty.getResources().getString(R.string.no_results)));
                    empty.setVisibility(View.VISIBLE);
                }
                if (adp != null) adp.setData(new ArrayList<>());
                return;
            }
            if (result.isEmpty()){
                if (empty != null){
                    empty.setText(R.string.no_results);
                    empty.setVisibility(View.VISIBLE);
                }
                if (adp != null) adp.setData(new ArrayList<>());
            } else {
                if (empty != null) empty.setVisibility(View.GONE);
                if (adp != null) adp.setData(result);
            }
        }
    }
}
