package com.xmcx.audio.dump;

import com.xmcx.audio.dump.cloudmusic.CloudMusicDumper;
import com.xmcx.audio.dump.qqmusic.QQMusicDumper;
import com.xmcx.audio.dump.wrapper.FileWrapper;

import java.util.*;

/**
 * Dumper chain
 */
public class DumperStrategy {

    private final Map<String, AbstractDumper> dumperMap = new HashMap<>();

    private final List<String> supportedExtensions = new ArrayList<>();

    {
        Arrays.asList(new CloudMusicDumper(), new QQMusicDumper())
                .forEach(dumper -> {
                    dumper.supportedExtensions().forEach(ext -> {
                        dumperMap.put(ext, dumper);
                        supportedExtensions.add(ext);
                    });
                });
    }

    public boolean isSupported(FileWrapper fileWrapper) {
        return supportedExtensions.contains(fileWrapper.extension);
    }

    public Void dump(FileWrapper fileWrapper) {
        AbstractDumper dumper = dumperMap.get(fileWrapper.extension);
        if (dumper != null) {
            dumper.dump(fileWrapper);
        }
        return null;
    }

}
