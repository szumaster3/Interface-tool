package fs.com.alex;

import fs.com.alex.store.Store;

import java.io.IOException;

public final class Cache {

    private static Store store;

    private Cache() {
    }

    public static void init(String path) throws IOException {
        store = new Store(path);
    }

    public static Store getStore() {
        return store;
    }

    public static boolean isInitialized() {
        return store != null;
    }
}
