package com.xmcx.audio.dump;

import com.xmcx.audio.dump.cloudmusic.CloudMusicMetadata;

import java.io.*;

public class Wrapper implements Closeable {
    /**
     * original file
     */
    public final File file;

    /**
     * original filename
     */
    public final String filename;

    /**
     * original stream
     */
    public final FileInputStream fis;

    /**
     * music file(dumped)
     */
    public File musicFile;

    /**
     * metadata
     */
    public CloudMusicMetadata metadata;

    public Wrapper(File file, String filename) throws FileNotFoundException {
        this.file = file;
        this.filename = filename;
        this.fis = new FileInputStream(file);
    }

    @Override
    public void close() throws IOException {
        fis.close();
    }
}