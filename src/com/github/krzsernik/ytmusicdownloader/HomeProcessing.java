package com.github.krzsernik.ytmusicdownloader;

import org.json.JSONObject;

import java.io.IOException;

public class HomeProcessing {
    JSONObject prevEndpoint;

    HomeProcessing(String url) throws IOException {
        Request req = new Request(url, "GET");
        req.setRequestHeader("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
        req.send();

        String homeBody = req.getContent();
        req.close();

        String json = homeBody.split("ytcfg\\.set\\(")[1];
        json = json.split("\\);</script>")[0];

        JSONObject jsonObj = new JSONObject(json);
        prevEndpoint = new JSONObject(jsonObj.getString("INITIAL_ENDPOINT"));
        String baseJSUrl = "https:" + jsonObj.getJSONObject("PLAYER_CONFIG").getJSONObject("assets").getString("js");

        Signature.setBaseUrl(baseJSUrl);
    }

    public static void main(String[] args) {
        try {
            HomeProcessing h = new HomeProcessing("https://music.youtube.com/watch?v=idvFihD_cJk");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
