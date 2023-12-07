package com.xmcx.audio.dump.wrapper;

import com.xmcx.audio.utils.FilenameUtil;

import java.io.File;

/**
 * original file wrapper
 */
public class FileWrapper {

    /**
     * file
     */
    public final File file;

    /**
     * filename
     */
    public final String filename;

    /**
     * basename
     */
    public final String basename;

    /**
     * extension(lower case)
     */
    public final String extension;

    public FileWrapper(File file) {
        this.file = file;
        this.filename = file.getName();
        this.basename = FilenameUtil.getBasename(this.filename);
        this.extension = FilenameUtil.getExtension(this.filename);
    }

    protected FileWrapper(FileWrapper fileWrapper) {
        this.file = fileWrapper.file;
        this.filename = fileWrapper.filename;
        this.basename = fileWrapper.basename;
        this.extension = fileWrapper.extension;
    }
}
