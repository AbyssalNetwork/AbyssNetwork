package org.vardinsdev.abyssnetwork.Database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.staff.StaffMember;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous HTTP client for the Abyss Go backend. All requests run off the
 * tick thread via {@link HttpClient#sendAsync}. When the API is disabled
 * (dev mode) every call becomes a completed no-op.
 */
public class ApiClient {
    private static final ApiClient INSTANCE = new ApiClient();

    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    private String baseUrl;
    private boolean enabled;

    private ApiClient() {
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public void init() {
        if (Config.isDev()) {
            enabled = false;
            AbyssLogger.warn("API client disabled (TYPE=dev) - nothing will be persisted.");
            return;
        }
        baseUrl = Config.apiBaseUrl();
        enabled = true;
        AbyssLogger.info("API client enabled -> " + baseUrl);
    }

    public boolean isEnabled() {
        return enabled;
    }

    // ---- players ----

    public CompletableFuture<Void> upsertPlayer(String uuid, String username) {
        if (!enabled) return CompletableFuture.completedFuture(null);
        ObjectNode body = mapper.createObjectNode();
        body.put("uuid", uuid);
        body.put("username", username);
        return send("POST", "/players", body.toString())
                .thenAccept(response -> {
                })
                .exceptionally(ex -> logFailure("POST /players", ex));
    }

    // ---- stats ----

    public CompletableFuture<Void> recordStats(String uuid, String username, int kills, int deaths) {
        if (!enabled) return CompletableFuture.completedFuture(null);
        ObjectNode body = mapper.createObjectNode();
        body.put("username", username);
        body.put("kills", kills);
        body.put("deaths", deaths);
        return send("POST", "/players/" + uuid + "/stats", body.toString())
                .thenAccept(response -> {
                })
                .exceptionally(ex -> logFailure("POST /players/" + uuid + "/stats", ex));
    }

    public CompletableFuture<PlayerStats> fetchPlayer(String uuid) {
        if (!enabled) return CompletableFuture.completedFuture(null);
        return send("GET", "/players/" + uuid, null)
                .thenApply(HttpResponse::body)
                .thenApply(this::parsePlayer)
                .exceptionally(ex -> {
                    logFailure("GET /players/" + uuid, ex);
                    return null;
                });
    }

    public CompletableFuture<PlayerStats> fetchPlayerByUsername(String username) {
        if (!enabled) return CompletableFuture.completedFuture(null);
        return send("GET", "/players/by-username/" + encodePath(username), null)
                .thenApply(HttpResponse::body)
                .thenApply(this::parsePlayer)
                .exceptionally(ex -> {
                    logFailure("GET /players/by-username/" + username, ex);
                    return null;
                });
    }

    private static String encodePath(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    private PlayerStats parsePlayer(String json) {
        try {
            return mapper.readValue(json, PlayerStats.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse player response", e);
        }
    }

    // ---- staff ----

    public CompletableFuture<List<StaffMember>> fetchStaff() {
        if (!enabled) return CompletableFuture.completedFuture(List.of());
        return send("GET", "/staff", null)
                .thenApply(HttpResponse::body)
                .thenApply(this::parseStaff)
                .exceptionally(ex -> {
                    logFailure("GET /staff", ex);
                    return List.of();
                });
    }

    public CompletableFuture<Void> upsertStaff(StaffMember member) {
        if (!enabled) return CompletableFuture.completedFuture(null);
        try {
            String body = mapper.writeValueAsString(member);
            return send("POST", "/staff", body)
                    .thenAccept(response -> {
                    })
                    .exceptionally(ex -> logFailure("POST /staff " + member.getUuid(), ex));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<Void> deleteStaff(UUID uuid) {
        if (!enabled) return CompletableFuture.completedFuture(null);
        return send("DELETE", "/staff/" + uuid, null)
                .thenAccept(response -> {
                })
                .exceptionally(ex -> logFailure("DELETE /staff " + uuid, ex));
    }

    private List<StaffMember> parseStaff(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<StaffMember>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse staff response", e);
        }
    }

    // ---- bans ----

    public CompletableFuture<Void> banPlayer(String uuid, String username, String reason, String bannedBy) {
        if (!enabled) return CompletableFuture.completedFuture(null);
        ObjectNode body = mapper.createObjectNode();
        body.put("uuid", uuid);
        body.put("username", username);
        body.put("reason", reason);
        body.put("bannedBy", bannedBy);
        return send("POST", "/bans", body.toString())
                .thenAccept(response -> {
                })
                .exceptionally(ex -> logFailure("POST /bans " + uuid, ex));
    }

    public CompletableFuture<Ban> fetchBan(String uuid) {
        if (!enabled) return CompletableFuture.completedFuture(null);
        return send("GET", "/bans/" + uuid, null)
                .thenApply(response -> response.statusCode() / 100 == 2 ? parseBan(response.body()) : null)
                .exceptionally(ex -> {
                    logFailure("GET /bans/" + uuid, ex);
                    return null;
                });
    }

    public CompletableFuture<Void> unban(String uuid) {
        if (!enabled) return CompletableFuture.completedFuture(null);
        return send("DELETE", "/bans/" + uuid, null)
                .thenAccept(response -> {
                })
                .exceptionally(ex -> logFailure("DELETE /bans/" + uuid, ex));
    }

    private Ban parseBan(String json) {
        try {
            return mapper.readValue(json, Ban.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ban response", e);
        }
    }

    private Void logFailure(String request, Throwable ex) {
        AbyssLogger.error("API " + request + " failed: " + unwrap(ex).getMessage());
        return null;
    }

    private static Throwable unwrap(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private CompletableFuture<HttpResponse<String>> send(String method, String path, String body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(5));
            String token = Config.apiToken();
            if (!token.isEmpty()) {
                builder.header("Authorization", "Bearer " + token);
            }
            if (body != null) {
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofString(body));
            } else {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }
            return client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() / 100 != 2) {
                            AbyssLogger.warn("API " + method + " " + path + " -> " + response.statusCode() + ": " + response.body());
                        }
                        return response;
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
