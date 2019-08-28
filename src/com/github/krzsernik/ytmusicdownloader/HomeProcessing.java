package com.github.krzsernik.ytmusicdownloader;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

public class HomeProcessing {
    JSONObject prevEndpoint;
    JSONObject requestBody;

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

        requestBody = new JSONObject("{\"context\":{\"client\":{\"clientName\":\"WEB_REMIX\"," +
                "\"clientVersion\":\"0.1\",\"hl\":\"pl\",\"gl\":\"PL\",\"experimentIds\":[]}," +
                "\"capabilities\":{},\"request\":{\"internalExperimentFlags\":{}}}," +
                "\"enablePersistentPlaylistPanel\":true,\"tunerSettingValue\":\"AUTOMIX_SETTING_NORMAL\"," +
                "\"isAudioOnly\":true}");

        JSONObject flags = jsonObj.getJSONObject("EXPERIMENT_FLAGS");
        for (Iterator<String> it = flags.keys(); it.hasNext(); ) {
            String key = it.next();
            requestBody.getJSONObject("context").getJSONObject("request")
                    .getJSONObject("internalExperimentFlags").put(key, flags.get(key));
        }

        prevEndpoint = new JSONObject(jsonObj.getString("INITIAL_ENDPOINT"));
        String baseJSUrl = "https:" + jsonObj.getJSONObject("PLAYER_CONFIG").getJSONObject("assets").getString("js");
        System.out.println(baseJSUrl);

        Signature.setBaseUrl(baseJSUrl);

        /*
        x-youtube-client-name: INNERTUBE_CONTEXT_CLIENT_NAME
        x-youtube-client-version: INNERTUBE_CLIENT_VERSION
        x-youtube-identity-token: QUFFLUhqbXN1ZEZhX0ZXN1VQTlhFaUhoek5HUWNmODBZQXw=  <--- only important when logged in, created in index
        x-youtube-page-cl: PAGE_CL
        x-youtube-page-label: PAGE_BUILD_LABEL
        x-youtube-utc-offset: 120  <--- offset from utc given in minutes
         */
    }

    public static void main(String[] args) {
        try {
            HomeProcessing h = new HomeProcessing("https://music.youtube.com/watch?v=idvFihD_cJk");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
