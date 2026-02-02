package net.lenni0451.sourcegen.utils;

import net.lenni0451.commons.gson.GsonParser;
import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.commons.httpclient.HttpClient;
import net.lenni0451.commons.httpclient.HttpResponse;
import net.lenni0451.commons.httpclient.constants.HttpHeaders;
import net.lenni0451.commons.httpclient.content.impl.ByteArrayContent;
import net.lenni0451.commons.httpclient.executor.ExecutorType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NetUtils {

    private static final HttpClient HTTP_CLIENT = new HttpClient(ExecutorType.URL_CONNECTION)
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

    public static List<String> getMavenVersions(final String url) throws IOException {
        try {
            String xml = new String(get(url), StandardCharsets.UTF_8);
            List<String> versions = new ArrayList<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            NodeList versionNodes = document.getElementsByTagName("version");
            for (int i = 0; i < versionNodes.getLength(); i++) {
                versions.add(versionNodes.item(i).getTextContent());
            }
            return versions;
        } catch (Exception e) {
            throw new IOException("Failed to parse maven metadata from " + url, e);
        }
    }

    public static String getMavenLatestVersion(final String url) throws IOException {
        try {
            String xml = new String(get(url), StandardCharsets.UTF_8);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            return document.getElementsByTagName("latest").item(0).getTextContent();
        } catch (Exception e) {
            throw new IOException("Failed to parse maven metadata from " + url, e);
        }
    }

}
