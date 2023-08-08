package com.xmcx.audio.utils;

import java.util.function.UnaryOperator;

/**
 * Filename util
 */
public class FilenameUtil {

    public static String filename(String basename, String extension) {
        return filename(basename, extension, null);
    }

    public static String filename(String basename, String extension, UnaryOperator<String> basenameModifier) {
        if (basenameModifier != null) {
            basename = basenameModifier.apply(basename);
        }
        return basename + '.' + extension;
    }

}
