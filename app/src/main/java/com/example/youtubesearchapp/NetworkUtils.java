package com.example.youtubesearchapp;

import android.net.Uri;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class NetworkUtils {
    private static final String LOG_TAG = "NetworkUtils";
    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String PARAM_PART = "part", PARAM_TYPE = "type", PARAM_Q = "q",
            PARAM_MAX = "maxResults", PARAM_KEY = "key", PARAM_ORDER = "order";

    static URL buildYouTubeUrl(String query, int maxResults, String apiKey, String order) throws Exception {
        Uri.Builder b = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_PART, "snippet")
                .appendQueryParameter(PARAM_TYPE, "video")
                .appendQueryParameter(PARAM_Q, query)
                .appendQueryParameter(PARAM_MAX, String.valueOf(maxResults))
                .appendQueryParameter(PARAM_KEY, apiKey);
        if (order != null && !order.isEmpty()) b.appendQueryParameter(PARAM_ORDER, order);
        return new URL(b.build().toString());
    }

    static String getHttp(URL url) {
        HttpURLConnection conn = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "YouTubeSearchApp/1.0");
            conn.connect();

            int code = conn.getResponseCode();
            is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return null;

            isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');

            if (code < 200 || code >= 300) {
                Log.w(LOG_TAG, "HTTP " + code + " for " + url + " body=" + sb);
            }
            return sb.length() == 0 ? null : sb.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "HTTP error for " + url, e);
            return null;
        } finally {
            try { if (reader != null) reader.close(); } catch (Exception ignore) {}
            try { if (isr != null) isr.close(); } catch (Exception ignore) {}
            try { if (is != null) is.close(); } catch (Exception ignore) {}
            if (conn != null) conn.disconnect();
        }
    }
}
