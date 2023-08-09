package com.xmcx.audio.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * IO util
 */
public class IoUtil {

    /**
     * Indicates the network is connectable.
     * Default is {@code true}, avoid misjudgments due to delayed results of checking connections
     */
    private static volatile boolean connectable = true;

    static {
        // daemon: terminate the timer when the main thread finished
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    // disconnect is unnecessary
                    HttpURLConnection conn = (HttpURLConnection) new URL("https://music.163.com").openConnection();
                    conn.setRequestMethod("HEAD");
                    connectable = conn.getResponseCode() == HttpURLConnection.HTTP_OK;
                } catch (IOException e) {
                    connectable = false;
                }
            }
        };
        timer.schedule(timerTask, 0, 3000);
    }

    /**
     * Read bytes
     */
    public static int readBytes(FileInputStream fis, byte[] b) {
        try {
            return fis.read(b);
        } catch (IOException e) {
            // should not happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Skip n bytes
     */
    public static void skipN(FileInputStream fis, long n) {
        try {
            fis.skip(n);
        } catch (IOException e) {
            // should not happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Write bytes to file
     */
    public static void writeBytes(File dest, byte[] data) {
        if (dest.exists()) {
            dest.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(data);
        } catch (IOException e) {
            // should not happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Read bytes from url
     */
    public static byte[] readUrl(String url) {
        if (!connectable) {
            LoggerUtil.info("readUrl");
            return new byte[0];
        }
        int times = 2;
        while (true) {
            try (InputStream is = new BufferedInputStream(new URL(url).openStream());
                 ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                //BufferedInputStream.DEFAULT_BUFFER_SIZE
                byte[] bytes = new byte[8192];
                int l;
                while ((l = is.read(bytes)) > 0) {
                    os.write(bytes, 0, l);
                }
                return os.toByteArray();
            } catch (Exception e) {
                if (times-- > 0) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    continue;
                }
                // should not happen
                throw new RuntimeException(e);
            }
        }
    }

}
