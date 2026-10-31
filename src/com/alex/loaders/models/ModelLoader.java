package com.alex.loaders.models;

import com.alex.Cache;
import com.alex.store.Index;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads and caches models.
 */
public final class ModelLoader {

    private static final Map<Integer, Model> MODELS = new HashMap<>();
    private static final Object MISSING_LOCK = new Object();

    private ModelLoader() {
    }

    public static Model getModel(int modelId) {
        if (modelId < 0) {
            return null;
        }
        synchronized (MISSING_LOCK) {
            Model cached = MODELS.get(modelId);
            if (cached != null) {
                return cached;
            }
            try {
                Index index = Cache.getStore().getIndexes()[7];
                if (index == null || index.getArchive(modelId) == null) {
                    return null;
                }
                byte[] data = index.getFile(modelId, 0, null);
                Model model = Model.decode(data, modelId);
                if (model != null) {
                    MODELS.put(modelId, model);
                }
                return model;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Drops every cached model, e.g. after the cache has been modified.
     */
    public static void clear() {
        synchronized (MISSING_LOCK) {
            MODELS.clear();
        }
    }
}
