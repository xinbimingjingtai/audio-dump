package com.xmcx.audio.dump;

import lombok.Getter;

/**
 * Option(s)
 */
@Getter
public final class Option {

    /**
     * Dump mode. The dump is sync by default, only 'ASYNC' means that the dump use async
     */
    public static final Option DUMP_MODE = new Option("dumpMode", "ASYNC", "The dump is sync by default, only 'ASYNC' means that the dump use async");

    /**
     * Remark toggle. The remark is enabled by default, only 'OFF' means that the remark is disabled
     */
    public static final Option REMARK_TOGGLE = new Option("remark", "OFF", "The remark is enabled by default, only 'OFF' means that the remark is disabled");

    /**
     * Log file toggle. The log is enabled by default, only 'OFF' means that the log is disabled
     */
    public static final Option LOG_FILE_TOGGLE = new Option("logFileToggle", "OFF", "The log is enabled by default, only 'OFF' means that the log is disabled");

    /**
     * Log file directory. Specify the log output directory
     */
    public static final Option LOG_FILE_DIRECTORY = new Option("logFileDirectory", "<log-file-directory>", "Specify the log output directory");

    /**
     * Log filename. Specify the log output filename
     */
    public static final Option LOG_FILENAME = new Option("logFileName", "<log-filename>", "Specify the log output filename");

    /**
     * key
     */
    private final String key;
    /**
     * value or explanation of value
     */
    private final String value;
    /**
     * explanation
     */
    private final String explanation;

    private final String option;

    /**
     * Does the {@code value} equals to {@code System.getProperty(key)}
     */
    private final boolean toggled;

    private Option(String key, String value, String explanation) {
        this.key = key;
        this.value = value;
        this.explanation = explanation;
        this.option = "-D" + key + "=" + value;
        this.toggled = value.equals(System.getProperty(key));
    }

    @Override
    public String toString() {
        return "\t" + option + System.lineSeparator() + "\t\t" + explanation;
    }
}