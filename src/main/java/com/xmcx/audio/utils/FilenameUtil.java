package com.xmcx.audio.utils;

import java.util.function.UnaryOperator;

/**
 * Filename util
 */
public class FilenameUtil {

    public static String modifyExtension(String filename, String extension) {
        return modifyExtension(filename, extension, null);
    }

    public static String modifyExtension(String filename, String extension, UnaryOperator<String> filenameModifier) {
        if (filenameModifier != null) {
            filename = filenameModifier.apply(filename);
        }
        int index = filename.lastIndexOf('.');
        return index < 0 ? (filename + "." + extension) : (filename.substring(0, index + 1) + extension);
    }

}
