package ru.netology;

import java.io.IOException;
import java.io.InputStream;

public class Request {
    private String method;
    private String path;
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
        path = parts[1];
        protocolVersion = parts[2];
        headers = request.substring(request.indexOf("\r\n") + 2, request.length() - 4);
        body = inputStream;
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
