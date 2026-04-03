package com.alex.io;

public final class OutputStream extends Stream {
    private static final int[] BIT_MASK = new int[32];

    static {
        for (int i = 0; i < 32; ++i) {
            BIT_MASK[i] = (1 << i) - 1;
        }
    }

    public OutputStream(int capacity) {
        this.setBuffer(new byte[capacity]);
    }

    public OutputStream() {
        this.setBuffer(new byte[16]);
    }

    public void checkCapacityPosition(int position) {
        if (position >= this.getBuffer().length) {
            byte[] newBuffer = new byte[position + 16];
            System.arraycopy(this.getBuffer(), 0, newBuffer, 0, this.getBuffer().length);
            this.setBuffer(newBuffer);
        }

    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void writeBytes(byte[] b) {
        byte offset = 0;
        int length = b.length;
        this.checkCapacityPosition(this.getOffset() + length - offset);
        System.arraycopy(b, offset, this.getBuffer(), this.getOffset(), length);
        this.setOffset(this.getOffset() + (length - offset));
    }

    public void writeByte(int i) {
        this.writeByte(i, this.offset++);
    }

    public void writeByte(int i, int position) {
        this.checkCapacityPosition(position);
        this.getBuffer()[position] = (byte) i;
    }

    public void writeBigSmart(int i) {
        //if(i >= 32767 && i >= 0) {
        if (i >= 52767 && i >= 0) {
            this.writeInt(i - Integer.MAX_VALUE - 1);
        } else {
            this.writeShort(i >= 0 ? i : 52767);
            //this.writeShort(i >= 0?i:32767);
        }

    }

    public void writeShort(int i) {
        this.writeByte(i >> 8);
        this.writeByte(i);
    }

    public void writeShortLE(int i) {
        this.writeByte(i);
        this.writeByte(i >> 8);
    }

    public void write24BitInt(int i) {
        this.writeByte(i >> 16);
        this.writeByte(i >> 8);
        this.writeByte(i);
    }

    public void writeInt(int i) {
        this.writeByte(i >> 24);
        this.writeByte(i >> 16);
        this.writeByte(i >> 8);
        this.writeByte(i);
    }

    public void writeString(String s) {
        this.checkCapacityPosition(this.getOffset() + s.length() + 1);
        System.arraycopy(s.getBytes(), 0, this.getBuffer(), this.getOffset(), s.length());
        this.setOffset(this.getOffset() + s.length());
        this.writeByte(0);
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
