package com.sparrow.bundle.framework.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class IOUtil {

    public static String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
            return baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
