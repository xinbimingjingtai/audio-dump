package com.xmcx.audio.dump.qqmusic;

import com.xmcx.audio.dump.AbstractDumper;
import com.xmcx.audio.dump.wrapper.Wrapper;
import com.xmcx.audio.utils.FilenameUtil;
import com.xmcx.audio.utils.IoUtil;
import com.xmcx.audio.utils.LoggerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * QQMusic dump
 * <p>
 * QMC3/QMC0/QMCFLAC
 */
public class QQMusicDumper extends AbstractDumper {

    private final List<String> mp3Extensions = Arrays.asList("qmc0", "qmc3");

    private final List<String> flacExtensions = Collections.singletonList("qmcflac");

    private final List<String> supportedExtensions = new ArrayList<>();

    {
        supportedExtensions.addAll(mp3Extensions);
        supportedExtensions.addAll(flacExtensions);
    }

    /**
     * avoid duplicate create {@code Pattern}
     */
    private final Pattern basenamePattern = Pattern.compile(" *\\[(mqms\\d*)]", Pattern.CASE_INSENSITIVE);

    private final UnaryOperator<String> basenameModifier = basename -> basenamePattern.matcher(basename).replaceAll("");

    @Override
    public List<String> supportedExtensions() {
        return supportedExtensions;
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

        String extension = flacExtensions.contains(wrapper.extension) ? "flac" : "mp3";

        File musicFile = new File(wrapper.file.getParent(), FilenameUtil.filename(wrapper.basename, extension, basenameModifier));
        IoUtil.writeBytes(musicFile, music);
        return musicFile;
    }

}
