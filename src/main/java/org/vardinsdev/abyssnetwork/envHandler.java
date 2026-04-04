package org.vardinsdev.abyssnetwork;

import io.github.cdimascio.dotenv.Dotenv;

public class envHandler {
    public static Dotenv register() {
        return Dotenv.load();
    }
}
