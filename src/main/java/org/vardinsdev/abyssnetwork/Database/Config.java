package org.vardinsdev.abyssnetwork.Database;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Central access point for environment-driven configuration.
 * Reads keys from {@code .env} via dotenv-java. {@code TYPE=dev} disables
 * persistence entirely (no API calls), matching the historic dev mode.
 */
public final class Config {
    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    private Config() {
    }

    public static boolean isDev() {
        return "dev".equalsIgnoreCase(DOTENV.get("TYPE", "prod"));
    }

    public static String apiBaseUrl() {
        String url = DOTENV.get("ABYSS_API_URL", "http://localhost:8080");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static String apiToken() {
        return DOTENV.get("ABYSS_API_TOKEN", "");
    }
}
