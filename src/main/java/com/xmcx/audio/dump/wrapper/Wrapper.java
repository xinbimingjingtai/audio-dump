package com.xmcx.audio.dump.wrapper;

import com.xmcx.audio.dump.cloudmusic.CloudMusicMetadata;

import java.io.*;

public class Wrapper extends FileWrapper implements Closeable {

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

    public Wrapper(FileWrapper fileWrapper) throws FileNotFoundException {
        super(fileWrapper);
        this.fis = new FileInputStream(file);
    }

    @Override
    public void close() throws IOException {
        fis.close();
    }
}