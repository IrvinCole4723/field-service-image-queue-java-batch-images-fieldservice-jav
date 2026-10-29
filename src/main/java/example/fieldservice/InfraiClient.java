package example.fieldservice;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiClient {
    private static final String RESIZE_CAPABILITY = "infrai.image.resize";
    private final String key;
    private final String baseUrl;
    private final HttpClient http;

    public InfraiClient(String key, String baseUrl) {
        this.key = key;
        this.baseUrl = baseUrl;
        this.http = HttpClient.newHttpClient();
    }

    public String publish(String queue, String payload) throws IOException, InterruptedException {
        String body = "{\"queue\":\"" + json(queue) + "\",\"payload\":\"" + json(payload) + "\"}";
        return send("POST", "/v1/queue/publish", body);
    }

    public String consume(String queue) throws IOException, InterruptedException {
        String body = "{\"queue\":\"" + json(queue) + "\",\"max_messages\":1,\"visibility_timeout\":30}";
        return send("POST", "/v1/queue/consume", body);
    }

    public String resize(String imageId) throws IOException, InterruptedException {
        if (!RESIZE_CAPABILITY.equals("infrai.image.resize")) throw new IllegalStateException("resize capability is not configured");
        String body = "{\"image\":\"" + json(imageId) + "\",\"width\":1600,\"height\":1200,\"fit\":\"inside\",\"enlarge\":false,\"format\":\"jpeg\",\"store\":true}";
        return send("POST", "/v1/image/resize", body);
    }

    private String send(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .build();
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":false")) {
                throw new IOException("Infrai request rejected: " + envelope);
            }
            if (response.statusCode() != 429) {
                if (response.statusCode() >= 500) throw new IOException("Infrai transport response: " + response.statusCode());
                return envelope;
            }
            long delay = retryDelay(response, attempt);
            Thread.sleep(delay);
        }
        throw new IOException("Infrai request retry limit reached");
    }

    private long retryDelay(HttpResponse<String> response, int attempt) {
        return response.headers().firstValue("Retry-After").map(Long::parseLong).map(v -> v * 1000)
                .orElse(250L * (1L << attempt));
    }

    private String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
