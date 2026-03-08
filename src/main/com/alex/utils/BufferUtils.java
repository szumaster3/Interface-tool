package com.alex.utils;


public final class BufferUtils {
    private final byte[] buffer;
    public int offset;

    public BufferUtils(byte[] data) {
        this.buffer = data;
        this.offset = 0;
    }

    public int readUnsignedByte() {
        return this.buffer[this.offset++] & 255;
    }

    public byte readByte() {
        return this.buffer[this.offset++];
    }

    public int readUnsignedShort() {
        this.offset += 2;
        return ((this.buffer[this.offset - 2] & 255) << 8) + (this.buffer[this.offset - 1] & 255);
    }

    public int readShort() {
        this.offset += 2;
        return ((this.buffer[this.offset - 2] & 255) << 8) + (this.buffer[this.offset - 1] & 255);
    }

    public int readSmart() {
        return (this.buffer[this.offset] & 255) < 128 ? this.readUnsignedByte() - 64 : this.readUnsignedShort() - 49152;
    }

    public int readMedium() {
        this.offset += 3;
        return ((this.buffer[this.offset - 3] & 255) << 16) + ((this.buffer[this.offset - 2] & 255) << 8) + (this.buffer[this.offset - 1] & 255);
    }
}
