package com.alex.io;

public final class InputStream extends Stream {
   public InputStream(byte[] buffer) {
      this.buffer = buffer;
      this.length = buffer.length;
   }

   public void setLength(int length) {
      this.length = length;
   }

   public void setOffset(int offset) {
      this.offset = offset;
   }

   public int getRemaining() {
      return this.offset < this.length?this.length - this.offset:0;
   }

   public int readByte() {
      return this.getRemaining() > 0?this.buffer[this.offset++]:0;
   }

   public void readBytes(byte[] buffer, int off, int len) {
      for(int k = off; k < len + off; ++k) {
         buffer[k] = (byte)this.readByte();
      }

   }

   public int readUnsignedByte() {
      return this.readByte() & 255;
   }

   public int readShort() {
      int i = (this.readUnsignedByte() << 8) + this.readUnsignedByte();
      if(i > 32767) {
         i -= 65536;
      }

      return i;
   }

   public int readUnsignedShort() {
      return (this.readUnsignedByte() << 8) + this.readUnsignedByte();
   }

   public int readInt() {
      return (this.readUnsignedByte() << 24) + (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 8) + this.readUnsignedByte();
   }

   public int read24BitInt() {
      return (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 8) + this.readUnsignedByte();
   }

   public int readBigSmart() {
      if(~this.buffer[this.offset] <= -1) {
         int value = this.readUnsignedShort();
         return value == 32767?-1:value;
      } else {
         return this.readInt() & Integer.MAX_VALUE;
      }
   }
}
