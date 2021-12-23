package com.example.crawler.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {
    public static Map<String, String> unzip(String content) throws IOException, IllegalArgumentException {
        Map<String, String> res = new HashMap<String, String>();

        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder encoder = new BASE64Encoder();
        byte[] decodedBytes = decoder.decodeBuffer(content);


        String file_content = "";

        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(decodedBytes));
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            int needed = (int) entry.getSize();
            int read = 0;
            file_content = "";
            byte[] bytesIn = new byte[needed];
            while (needed > 0) {
                int pos = zis.read(bytesIn, read, needed);
                if (pos == -1) {
                    throw new IOException("Unexpected end of stream after " + pos + " bytes for entry " + entry.getName());
                }
                read += pos;
                needed -= pos;
            }
            /*file_content = encoder.encode(bytesIn);*/
            file_content = new String(bytesIn, "utf-8");

            res.put(entry.getName(), file_content);
            zis.closeEntry();
        }
        zis.close();
        return res;
    }
}
