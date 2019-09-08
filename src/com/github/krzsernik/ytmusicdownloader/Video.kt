package com.github.krzsernik.ytmusicdownloader;

import org.json.JSONObject;

public class Video {
    String id;
    String title;
    String author;
    JSONObject endpoint;

    Video(JSONObject videoInfo) {
        id = videoInfo.getString("videoId");
        title = videoInfo.getJSONObject("title").getJSONArray("runs")
                .getJSONObject(0).getString("text");
        author = videoInfo.getJSONObject("shortBylineText").getJSONArray("runs")
                .getJSONObject(0).getString("text");
        endpoint = videoInfo.getJSONObject("navigationEndpoint").getJSONObject("watchEndpoint");
    }
}
