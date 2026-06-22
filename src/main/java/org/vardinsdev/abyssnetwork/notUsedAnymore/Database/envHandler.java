package org.vardinsdev.abyssnetwork.notUsedAnymore.Database;

import io.github.cdimascio.dotenv.Dotenv;

public class envHandler {
    public static Dotenv register() {
        return Dotenv.load();
    }
}
