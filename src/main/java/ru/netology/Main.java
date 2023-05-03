package ru.netology;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();
        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/classic.html", (request, responseStream) -> {
            String path = request.getPath();
            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            responseStream.write((server.compose200OutputHeaders(mimeType, content.length)).getBytes());
            responseStream.write(content);
            responseStream.flush();
        });
        server.addHandler("GET", "/spring.png", (request, responseStream) -> {
            final var filePath = Path.of(".", "public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            responseStream.write((server.compose200OutputHeaders(mimeType, length)).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();
        });
        server.addHandler("GET", "/spring.svg", (request, responseStream) -> {
            final var filePath = Path.of(".", "public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            responseStream.write((server.compose200OutputHeaders(mimeType, length)).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();
        });
        server.addHandler("GET", "/forms.html", (request, responseStream) -> {
            final var filePath = Path.of(".", "public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            responseStream.write((server.compose200OutputHeaders(mimeType, length)).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();
        });
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            responseStream.write((server.compose200OkHeaders()).getBytes());
            responseStream.flush();
        });

        server.listen(9999);
    }
}
