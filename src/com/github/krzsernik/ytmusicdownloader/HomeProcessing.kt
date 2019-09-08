package com.github.krzsernik.ytmusicdownloader

import org.json.JSONObject
import java.io.IOException
import java.net.HttpCookie
import java.util.*

class HomeProcessing @Throws(IOException::class)
internal constructor(internal var referer: String) {
    internal var prevEndpoint: JSONObject = JSONObject()
    internal var requestBody = JSONObject("{\"context\":{\"client\":{\"clientName\":\"WEB_REMIX\"," +
            "\"clientVersion\":\"0.1\",\"hl\":\"pl\",\"gl\":\"PL\",\"experimentIds\":[],\"utcOffsetMinutes\":120," +
            "\"locationInfo\":{\"locationPermissionAuthorizationStatus\":" +
            "\"LOCATION_PERMISSION_AUTHORIZATION_STATUS_UNSUPPORTED\"}," +
            "\"musicAppInfo\":{\"musicActivityMasterSwitch\":\"MUSIC_ACTIVITY_MASTER_SWITCH_INDETERMINATE\"," +
            "\"musicLocationMasterSwitch\":\"MUSIC_LOCATION_MASTER_SWITCH_INDETERMINATE\"}}," +
            "\"capabilities\":{},\"request\":{\"sessionIndex\":{},\"internalExperimentFlags\":{}},\"user\":{\"enableSafetyMode\":false}}," +
            "\"enablePersistentPlaylistPanel\":true,\"tunerSettingValue\":\"AUTOMIX_SETTING_NORMAL\"," +
            "\"isAudioOnly\":true}")
    internal var homeJson: JSONObject = JSONObject()
    internal var cookies: List<HttpCookie>
    internal var videosList: ArrayList<Video> = ArrayList()

    init {
        val req = Request(referer, "GET")
        req.send()

        val homeBody = req.content
        cookies = req.cookies
        req.close()

        scrapeData(homeBody)

        val ou = youtubeRequest("https://music.youtube.com/youtubei/v1/next?alt=json&key=" + homeJson.getString("INNERTUBE_API_KEY"))
        val videos = ou.getJSONObject("contents").getJSONObject("singleColumnMusicWatchNextResultsRenderer")
                .getJSONObject("playlist").getJSONObject("playlistPanelRenderer").getJSONArray("contents")
        for (key in 0 until videos.length()) {
            videosList.add(Video(videos.getJSONObject(key).getJSONObject("playlistPanelVideoRenderer")))
        }
    }

    @Throws(IOException::class)
    fun youtubeRequest(url: String): JSONObject {
        val req = Request(url, "POST")
        req.cookies = cookies
        req.setRequestHeader("Content-Type", "application/json; charset=utf-8")
        req.setRequestHeader("referer", referer)
        req.setRequestHeader("x-goog-visitor-id", homeJson.getString("VISITOR_DATA"))
        req.setRequestHeader("x-youtube-client-name", homeJson.get("INNERTUBE_CONTEXT_CLIENT_NAME").toString())
        req.setRequestHeader("x-youtube-client-version", homeJson.getString("INNERTUBE_CLIENT_VERSION"))
        //        req.setRequestHeader("x-youtube-identity-token", homeJson.getString("ID_TOKEN"));
        req.setRequestHeader("x-youtube-page-cl", homeJson.get("PAGE_CL").toString())
        req.setRequestHeader("x-youtube-page-label", homeJson.getString("PAGE_BUILD_LABEL"))
        req.setRequestHeader("x-youtube-utc-offset", "120")

        val data = JSONObject(requestBody, requestBody.keySet().toTypedArray())
        val it = prevEndpoint.keys()
        while (it.hasNext()) {
            val key = it.next()
            data.put(key, prevEndpoint.get(key))
        }
        req.setData(data.toString())

        val status = req.send()
        println("STATUS: $status")

        return JSONObject(req.content)
    }

    fun scrapeData(homeBody: String) {
        var json = homeBody.split("ytcfg\\.set\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        json = json.split("\\);</script>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

        homeJson = JSONObject(json)

        val flags = homeJson.getJSONObject("EXPERIMENT_FLAGS")
        val experimentFlags = requestBody.getJSONObject("context").getJSONObject("request")
                .getJSONObject("internalExperimentFlags")
        val it = flags.keys()
        while (it.hasNext()) {
            val key = it.next()
            experimentFlags.put(key, flags.get(key))
        }

        prevEndpoint = JSONObject(homeJson.getString("INITIAL_ENDPOINT")).getJSONObject("watchEndpoint")
        val baseJSUrl = "https:" + homeJson.getJSONObject("PLAYER_CONFIG").getJSONObject("assets").getString("js")

        Signature.setBaseUrl(baseJSUrl)
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val h = HomeProcessing("https://music.youtube.com/watch?v=aAmq7lJLwh8&list=RDMM9EwXewPAFZk")
                for (v in h.videosList) {
                    println(v.author + " - " + v.title)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}