package com.xmcx.audio.dump.wrapper;

import java.io.File;
import java.util.Locale;

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
        int index = this.filename.indexOf('.');
        if (index >= 0) {
            this.basename = this.filename.substring(0, index);
            this.extension = this.filename.substring(index + 1).toLowerCase(Locale.ENGLISH);
        } else {
            this.basename = this.filename;
            this.extension = null;
        }
    }

    protected FileWrapper(FileWrapper fileWrapper) {
        this.file = fileWrapper.file;
        this.filename = fileWrapper.filename;
        this.basename = fileWrapper.basename;
        this.extension = fileWrapper.extension;
    }
}
