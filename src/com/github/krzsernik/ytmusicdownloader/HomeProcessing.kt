package com.github.krzsernik.ytmusicdownloader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HomeProcessing {
    JSONObject prevEndpoint;
    JSONObject requestBody = new JSONObject("{\"context\":{\"client\":{\"clientName\":\"WEB_REMIX\"," +
            "\"clientVersion\":\"0.1\",\"hl\":\"pl\",\"gl\":\"PL\",\"experimentIds\":[],\"utcOffsetMinutes\":120," +
            "\"locationInfo\":{\"locationPermissionAuthorizationStatus\":" +
            "\"LOCATION_PERMISSION_AUTHORIZATION_STATUS_UNSUPPORTED\"}," +
            "\"musicAppInfo\":{\"musicActivityMasterSwitch\":\"MUSIC_ACTIVITY_MASTER_SWITCH_INDETERMINATE\"," +
            "\"musicLocationMasterSwitch\":\"MUSIC_LOCATION_MASTER_SWITCH_INDETERMINATE\"}}," +
            "\"capabilities\":{},\"request\":{\"sessionIndex\":{},\"internalExperimentFlags\":{}},\"user\":{\"enableSafetyMode\":false}}," +
            "\"enablePersistentPlaylistPanel\":true,\"tunerSettingValue\":\"AUTOMIX_SETTING_NORMAL\"," +
            "\"isAudioOnly\":true}");
    JSONObject homeJson;
    List<HttpCookie> cookies;
    String referer;
    List<Video> videosList = new ArrayList<>();

    HomeProcessing(String url) throws IOException {
        referer = url;
        Request req = new Request(url, "GET");
        req.send();

        String homeBody = req.getContent();
        cookies = req.getCookies();
        req.close();

        scrapeData(homeBody);

        JSONObject ou = youtubeRequest("https://music.youtube.com/youtubei/v1/next?alt=json&key=" + homeJson.getString("INNERTUBE_API_KEY"));
        JSONArray videos = ou.getJSONObject("contents").getJSONObject("singleColumnMusicWatchNextResultsRenderer")
                .getJSONObject("playlist").getJSONObject("playlistPanelRenderer").getJSONArray("contents");
        for(int key = 0; key < videos.length(); key++) {
            videosList.add(new Video(videos.getJSONObject(key).getJSONObject("playlistPanelVideoRenderer")));
        }
    }

    public JSONObject youtubeRequest(String url) throws IOException {
        Request req = new Request(url, "POST");
        req.setCookies(cookies);
        req.setRequestHeader("Content-Type", "application/json; charset=utf-8");
        req.setRequestHeader("referer", referer);
        req.setRequestHeader("x-goog-visitor-id", homeJson.getString("VISITOR_DATA"));
        req.setRequestHeader("x-youtube-client-name", homeJson.get("INNERTUBE_CONTEXT_CLIENT_NAME").toString());
        req.setRequestHeader("x-youtube-client-version", homeJson.getString("INNERTUBE_CLIENT_VERSION"));
//        req.setRequestHeader("x-youtube-identity-token", homeJson.getString("ID_TOKEN"));
        req.setRequestHeader("x-youtube-page-cl", homeJson.get("PAGE_CL").toString());
        req.setRequestHeader("x-youtube-page-label", homeJson.getString("PAGE_BUILD_LABEL"));
        req.setRequestHeader("x-youtube-utc-offset", "120");

        JSONObject data = new JSONObject(requestBody, requestBody.keySet().toArray(new String[0]));
        for (Iterator<String> it = prevEndpoint.keys(); it.hasNext(); ) {
            String key = it.next();
            data.put(key, prevEndpoint.get(key));
        }
        req.setData(data.toString());

        int status = req.send();
        System.out.println("STATUS: " + status);

        return new JSONObject(req.getContent());
    }

    public void scrapeData(String homeBody) {
        String json = homeBody.split("ytcfg\\.set\\(")[1];
        json = json.split("\\);</script>")[0];

        homeJson = new JSONObject(json);

        JSONObject flags = homeJson.getJSONObject("EXPERIMENT_FLAGS");
        JSONObject experimentFlags = requestBody.getJSONObject("context").getJSONObject("request")
                .getJSONObject("internalExperimentFlags");
        for (Iterator<String> it = flags.keys(); it.hasNext(); ) {
            String key = it.next();
            experimentFlags.put(key, flags.get(key));
        }

        prevEndpoint = new JSONObject(homeJson.getString("INITIAL_ENDPOINT")).getJSONObject("watchEndpoint");
        String baseJSUrl = "https:" + homeJson.getJSONObject("PLAYER_CONFIG").getJSONObject("assets").getString("js");
        System.out.println(baseJSUrl);

        Signature.setBaseUrl(baseJSUrl);
    }

    public static void main(String[] args) {
        try {
            HomeProcessing h = new HomeProcessing("https://music.youtube.com/watch?v=aAmq7lJLwh8&list=RDMM9EwXewPAFZk");
//            for (Video v: h.videosList) {
//                System.out.println(v.author + " - " + v.title);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
