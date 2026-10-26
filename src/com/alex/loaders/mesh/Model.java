package com.alex.loaders.mesh;

/**
 * Decoded RuneScape model.
 * <p>
 * Decoder ported from Simple-JFX-Model-Editor
 * <a href="https://github.com/Suicolen/Simple-JFX-Model-Editor">Source</a>
 */
public final class Model {

    public int modelId = -1;

    public int vertices;
    public int triangleCount;
    public int texturedFaces;

    public int[] verticesXCoordinate;
    public int[] verticesYCoordinate;
    public int[] verticesZCoordinate;

    public int[] faceIndicesA;
    public int[] faceIndicesB;
    public int[] faceIndicesC;

    /**
     * HSL (16-bit) colour of every face.
     */
    public short[] triangleColors;
    /**
     * Face render type, may be null.
     */
    public int[] triangleInfo;
    /**
     * Face alpha (0 = opaque, 255 = fully transparent), may be null.
     */
    public int[] faceAlpha;
    public byte[] trianglePriorities;
    public byte modelPriority;
    public int[] vertexWeights;
    public int[] triangleSkin;

    /**
     * Material id of every face or -1, may be null.
     */
    public short[] faceMaterial;

    /**
     * True when the model was stored with version 15 (coordinates have 4x resolution).
     */
    public boolean isNewHeader;

    // bounding box (model space, after scaling)
    public int minX, maxX, minY, maxY, minZ, maxZ;

    /**
     * Light intensity of every face corner (client style lighting: ambient 64, light -50/-10/-50).
     */
    public int[] lightA;
    public int[] lightB;
    public int[] lightC;

    /**
     * Height above the origin (-minY, never negative), used by the client to centre interface models.
     */
    public int height;

    private Model() {
    }

    public static Model decode(byte[] data, int id) {
        if (data == null || data.length < 18) {
            return null;
        }
        Model model = new Model();
        model.modelId = id;
        try {
            if (data[data.length - 1] == -1 && data[data.length - 2] == -1) {
                model.decodeNew(data);
            } else {
                model.decode317(data);
            }
            model.finish();
        } catch (RuntimeException e) {
            System.err.println("Failed to decode model " + id + ": " + e);
            return null;
        }
        return model;
    }

    public boolean isTextured(int face) {
        return faceMaterial != null && faceMaterial[face] >= 0;
    }

    /**
     * Opacity of a face in range 0..255.
     */
    public int getOpacity(int face) {
        if (faceAlpha == null) {
            return 255;
        }
        return 255 - (faceAlpha[face] & 0xFF);
    }

    private void finish() {
        if (isNewHeader) {
            for (int i = 0; i < vertices; i++) {
                verticesXCoordinate[i] /= 4;
                verticesYCoordinate[i] /= 4;
                verticesZCoordinate[i] /= 4;
            }
        }
        for (int i = 0; i < triangleCount; i++) {
            if (faceIndicesA[i] >= vertices || faceIndicesB[i] >= vertices || faceIndicesC[i] >= vertices
                    || faceIndicesA[i] < 0 || faceIndicesB[i] < 0 || faceIndicesC[i] < 0) {
                throw new IllegalStateException("face index out of range");
            }
        }
        if (vertices > 0) {
            minX = minY = minZ = Integer.MAX_VALUE;
            maxX = maxY = maxZ = Integer.MIN_VALUE;
            for (int i = 0; i < vertices; i++) {
                minX = Math.min(minX, verticesXCoordinate[i]);
                maxX = Math.max(maxX, verticesXCoordinate[i]);
                minY = Math.min(minY, verticesYCoordinate[i]);
                maxY = Math.max(maxY, verticesYCoordinate[i]);
                minZ = Math.min(minZ, verticesZCoordinate[i]);
                maxZ = Math.max(maxZ, verticesZCoordinate[i]);
            }
            height = Math.max(0, -minY);
        }
        applyLighting();
    }

    /**
     * True when the face is flat shaded (render type bit 0).
     */
    public boolean isFlat(int face) {
        return triangleInfo != null && (triangleInfo[face] & 1) != 0;
    }

    /**
     * True when the face is hidden (render type -1).
     */
    public boolean isHidden(int face) {
        return triangleInfo != null && triangleInfo[face] == -1;
    }

    /**
     * Port of the client's model lighting (ambient 64, contrast 768, light direction -50/-10/-50).
     */
    private void applyLighting() {
        final int lx = -50, ly = -10, lz = -50, ambient = 64;
        int specular = (int) Math.sqrt(5100.0D);
        specular = specular * 768 >> 8;

        lightA = new int[triangleCount];
        lightB = new int[triangleCount];
        lightC = new int[triangleCount];

        int[] vnx = new int[vertices];
        int[] vny = new int[vertices];
        int[] vnz = new int[vertices];
        int[] vcount = new int[vertices];

        for (int i = 0; i < triangleCount; i++) {
            int a = faceIndicesA[i];
            int b = faceIndicesB[i];
            int c = faceIndicesC[i];
            int ux = verticesXCoordinate[b] - verticesXCoordinate[a];
            int uy = verticesYCoordinate[b] - verticesYCoordinate[a];
            int uz = verticesZCoordinate[b] - verticesZCoordinate[a];
            int vx = verticesXCoordinate[c] - verticesXCoordinate[a];
            int vy = verticesYCoordinate[c] - verticesYCoordinate[a];
            int vz = verticesZCoordinate[c] - verticesZCoordinate[a];
            int nx = uy * vz - vy * uz;
            int ny = uz * vx - vz * ux;
            int nz = ux * vy - vx * uy;
            while (nx > 8192 || ny > 8192 || nz > 8192 || nx < -8192 || ny < -8192 || nz < -8192) {
                nx >>= 1;
                ny >>= 1;
                nz >>= 1;
            }
            int len = (int) Math.sqrt((double) (nx * nx + ny * ny + nz * nz));
            if (len <= 0) {
                len = 1;
            }
            nx = (nx << 8) / len;
            ny = (ny << 8) / len;
            nz = (nz << 8) / len;

            if (isFlat(i)) {
                int light = ambient + (nx * lx + ny * ly + nz * lz) / (specular + specular / 2);
                lightA[i] = light;
                lightB[i] = light;
                lightC[i] = light;
            } else {
                vnx[a] += nx;
                vny[a] += ny;
                vnz[a] += nz;
                vcount[a]++;
                vnx[b] += nx;
                vny[b] += ny;
                vnz[b] += nz;
                vcount[b]++;
                vnx[c] += nx;
                vny[c] += ny;
                vnz[c] += nz;
                vcount[c]++;
            }
        }

        for (int i = 0; i < triangleCount; i++) {
            if (isFlat(i)) {
                continue;
            }
            int a = faceIndicesA[i];
            int b = faceIndicesB[i];
            int c = faceIndicesC[i];
            lightA[i] = ambient + (lx * vnx[a] + ly * vny[a] + lz * vnz[a]) / (specular * vcount[a]);
            lightB[i] = ambient + (lx * vnx[b] + ly * vny[b] + lz * vnz[b]) / (specular * vcount[b]);
            lightC[i] = ambient + (lx * vnx[c] + ly * vny[c] + lz * vnz[c]) / (specular * vcount[c]);
        }
    }

    // New format

    private void decodeNew(byte[] data) {
        Reader first = new Reader(data);
        Reader second = new Reader(data);
        Reader third = new Reader(data);
        Reader fourth = new Reader(data);
        Reader fifth = new Reader(data);
        Reader sixth = new Reader(data);
        Reader seventh = new Reader(data);

        first.pos = data.length - 23;
        vertices = first.unsignedShort();
        triangleCount = first.unsignedShort();
        texturedFaces = first.unsignedByte();

        int flag = first.unsignedByte();
        boolean hasFaceTypes = (flag & 0x1) != 0;
        boolean hasVersion = (flag & 0x8) != 0;
        int version = 0;
        if (hasVersion) {
            first.pos -= 7;
            version = first.unsignedByte();
            first.pos += 6;
        }
        isNewHeader = version == 15;

        int priorityOpcode = first.unsignedByte();
        int alphaOpcode = first.unsignedByte();
        int muscleOpcode = first.unsignedByte();
        int textureOpcode = first.unsignedByte();
        int bonesOpcode = first.unsignedByte();
        int vertexXLength = first.unsignedShort();
        int vertexYLength = first.unsignedShort();
        int vertexZLength = first.unsignedShort();
        int pointsLength = first.unsignedShort();
        int textureIndicesLength = first.unsignedShort();

        int pos = texturedFaces;

        int vertexOffset = pos;
        pos += vertices;

        int renderTypeOffset = pos;
        if (hasFaceTypes) {
            pos += triangleCount;
        }

        int faceOffset = pos;
        pos += triangleCount;

        int priorityOffset = pos;
        if (priorityOpcode == 255) {
            pos += triangleCount;
        }

        int muscleOffset = pos;
        if (muscleOpcode == 1) {
            pos += triangleCount;
        }

        int bonesOffset = pos;
        if (bonesOpcode == 1) {
            pos += vertices;
        }

        int alphaOffset = pos;
        if (alphaOpcode == 1) {
            pos += triangleCount;
        }

        int pointsOffset = pos;
        pos += pointsLength;

        int textureIdOffset = pos;
        if (textureOpcode == 1) {
            pos += triangleCount * 2;
        }

        int textureCoordinateOffset = pos;
        pos += textureIndicesLength;

        int colorOffset = pos;
        pos += triangleCount * 2;

        int vertexXOffset = pos;
        pos += vertexXLength;

        int vertexYOffset = pos;
        pos += vertexYLength;

        int vertexZOffset = pos;

        verticesXCoordinate = new int[vertices];
        verticesYCoordinate = new int[vertices];
        verticesZCoordinate = new int[vertices];
        faceIndicesA = new int[triangleCount];
        faceIndicesB = new int[triangleCount];
        faceIndicesC = new int[triangleCount];
        triangleColors = new short[triangleCount];
        if (bonesOpcode == 1) {
            vertexWeights = new int[vertices];
        }
        if (hasFaceTypes) {
            triangleInfo = new int[triangleCount];
        }
        if (priorityOpcode == 255) {
            trianglePriorities = new byte[triangleCount];
        } else {
            modelPriority = (byte) priorityOpcode;
        }
        if (alphaOpcode == 1) {
            faceAlpha = new int[triangleCount];
        }
        if (muscleOpcode == 1) {
            triangleSkin = new int[triangleCount];
        }
        if (textureOpcode == 1) {
            faceMaterial = new short[triangleCount];
        }

        // vertices
        first.pos = vertexOffset;
        second.pos = vertexXOffset;
        third.pos = vertexYOffset;
        fourth.pos = vertexZOffset;
        fifth.pos = bonesOffset;
        int baseX = 0;
        int baseY = 0;
        int baseZ = 0;
        for (int point = 0; point < vertices; point++) {
            int mask = first.unsignedByte();
            int x = (mask & 1) != 0 ? second.signedSmart() : 0;
            int y = (mask & 2) != 0 ? third.signedSmart() : 0;
            int z = (mask & 4) != 0 ? fourth.signedSmart() : 0;
            baseX += x;
            baseY += y;
            baseZ += z;
            verticesXCoordinate[point] = baseX;
            verticesYCoordinate[point] = baseY;
            verticesZCoordinate[point] = baseZ;
            if (vertexWeights != null) {
                vertexWeights[point] = fifth.unsignedByte();
            }
        }

        // face attributes
        first.pos = colorOffset;
        second.pos = renderTypeOffset;
        third.pos = priorityOffset;
        fourth.pos = alphaOffset;
        fifth.pos = muscleOffset;
        sixth.pos = textureIdOffset;
        seventh.pos = textureCoordinateOffset;
        for (int face = 0; face < triangleCount; face++) {
            triangleColors[face] = (short) first.unsignedShort();
            if (triangleInfo != null) {
                triangleInfo[face] = second.signedByte();
            }
            if (trianglePriorities != null) {
                trianglePriorities[face] = (byte) third.signedByte();
            }
            if (faceAlpha != null) {
                faceAlpha[face] = fourth.unsignedByte();
            }
            if (triangleSkin != null) {
                triangleSkin[face] = fifth.unsignedByte();
            }
            if (faceMaterial != null) {
                faceMaterial[face] = (short) (sixth.unsignedShort() - 1);
                if (faceMaterial[face] >= 0 && triangleInfo != null
                        && triangleInfo[face] < 2
                        && triangleColors[face] != 127
                        && triangleColors[face] != -27075
                        && triangleColors[face] != 8128
                        && triangleColors[face] != 7510) {
                    faceMaterial[face] = -1;
                }
                if (faceMaterial[face] != -1) {
                    seventh.unsignedByte(); // texture coordinate index, not needed for the preview
                }
            }
        }

        // face indices
        decodeFaceIndices(data, pointsOffset, faceOffset);
    }

    // Old format

    private void decode317(byte[] data) {
        Reader first = new Reader(data);
        Reader second = new Reader(data);
        Reader third = new Reader(data);
        Reader fourth = new Reader(data);
        Reader fifth = new Reader(data);

        first.pos = data.length - 18;
        vertices = first.unsignedShort();
        triangleCount = first.unsignedShort();
        texturedFaces = first.unsignedByte();

        int renderTypeOpcode = first.unsignedByte();
        int priorityOpcode = first.unsignedByte();
        int alphaOpcode = first.unsignedByte();
        int skinOpcode = first.unsignedByte();
        int labelOpcode = first.unsignedByte();
        int vertexXLength = first.unsignedShort();
        int vertexYLength = first.unsignedShort();
        int vertexZLength = first.unsignedShort();
        int indicesLength = first.unsignedShort();

        int pos = 0;

        int vertexFlagOffset = pos;
        pos += vertices;

        int compressTypeOffset = pos;
        pos += triangleCount;

        int priorityOffset = pos;
        if (priorityOpcode == 255) {
            pos += triangleCount;
        }

        int skinOffset = pos;
        if (skinOpcode == 1) {
            pos += triangleCount;
        }

        int renderTypeOffset = pos;
        if (renderTypeOpcode == 1) {
            pos += triangleCount;
        }

        int labelOffset = pos;
        if (labelOpcode == 1) {
            pos += vertices;
        }

        int alphaOffset = pos;
        if (alphaOpcode == 1) {
            pos += triangleCount;
        }

        int indicesOffset = pos;
        pos += indicesLength;

        int colorOffset = pos;
        pos += triangleCount * 2;

        pos += texturedFaces * 6;

        int xOffset = pos;
        pos += vertexXLength;

        int yOffset = pos;
        pos += vertexYLength;

        int zOffset = pos;

        verticesXCoordinate = new int[vertices];
        verticesYCoordinate = new int[vertices];
        verticesZCoordinate = new int[vertices];
        faceIndicesA = new int[triangleCount];
        faceIndicesB = new int[triangleCount];
        faceIndicesC = new int[triangleCount];
        triangleColors = new short[triangleCount];
        if (labelOpcode == 1) {
            vertexWeights = new int[vertices];
        }
        if (renderTypeOpcode == 1) {
            triangleInfo = new int[triangleCount];
        }
        if (priorityOpcode == 255) {
            trianglePriorities = new byte[triangleCount];
        } else {
            modelPriority = (byte) priorityOpcode;
        }
        if (alphaOpcode == 1) {
            faceAlpha = new int[triangleCount];
        }
        if (skinOpcode == 1) {
            triangleSkin = new int[triangleCount];
        }

        first.pos = vertexFlagOffset;
        second.pos = xOffset;
        third.pos = yOffset;
        fourth.pos = zOffset;
        fifth.pos = labelOffset;
        int baseX = 0;
        int baseY = 0;
        int baseZ = 0;
        for (int point = 0; point < vertices; point++) {
            int flag = first.unsignedByte();
            int x = (flag & 1) != 0 ? second.signedSmart() : 0;
            int y = (flag & 2) != 0 ? third.signedSmart() : 0;
            int z = (flag & 4) != 0 ? fourth.signedSmart() : 0;
            baseX += x;
            baseY += y;
            baseZ += z;
            verticesXCoordinate[point] = baseX;
            verticesYCoordinate[point] = baseY;
            verticesZCoordinate[point] = baseZ;
            if (vertexWeights != null) {
                vertexWeights[point] = fifth.unsignedByte();
            }
        }

        first.pos = colorOffset;
        second.pos = renderTypeOffset;
        third.pos = priorityOffset;
        fourth.pos = alphaOffset;
        fifth.pos = skinOffset;
        for (int face = 0; face < triangleCount; face++) {
            triangleColors[face] = (short) first.unsignedShort();
            if (triangleInfo != null) {
                triangleInfo[face] = second.unsignedByte();
            }
            if (trianglePriorities != null) {
                trianglePriorities[face] = (byte) third.signedByte();
            }
            if (faceAlpha != null) {
                faceAlpha[face] = fourth.unsignedByte();
            }
            if (triangleSkin != null) {
                triangleSkin[face] = fifth.unsignedByte();
            }
        }

        decodeFaceIndices(data, indicesOffset, compressTypeOffset);
    }

    // Shared

    private void decodeFaceIndices(byte[] data, int indicesOffset, int typeOffset) {
        Reader indices = new Reader(data);
        Reader types = new Reader(data);
        indices.pos = indicesOffset;
        types.pos = typeOffset;
        int a = 0;
        int b = 0;
        int c = 0;
        int last = 0;
        for (int face = 0; face < triangleCount; face++) {
            int opcode = types.unsignedByte();
            if (opcode == 1) {
                a = indices.signedSmart() + last;
                last = a;
                b = indices.signedSmart() + last;
                last = b;
                c = indices.signedSmart() + last;
                last = c;
            } else if (opcode == 2) {
                b = c;
                c = indices.signedSmart() + last;
                last = c;
            } else if (opcode == 3) {
                a = c;
                c = indices.signedSmart() + last;
                last = c;
            } else if (opcode == 4) {
                int tmp = a;
                a = b;
                b = tmp;
                c = indices.signedSmart() + last;
                last = c;
            }
            faceIndicesA[face] = a;
            faceIndicesB[face] = b;
            faceIndicesC[face] = c;
        }
    }

    private static final class Reader {
        private final byte[] data;
        int pos;

        Reader(byte[] data) {
            this.data = data;
        }

        int unsignedByte() {
            return data[pos++] & 0xFF;
        }

        int signedByte() {
            return data[pos++];
        }

        int unsignedShort() {
            pos += 2;
            return ((data[pos - 2] & 0xFF) << 8) | (data[pos - 1] & 0xFF);
        }

        int signedSmart() {
            if ((data[pos] & 0xFF) < 128) {
                return unsignedByte() - 64;
            }
            return unsignedShort() - 49152;
        }
    }
}
