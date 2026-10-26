package com.alex.loaders.mesh;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * Software renderer which draws a {@link Model}.
 */
public final class ModelRenderer {

    private static final double[] SIN = new double[2048];
    private static final double[] COS = new double[2048];
    private static final int[] PALETTE = new int[65536];
    private static final double BRIGHTNESS = 0.8D;
    private static final double FOCAL = 512.0;

    static {
        for (int i = 0; i < 2048; i++) {
            double angle = i * Math.PI * 2.0 / 2048.0;
            SIN[i] = Math.sin(angle);
            COS[i] = Math.cos(angle);
        }
        buildPalette();
    }

    /**
     * Same palette as the client: hue (6 bits), saturation (3 bits), lightness (7 bits).
     */
    private static void buildPalette() {
        int index = 0;
        for (int i = 0; i < 512; i++) {
            double hue = (double) (i / 8) / 64.0D + 0.0078125D;
            double sat = (double) (i & 7) / 8.0D + 0.0625D;
            for (int l = 0; l < 128; l++) {
                double light = (double) l / 128.0D;
                double r = light, g = light, b = light;
                if (sat != 0.0D) {
                    double q = light < 0.5D ? light * (sat + 1.0D) : light + sat - light * sat;
                    double p = light * 2.0D - q;
                    double tr = hue + 1.0D / 3.0D;
                    if (tr > 1.0D) {
                        tr--;
                    }
                    double tb = hue - 1.0D / 3.0D;
                    if (tb < 0.0D) {
                        tb++;
                    }
                    r = hueToRgb(p, q, tr);
                    g = hueToRgb(p, q, hue);
                    b = hueToRgb(p, q, tb);
                }
                int ri = (int) (Math.pow((int) (r * 256.0D) / 256.0D, BRIGHTNESS) * 256.0D);
                int gi = (int) (Math.pow((int) (g * 256.0D) / 256.0D, BRIGHTNESS) * 256.0D);
                int bi = (int) (Math.pow((int) (b * 256.0D) / 256.0D, BRIGHTNESS) * 256.0D);
                int rgb = (Math.min(255, ri) << 16) + (Math.min(255, gi) << 8) + Math.min(255, bi);
                PALETTE[index++] = rgb == 0 ? 1 : rgb;
            }
        }
    }

    private static double hueToRgb(double p, double q, double t) {
        if (t * 6.0D < 1.0D) {
            return p + (q - p) * 6.0D * t;
        } else if (t * 2.0D < 1.0D) {
            return q;
        } else if (t * 3.0D < 2.0D) {
            return p + (q - p) * (2.0D / 3.0D - t) * 6.0D;
        }
        return p;
    }

    /**
     * Applies a light value (0..127) to a 16-bit HSL colour.
     */
    private static int mixLight(int hsl, int light) {
        light = light * (hsl & 127) >> 7;
        if (light < 2) {
            light = 2;
        } else if (light > 126) {
            light = 126;
        }
        return (hsl & 0xFF80) + light;
    }

    private final int width;
    private final int height;
    private final BufferedImage image;
    private final int[] pixels;
    private final float[] depth;

    public ModelRenderer(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        this.depth = new float[this.width * this.height];
    }

    public BufferedImage getImage() {
        return image;
    }

    /**
     * Clears the image to the given ARGB colour (0 = transparent).
     */
    public void clear(int argb) {
        Arrays.fill(pixels, argb);
        Arrays.fill(depth, Float.POSITIVE_INFINITY);
    }

    /**
     * Draws the model using the client's interface model set-up.
     *
     * @param centerX screen X of the model origin (centre of the component)
     * @param centerY screen Y of the model origin (centre of the component)
     * @param pitch   component X angle (0..2047), applied as camera pitch around the model origin
     * @param yaw     component Y angle (0..2047)
     * @param roll    component Z angle (0..2047)
     * @param zoom    component zoom, the distance of the model from the camera
     * @param offsetX component origin X, screen offset in pixels
     * @param offsetY component origin Y, screen offset in pixels
     * @param ortho   orthographic projection
     */
    public void render(Model model, int centerX, int centerY, int pitch, int yaw, int roll, int zoom, int offsetX, int offsetY, boolean ortho) {
        if (model == null || model.vertices == 0 || model.triangleCount == 0) {
            return;
        }
        if (zoom < 1) {
            zoom = 1;
        }

        double sinP = SIN[pitch & 2047], cosP = COS[pitch & 2047];
        double sinY = SIN[yaw & 2047], cosY = COS[yaw & 2047];
        double sinR = SIN[roll & 2047], cosR = COS[roll & 2047];

        // the model origin is placed 'zoom' units in front of the camera; the camera pitch is applied
        // around that origin. modelOriginX/Y are 2D screen offsets in pixels (verified against the cache:
        // they re-centre models whose bounding box is not centred on the origin).
        double moveY = zoom * sinP;
        double moveZ = zoom * cosP;

        int n = model.vertices;
        double[] vx = new double[n];
        double[] vy = new double[n];
        double[] vz = new double[n];
        double[] sx = new double[n];
        double[] sy = new double[n];

        for (int i = 0; i < n; i++) {
            double x = model.verticesXCoordinate[i];
            double y = model.verticesYCoordinate[i];
            double z = model.verticesZCoordinate[i];

            double t;
            if (roll != 0) {
                t = y * sinR + x * cosR;
                y = y * cosR - x * sinR;
                x = t;
            }
            if (yaw != 0) {
                t = z * sinY + x * cosY;
                z = z * cosY - x * sinY;
                x = t;
            }
            y += moveY;
            z += moveZ;
            t = y * cosP - z * sinP;
            z = y * sinP + z * cosP;
            y = t;

            vx[i] = x;
            vy[i] = y;
            vz[i] = z;
            if (ortho) {
                double scale = FOCAL / zoom;
                sx[i] = centerX + offsetX + x * scale;
                sy[i] = centerY + offsetY + y * scale;
            } else if (z > 1) {
                sx[i] = centerX + offsetX + x * FOCAL / z;
                sy[i] = centerY + offsetY + y * FOCAL / z;
            }
        }

        // faces grouped by priority: a higher priority is drawn on top of lower ones
        int[] bandCount = new int[12];
        for (int f = 0; f < model.triangleCount; f++) {
            bandCount[priorityOf(model, f)]++;
        }
        int bands = 0;
        for (int count : bandCount) {
            if (count > 0) {
                bands++;
            }
        }

        boolean first = true;
        for (int band = 0; band < 12; band++) {
            if (bandCount[band] == 0) {
                continue;
            }
            if (!first && bands > 1) {
                Arrays.fill(depth, Float.POSITIVE_INFINITY);
            }
            first = false;
            for (int pass = 0; pass < 2; pass++) {
                for (int f = 0; f < model.triangleCount; f++) {
                    if (priorityOf(model, f) != band) {
                        continue;
                    }
                    int opacity = model.getOpacity(f);
                    if (opacity <= 0 || (pass == 0) != (opacity >= 255) || model.isHidden(f)) {
                        continue;
                    }
                    drawFace(model, f, vz, sx, sy, opacity, ortho);
                }
            }
        }
    }

    private static int priorityOf(Model model, int face) {
        int priority = model.trianglePriorities != null ? model.trianglePriorities[face] : model.modelPriority;
        return Math.max(0, Math.min(11, priority));
    }

    private void drawFace(Model model, int f, double[] vz, double[] sx, double[] sy, int opacity, boolean ortho) {
        int a = model.faceIndicesA[f];
        int b = model.faceIndicesB[f];
        int c = model.faceIndicesC[f];
        if (!ortho && (vz[a] <= 1 || vz[b] <= 1 || vz[c] <= 1)) {
            return; // behind the camera
        }

        // back face culling, like the client
        double cull = (sx[a] - sx[b]) * (sy[c] - sy[b]) - (sy[a] - sy[b]) * (sx[c] - sx[b]);
        if (cull <= 0) {
            return;
        }

        int base = model.triangleColors[f] & 0xFFFF;
        int rgbA = PALETTE[mixLight(base, model.lightA[f])];
        int rgbB;
        int rgbC;
        if (model.isFlat(f)) {
            rgbB = rgbA;
            rgbC = rgbA;
        } else {
            rgbB = PALETTE[mixLight(base, model.lightB[f])];
            rgbC = PALETTE[mixLight(base, model.lightC[f])];
        }

        fillTriangle(sx[a], sy[a], vz[a], rgbA, sx[b], sy[b], vz[b], rgbB, sx[c], sy[c], vz[c], rgbC, opacity);
    }

    private void fillTriangle(double x0, double y0, double z0, int c0, double x1, double y1, double z1, int c1, double x2, double y2, double z2, int c2, int opacity) {
        int minX = (int) Math.max(0, Math.floor(Math.min(x0, Math.min(x1, x2))));
        int maxX = (int) Math.min(width - 1, Math.ceil(Math.max(x0, Math.max(x1, x2))));
        int minY = (int) Math.max(0, Math.floor(Math.min(y0, Math.min(y1, y2))));
        int maxY = (int) Math.min(height - 1, Math.ceil(Math.max(y0, Math.max(y1, y2))));
        if (minX > maxX || minY > maxY) {
            return;
        }

        double area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);
        if (Math.abs(area) < 1e-9) {
            return;
        }
        double invArea = 1.0 / area;
        double iz0 = 1.0 / z0, iz1 = 1.0 / z1, iz2 = 1.0 / z2;
        boolean translucent = opacity < 255;

        int r0 = (c0 >> 16) & 0xFF, g0 = (c0 >> 8) & 0xFF, b0 = c0 & 0xFF;
        int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;

        for (int y = minY; y <= maxY; y++) {
            double py = y + 0.5;
            int row = y * width;
            for (int x = minX; x <= maxX; x++) {
                double px = x + 0.5;
                double w1 = ((px - x0) * (y2 - y0) - (x2 - x0) * (py - y0)) * invArea;
                double w2 = ((x1 - x0) * (py - y0) - (px - x0) * (y1 - y0)) * invArea;
                double w0 = 1.0 - w1 - w2;
                if (w0 < -1e-6 || w1 < -1e-6 || w2 < -1e-6) {
                    continue;
                }
                float z = (float) (1.0 / (w0 * iz0 + w1 * iz1 + w2 * iz2));
                int idx = row + x;
                if (z >= depth[idx]) {
                    continue;
                }
                int r = clamp((int) (w0 * r0 + w1 * r1 + w2 * r2));
                int g = clamp((int) (w0 * g0 + w1 * g1 + w2 * g2));
                int b = clamp((int) (w0 * b0 + w1 * b1 + w2 * b2));
                if (translucent) {
                    int dst = pixels[idx];
                    if ((dst >>> 24) == 0) {
                        pixels[idx] = (opacity << 24) | (r << 16) | (g << 8) | b;
                    } else {
                        int inv = 255 - opacity;
                        int nr = (r * opacity + ((dst >> 16) & 0xFF) * inv) / 255;
                        int ng = (g * opacity + ((dst >> 8) & 0xFF) * inv) / 255;
                        int nb = (b * opacity + (dst & 0xFF) * inv) / 255;
                        pixels[idx] = 0xFF000000 | (nr << 16) | (ng << 8) | nb;
                    }
                } else {
                    pixels[idx] = 0xFF000000 | (r << 16) | (g << 8) | b;
                    depth[idx] = z;
                }
            }
        }
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }
}
