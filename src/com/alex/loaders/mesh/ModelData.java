package com.alex.loaders.mesh;

public class ModelData
{
    public byte[] data;
    public int numVertices;
    public int numTriangles;
    public int numTexTriangles;

    public static int[] pixels;
    public static int width;
    public static int height;
    public static int height2;
    public static int width2;
    public static int field65;

    public int field70;//vertexDirectionOffset;
    public int field71;//xDataOffset;
    public int field72;//yDataOffset;
    public int field73;//zDataOffset;
    public int field74;//vertexSkinOffset;
    public int field75;//triDataOffset;
    public int field76;//triangleTypeOffset;
    public int field77;//colourDataOffset;
    public int field78;//texturePointerOffset;
    public int field79;//trianglePriorityOffset;
    public int field80;//triangleAlphaOffset;
    public int field81;//triangleSkinOffset;
    public int field82;//uvMapTrianglesOffset;
}