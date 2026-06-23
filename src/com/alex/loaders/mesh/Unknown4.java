package com.alex.loaders.mesh;

import com.alex.Cache;
import com.alex.store.Archive;

import java.util.HashMap;
import java.util.Map;

public class Unknown4 {

    private static final Map<Integer, Unknown2> a = new HashMap<>();
    public static boolean isOptimizedSelected = false;

    public static Unknown2 getModel(int modelId) {
        return a.computeIfAbsent(modelId, id -> {
            try {
                Archive archive = Cache.getStore().getIndexes()[7].getArchive(id);
                if (archive == null)
                    return null;

                byte[] data = Cache.getStore().getIndexes()[7].getFile(id, 0, null);
                if (data == null)
                    return null;

                Unknown2 model;
                Unknown2.initModel(data);

                if (data[data.length - 1] == -1 && data[data.length - 2] == -1) {
                    model = Unknown2.loadModel(data);
                    model.id = modelId;
                    if (model.isNewHeader)
                        model.downScaleModel(4);
                }

                else

                {
                    model = Unknown2.createEmptyModel();
                    model.id = modelId;
                }

                model.method38(64, 768, -50, -10, -50, true);
                return model;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}
