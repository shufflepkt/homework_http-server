package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int NUMBER_OF_TREADS = 64;

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> HANDLERS = new ConcurrentHashMap<>();

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            final ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_TREADS);
            while (true) {
                final var socket = serverSocket.accept();

                threadPool.execute(() -> {
                    try (
                            socket;
                            final var inputStream = socket.getInputStream();
                            final var out = new BufferedOutputStream(socket.getOutputStream());
                    ) {
                        connectionHandler(inputStream, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectionHandler(InputStream inputStream, BufferedOutputStream out) throws IOException {
        Request request = new Request();
        try {
            request.parse(inputStream);
        } catch (BadRequestException e) {
            out.write(compose400OutputHeaders().getBytes());
            out.flush();
            return;
        }

        if (!HANDLERS.containsKey(request.getMethod())) {
            out.write(compose404OutputHeaders().getBytes());
            out.flush();
            return;
        }

        ConcurrentHashMap<String, Handler> handlers = HANDLERS.get(request.getMethod());

        if (!handlers.containsKey(request.getPath())) {
            out.write(compose404OutputHeaders().getBytes());
            out.flush();
            return;
        }

        handlers.get(request.getPath()).handle(request, out);
    }

    private String compose400OutputHeaders() {
        return "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    private String compose404OutputHeaders() {
        return "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    public String compose200OutputHeaders(String mimeType, long length) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    public String compose200OkHeaders() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!HANDLERS.containsKey(method)) {
            HANDLERS.put(method, new ConcurrentHashMap<>());
        }
        HANDLERS.get(method).putIfAbsent(path, handler);
    }
}
