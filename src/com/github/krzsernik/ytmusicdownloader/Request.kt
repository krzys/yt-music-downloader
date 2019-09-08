package com.github.krzsernik.ytmusicdownloader;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
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
        m_conn.setConnectTimeout(5000);
        m_conn.setRequestMethod(method);
        m_conn.setRequestProperty("Accept", "*/*");
        m_conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
        m_conn.setDoInput(true);
    }

    public void setRequestHeader(String name, String value) {
        m_conn.setRequestProperty(name, value);
    }

    public void setCookies(List<HttpCookie> cookies) {
        StringBuilder sb = new StringBuilder();
        cookies.forEach(cookie -> {
            sb.append(cookie.toString() + "; ");
        });
        m_conn.setRequestProperty("Cookie", sb.toString());
    }

    public void setData(String output) throws IOException {
        m_conn.setDoOutput(true);

        OutputStream os = m_conn.getOutputStream();
        os.write(output.getBytes("UTF-8"));
    }

    public int send() throws IOException {
        int statusCode = m_conn.getResponseCode();

        return statusCode;
    }

    public HttpsURLConnection getConnection() {
        return m_conn;
    }

    public String getHeader(String name) {
        return m_conn.getHeaderField(name);
    }

    public List<HttpCookie> getCookies() {
        if(m_cookieManager.getCookieStore().getCookies().isEmpty()) {
            String cookiesHeader = m_conn.getHeaderField("Set-Cookie");
            List<HttpCookie> cookies = HttpCookie.parse(cookiesHeader);
            cookies.forEach(cookie -> m_cookieManager.getCookieStore().add(null, cookie));
        }

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
