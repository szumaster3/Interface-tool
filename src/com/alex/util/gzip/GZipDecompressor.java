package com.alex.util.gzip;

import com.alex.io.Stream;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class GZipDecompressor {
   private static final Inflater inflaterInstance = new Inflater(true);

   public static boolean decompress(Stream stream, byte[] data) {
      synchronized (inflaterInstance) {
         byte[] buffer = stream.getBuffer();
         int offset = stream.getOffset();

         if (buffer[offset] == 31 && buffer[offset + 1] == -117) {
            try {
               int length = buffer.length - offset - 18;
               inflaterInstance.setInput(buffer, offset + 10, length);
               inflaterInstance.inflate(data);

            } catch (DataFormatException e) {
               inflaterInstance.reset();
               return false;
            } catch (Exception e) {
               inflaterInstance.reset();
               return false;
            }

            inflaterInstance.reset();
            return true;
         } else {
            return false;
         }
      }
   }
}