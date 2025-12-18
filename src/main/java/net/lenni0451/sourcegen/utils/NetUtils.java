package net.lenni0451.sourcegen.utils;

import net.lenni0451.commons.gson.GsonParser;
import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.commons.httpclient.HttpClient;
import net.lenni0451.commons.httpclient.HttpResponse;
import net.lenni0451.commons.httpclient.constants.HttpHeaders;
import net.lenni0451.commons.httpclient.content.impl.ByteArrayContent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class NetUtils {

    private static final HttpClient HTTP_CLIENT = new HttpClient()
            .setHeader("User-Agent", "Mozilla/5.0");

    public static byte[] get(final String url) throws IOException {
        return HTTP_CLIENT.get(url).execute().getContent().getAsBytes();
    }

    public static byte[] postInsecure(final String url, final String contentType, final byte[] data) throws IOException {
        return HTTP_CLIENT.post(url).setContent(new ByteArrayContent(data)).setIgnoreInvalidSSL(true).setHeader(HttpHeaders.CONTENT_TYPE, contentType).execute().getContent().getAsBytes();
    }

    public static void download(final String url, final File out) throws IOException {
        HttpResponse response = HTTP_CLIENT.get(url).setStreamedResponse(true).execute();
        try (InputStream download = response.getContent().getAsStream()) {
            if (response.getStatusCode() / 100 != 2) {
                throw new IOException("Failed to download file from " + url + " (Status: " + response.getStatusCode() + ")");
            }
            try (FileOutputStream fos = new FileOutputStream(out)) {
                download.transferTo(fos);
            }
        }
    }

    public static GsonObject getJsonObject(final String url) throws IOException {
        String response = new String(get(url), StandardCharsets.UTF_8);
        return GsonParser.parse(response).asObject();
    }

    public static GsonArray getJsonArray(final String url) throws IOException {
        String response = new String(get(url), StandardCharsets.UTF_8);
        return GsonParser.parse(response).asArray();
    }

}
