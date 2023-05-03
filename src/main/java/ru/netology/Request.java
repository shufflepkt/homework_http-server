package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class Request {
    private String method;
    private String path;
    private List<NameValuePair> queryParams;
    private String protocolVersion;
    private String headers;
    private InputStream body;

    public void parse(InputStream inputStream) throws IOException, BadRequestException {
        StringBuilder request = new StringBuilder();
        int ch;
        while ((ch = inputStream.read()) != -1) {
            request.append((char) ch);
            if (request.toString().contains("\r\n\r\n")) {
                break;
            }
        }

        String firstLine = request.substring(0, request.indexOf("\r\n"));

        final var parts = firstLine.split(" ");

        if (parts.length != 3) {
            throw new BadRequestException(firstLine);
        }

        method = parts[0];
        String url = parts[1];
        path = url.substring(0, url.indexOf('?'));
        queryParams = URLEncodedUtils.parse(url.substring(url.indexOf('?') + 1), StandardCharsets.UTF_8);
        protocolVersion = parts[2];
        headers = request.substring(request.indexOf("\r\n") + 2, request.length() - 4);
        body = inputStream;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return queryParams.stream().filter(x -> name.equals(x.getName())).collect(Collectors.toList());
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
    }
}
