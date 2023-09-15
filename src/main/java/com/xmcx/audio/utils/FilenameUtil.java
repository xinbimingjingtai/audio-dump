package com.xmcx.audio.utils;

import java.io.File;
import java.util.function.UnaryOperator;

/**
 * Filename util
 */
public class FilenameUtil {

    private static final char separator = File.separatorChar;
    private static final char extension_separator = '.';
    private static final int not_found = -1;
    private static final String empty = "";

    public static String filename(String basename, String extension) {
        return filename(basename, extension, null);
    }

    public static String filename(String basename, String extension, UnaryOperator<String> basenameModifier) {
        if (basenameModifier != null) {
            basename = basenameModifier.apply(basename);
        }
        return basename + extension_separator + extension;
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf(separator);
        return filename.substring(index + 1);
    }

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf(extension_separator);
        return index == not_found ? empty : filename.substring(index + 1);
    }

    public static String getBasename(String filename) {
        if (filename == null) {
            return null;
        }
        String name = getName(filename);
        int index = name.lastIndexOf(extension_separator);
        return index == not_found ? name : name.substring(0, index);
    }

}
