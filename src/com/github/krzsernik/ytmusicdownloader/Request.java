package com.github.krzsernik.ytmusicdownloader;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.List;

public class Request {
    private String m_url;
    private CookieManager m_cookieManager = new CookieManager();
    private HttpsURLConnection m_conn;

    Request(String url, String method) throws IOException {
        m_url = url;

        URL urlObj = new URL(url);
        m_conn = (HttpsURLConnection) urlObj.openConnection();
        m_conn.setRequestMethod(method);
    }

    public void setRequestHeader(String name, String value) {
        m_conn.setRequestProperty(name, value);
    }

    public int send() throws IOException {
        int statusCode = m_conn.getResponseCode();

        String cookiesHeader = m_conn.getHeaderField("Set-Cookie");
        List<HttpCookie> cookies = HttpCookie.parse(cookiesHeader);
        cookies.forEach(cookie -> m_cookieManager.getCookieStore().add(null, cookie));

        return statusCode;
    }

    public String getHeader(String name) {
        return m_conn.getHeaderField(name);
    }

    public List<HttpCookie> getCookies() {
        return m_cookieManager.getCookieStore().getCookies();
    }

    public String getContent() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(m_conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return content.toString();
    }

    public void close() {
        m_conn.disconnect();
    }
}
