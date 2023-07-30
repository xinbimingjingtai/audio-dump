package com.xmcx.audio.dump.qqmusic;

import com.xmcx.audio.dump.AbstractDumper;
import com.xmcx.audio.dump.Wrapper;
import com.xmcx.audio.utils.FilenameUtil;
import com.xmcx.audio.utils.IoUtil;
import com.xmcx.audio.utils.LoggerUtil;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * QQMusic dump
 * <p>
 * QMC3/QMC0/QMCFLAC
 */
public class QQMusicDumper extends AbstractDumper {

    private static final Pattern MP3_PATTERN = Pattern.compile(".*\\.(qmc3|qmc0)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FLAC_PATTERN = Pattern.compile(".*\\.qmcflac", Pattern.CASE_INSENSITIVE);

    private static final Pattern FILENAME_PATTERN = Pattern.compile(" *\\[(mqms\\d*)]", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isSupported(File file) {
        String filename = file.getName();
        return MP3_PATTERN.matcher(filename).matches() || FLAC_PATTERN.matcher(filename).matches();
    }

    @Override
    protected void doDump(Wrapper wrapper) {
        byte[] music = readMusic(wrapper);
        wrapper.musicFile = writeMusic(wrapper, music);
    }

    /**
     * Read music
     */
    private byte[] readMusic(Wrapper wrapper) {
        LoggerUtil.info("Read '%s' music", wrapper.filename);

        byte[] music = new byte[(int) wrapper.file.length()];
        int length = IoUtil.readBytes(wrapper.fis, music);
        music = Arrays.copyOf(music, length);
        return new QQMusicDecoder().decode(music);
    }

    /**
     * Write music
     */
    private File writeMusic(Wrapper wrapper, byte[] music) {
        LoggerUtil.info("Write '%s' music", wrapper.filename);

        String extension = FLAC_PATTERN.matcher(wrapper.filename).matches() ? "flac" : "mp3";
        File musicFile = new File(wrapper.file.getParent(),
                FilenameUtil.modifyExtension(wrapper.filename, extension, filename -> FILENAME_PATTERN.matcher(filename).replaceAll("")));
        IoUtil.writeBytes(musicFile, music);
        return musicFile;
    }

}
