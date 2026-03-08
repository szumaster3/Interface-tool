package com.alex.loaders.mesh;

import com.alex.loaders.interfaces.ComponentDefinition;

import java.awt.*;
import java.awt.image.*;

public final class Unknown3 implements ImageObserver, ImageProducer {

    private final int width;
    private final int height;

    private final int[] pixels;
    private final int[] screen;
    private final Image image;
    private final ColorModel colorModel = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);
    private ImageConsumer consumer;

    public Unknown3(int width, int height, Component component) {
        this.width = width;
        this.height = height;

        Unknown1.setLighting(0.8D);
        Unknown1.setDimensions(width, height);

        this.screen = Unknown1.screen;
        this.pixels = new int[width * height];

        this.image = component.createImage(this);
        component.prepareImage(image, this);

        initPanel();
    }

    private void initPanel() {
        ModelData.pixels = pixels;
        ModelData.width = width;
        ModelData.height = height;
        ModelData.width2 = width;
        ModelData.height2 = height;
        ModelData.field65 = width - 1;
    }

    public void updateModel(Unknown2 model, ComponentDefinition comp) {
        if (model == null || comp == null) return;
        Unknown1.method1();
        Unknown1.screen = screen;
        if(comp.getUnknownModelProp4() == 3000)
            model.setViewPoint(512, 0,0,0,0,0, comp.getModelZoom());
        else
            model.setViewPoint(0, 0, 0, 0, 0, 0, comp.getModelZoom());
        Unknown1.screenPosX = width ;// 2;
        Unknown1.screenPosY = height ;// 2;
    }


    public void updatePreview(int x, int y, Graphics g) {
        flushPixels();
        g.drawImage(image, x, y, this);
    }

    public void clear() {
        for (int i = 0; i < pixels.length; i++)
            pixels[i] = 0x00000000;
    }

    private synchronized void flushPixels() {
        for (int i = 0; i < pixels.length; i++) {
            if ((pixels[i] & 0x00FFFFFF) != 0)
                pixels[i] |= 0xFF000000;
        }

        consumer.setPixels(
                0, 0,
                width, height,
                colorModel,
                pixels,
                0, width
        );
        consumer.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
    }

    @Override
    public synchronized void addConsumer(ImageConsumer c) {
        consumer = c;
        c.setDimensions(width, height);
        c.setProperties(null);
        c.setColorModel(colorModel);
        c.setHints(ImageConsumer.TOPDOWNLEFTRIGHT);
    }

    @Override
    public synchronized boolean isConsumer(ImageConsumer c) {
        return consumer == c;
    }

    @Override
    public synchronized void removeConsumer(ImageConsumer c) {
        if (consumer == c)
            consumer = null;
    }

    @Override
    public void startProduction(ImageConsumer c) {
        addConsumer(c);
    }

    @Override
    public void requestTopDownLeftRightResend(ImageConsumer c) {
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        return true;
    }
}
