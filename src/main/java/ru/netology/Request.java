package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Request {
    private String method;
    private String path;
    private List<NameValuePair> queryParams;
    private String protocolVersion;
    private String headers;
    private List<String> listOfHeaders;
    private InputStream body;
    private List<NameValuePair> postParams;

    public static final String POST = "POST";
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String TYPE_X_WWW = "application/x-www-form-urlencoded";

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

        String pathAndQuery = parts[1];
        if (!pathAndQuery.startsWith("/")) {
            throw new BadRequestException(firstLine);
        }

        if (!pathAndQuery.contains("?")) {
            path = pathAndQuery;
        } else {
            path = pathAndQuery.substring(0, pathAndQuery.indexOf('?'));
            queryParams = URLEncodedUtils.parse(pathAndQuery.substring(pathAndQuery.indexOf('?') + 1), StandardCharsets.UTF_8);
        }

        protocolVersion = parts[2];

        headers = request.substring(request.indexOf("\r\n") + 2, request.length() - 4);
        listOfHeaders = Arrays.asList(headers.split("\r\n"));

        body = inputStream;

        if (method.equals(POST)) {
            final var contentLength = extractHeader(listOfHeaders, CONTENT_LENGTH_HEADER);
            final var contentType = extractHeader(listOfHeaders, CONTENT_TYPE_HEADER);

            if (contentLength.isPresent() && TYPE_X_WWW.equals(contentType.get())) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyString = new String(body.readNBytes(length));
                postParams = URLEncodedUtils.parse(bodyString, StandardCharsets.UTF_8);
            }
        }
    }

    private static Optional<String> extractHeader(List<String> listOfHeaders, String header) {
        return listOfHeaders.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return queryParams.stream().filter(x -> name.equals(x.getName())).collect(Collectors.toList());
    }

    public List<NameValuePair> getPostParams() {
        return method.equals(POST) ? postParams : null;
    }

    public List<NameValuePair> getPostParam(String name) {
        return method.equals(POST) ? postParams.stream().filter(x -> name.equals(x.getName())).collect(Collectors.toList()) : null;
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
