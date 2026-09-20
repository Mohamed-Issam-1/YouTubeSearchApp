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
import android.widget.LinearLayout;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText searchEditText;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView modeLabel;
    private TextView resultsHeader;
    private LinearLayout suggestionsContainer;
    private VideoAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        modeLabel = findViewById(R.id.modeLabel);
        resultsHeader = findViewById(R.id.resultsHeader);
        suggestionsContainer = findViewById(R.id.suggestionsContainer);
        RecyclerView videosRecycler = findViewById(R.id.videosRecycler);

        TextView suggestionAndroid = findViewById(R.id.suggestionAndroid);
        TextView suggestionAi = findViewById(R.id.suggestionAi);
        TextView suggestionMusic = findViewById(R.id.suggestionMusic);
        TextView suggestionWeb = findViewById(R.id.suggestionWeb);

        adapter = new VideoAdapter();
        videosRecycler.setLayoutManager(new LinearLayoutManager(this));
        videosRecycler.setAdapter(adapter);

        boolean demoMode = BuildConfig.YOUTUBE_API_KEY.trim().isEmpty();
        modeLabel.setVisibility(demoMode ? View.VISIBLE : View.GONE);

        searchButton.setOnClickListener(v -> startSearch());

        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP) {
                startSearch();
                return true;
            }
            return false;
        });

        suggestionAndroid.setOnClickListener(v ->
                runSuggestedSearch(getString(R.string.suggestion_android)));

        suggestionAi.setOnClickListener(v ->
                runSuggestedSearch(getString(R.string.suggestion_ai)));

        suggestionMusic.setOnClickListener(v ->
                runSuggestedSearch(getString(R.string.suggestion_music)));
    }

    private void runSuggestedSearch(String query){
        searchEditText.setText(query);
        searchEditText.setSelection(query.length());
        startSearch();
    }

    private void startSearch(){
        String query = searchEditText.getText().toString().trim();

        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(
                    searchEditText.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS
            );
        }

        if (query.isEmpty()){
            showStartState();
            emptyView.setText(R.string.no_search_term);
            return;
        }

        boolean demoMode = BuildConfig.YOUTUBE_API_KEY.trim().isEmpty();

        if (!demoMode) {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo info = cm != null ? cm.getActiveNetworkInfo() : null;

            if (info == null || !info.isConnected()){
                emptyView.setText(R.string.no_network);
                emptyView.setVisibility(View.VISIBLE);
                suggestionsContainer.setVisibility(View.VISIBLE);
                resultsHeader.setVisibility(View.GONE);
                adapter.setData(new ArrayList<>());
                return;
            }
        }

        suggestionsContainer.setVisibility(View.GONE);
        resultsHeader.setVisibility(View.GONE);

        new FetchYouTubeTask(
                progressBar,
                adapter,
                emptyView,
                resultsHeader,
                suggestionsContainer
        ).execute(query);
    }

    private void showStartState(){
        adapter.setData(new ArrayList<>());
        resultsHeader.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        suggestionsContainer.setVisibility(View.VISIBLE);
    }

    static class FetchYouTubeTask extends AsyncTask<String, Void, List<VideoItem>> {
        private final WeakReference<ProgressBar> progRef;
        private final WeakReference<VideoAdapter> adpRef;
        private final WeakReference<TextView> emptyRef;
        private final WeakReference<TextView> resultsHeaderRef;
        private final WeakReference<LinearLayout> suggestionsRef;
        private String errorMessage = null;

        FetchYouTubeTask(
                ProgressBar pb,
                VideoAdapter adp,
                TextView empty,
                TextView resultsHeader,
                LinearLayout suggestions
        ){
            this.progRef = new WeakReference<>(pb);
            this.adpRef = new WeakReference<>(adp);
            this.emptyRef = new WeakReference<>(empty);
            this.resultsHeaderRef = new WeakReference<>(resultsHeader);
            this.suggestionsRef = new WeakReference<>(suggestions);
        }

        @Override
        protected void onPreExecute(){
            ProgressBar pb = progRef.get();
            TextView empty = emptyRef.get();
            TextView resultsHeader = resultsHeaderRef.get();

            if (pb != null) {
                pb.setVisibility(View.VISIBLE);
            }

            if (resultsHeader != null) {
                resultsHeader.setVisibility(View.GONE);
            }

            if (empty != null) {
                empty.setText(R.string.loading);
                empty.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<VideoItem> doInBackground(String... params){
            String query = params[0];

            if (BuildConfig.YOUTUBE_API_KEY.trim().isEmpty()) {
                return buildDemoResults(query);
            }

            try{
                URL url = NetworkUtils.buildYouTubeUrl(
                        query,
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
                    errorMessage = root.getJSONObject("error")
                            .optString("message", "API error");
                    return null;
                }

                JSONArray items = root.optJSONArray("items");

                if (items == null || items.length() == 0) {
                    return new ArrayList<>();
                }

                List<VideoItem> list = new ArrayList<>();

                for (int i = 0; i < items.length(); i++){
                    JSONObject item = items.getJSONObject(i);

                    String videoId = item.optJSONObject("id") != null
                            ? item.getJSONObject("id").optString("videoId")
                            : "";

                    JSONObject snippet = item.optJSONObject("snippet");

                    if (snippet == null) {
                        continue;
                    }

                    String title = snippet.optString("title");
                    String description = snippet.optString("description");
                    String publishedAt = snippet.optString("publishedAt");
                    String channelTitle = snippet.optString("channelTitle");

                    String thumbnailUrl = null;
                    JSONObject thumbnails = snippet.optJSONObject("thumbnails");

                    if (thumbnails != null){
                        JSONObject high = thumbnails.optJSONObject("high");
                        JSONObject medium = thumbnails.optJSONObject("medium");
                        JSONObject standard = thumbnails.optJSONObject("default");

                        if (high != null) {
                            thumbnailUrl = high.optString("url");
                        } else if (medium != null) {
                            thumbnailUrl = medium.optString("url");
                        } else if (standard != null) {
                            thumbnailUrl = standard.optString("url");
                        }
                    }

                    list.add(new VideoItem(
                            videoId,
                            title,
                            description,
                            publishedAt,
                            channelTitle,
                            thumbnailUrl
                    ));
                }

                return list;

            } catch (Exception ex){
                errorMessage = ex.getMessage();
                return null;
            }
        }

        private static List<VideoItem> buildDemoResults(String query){
            String normalizedQuery = query.trim();
            List<VideoItem> list = new ArrayList<>();

            list.add(new VideoItem(
                    "",
                    normalizedQuery + " - Getting Started",
                    "A practical introduction with the key concepts you need to begin.",
                    "2026-09-19T10:00:00Z",
                    "Tech Starter",
                    R.drawable.demo_thumb_1
            ));

            list.add(new VideoItem(
                    "",
                    normalizedQuery + " - Complete Guide",
                    "A structured walkthrough covering setup, workflow, and useful tips.",
                    "2026-09-18T14:30:00Z",
                    "Learning Lab",
                    R.drawable.demo_thumb_2
            ));

            list.add(new VideoItem(
                    "",
                    normalizedQuery + " - Tips and Examples",
                    "Short examples that demonstrate how the topic can be applied in practice.",
                    "2026-09-17T09:15:00Z",
                    "Developer Notes",
                    R.drawable.demo_thumb_3
            ));

            list.add(new VideoItem(
                    "",
                    normalizedQuery + " - Practical Walkthrough",
                    "A local demo result used when live YouTube credentials are not configured.",
                    "2026-09-16T16:45:00Z",
                    "Code Studio",
                    R.drawable.demo_thumb_4
            ));

            return list;
        }

        @Override
        protected void onPostExecute(List<VideoItem> result){
            ProgressBar pb = progRef.get();
            TextView empty = emptyRef.get();
            VideoAdapter adp = adpRef.get();
            TextView resultsHeader = resultsHeaderRef.get();
            LinearLayout suggestions = suggestionsRef.get();

            if (pb != null) {
                pb.setVisibility(View.GONE);
            }

            if (result == null){
                if (empty != null){
                    empty.setText(
                            errorMessage != null
                                    ? errorMessage
                                    : empty.getResources().getString(R.string.no_results)
                    );
                    empty.setVisibility(View.VISIBLE);
                }

                if (resultsHeader != null) {
                    resultsHeader.setVisibility(View.GONE);
                }

                if (suggestions != null) {
                    suggestions.setVisibility(View.VISIBLE);
                }

                if (adp != null) {
                    adp.setData(new ArrayList<>());
                }

                return;
            }

            if (result.isEmpty()){
                if (empty != null){
                    empty.setText(R.string.no_results);
                    empty.setVisibility(View.VISIBLE);
                }

                if (resultsHeader != null) {
                    resultsHeader.setVisibility(View.GONE);
                }

                if (suggestions != null) {
                    suggestions.setVisibility(View.VISIBLE);
                }

                if (adp != null) {
                    adp.setData(new ArrayList<>());
                }

            } else {
                if (empty != null) {
                    empty.setVisibility(View.GONE);
                }

                if (suggestions != null) {
                    suggestions.setVisibility(View.GONE);
                }

                if (resultsHeader != null) {
                    String countText = result.size() == 1
                            ? "1 result"
                            : String.format(Locale.US, "%d results", result.size());

                    resultsHeader.setText(countText);
                    resultsHeader.setVisibility(View.VISIBLE);
                }

                if (adp != null) {
                    adp.setData(result);
                }
            }
        }
    }
}
