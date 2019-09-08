package com.github.krzsernik.ytmusicdownloader

import org.json.JSONObject

class Video {
    internal var id: String
    internal var title: String
    internal var author: String
    internal var endpoint: JSONObject

    internal constructor(title: String, author: String) {
        this.id = ""
        this.title = title
        this.author = author
        this.endpoint = JSONObject()
    }

    internal constructor(videoInfo: JSONObject) {
        id = videoInfo.getString("videoId")
        title = videoInfo.getJSONObject("title").getJSONArray("runs")
                .getJSONObject(0).getString("text")
        author = videoInfo.getJSONObject("shortBylineText").getJSONArray("runs")
                .getJSONObject(0).getString("text")
        endpoint = videoInfo.getJSONObject("navigationEndpoint").getJSONObject("watchEndpoint")
    }
}
