package com.xmcx.audio.dump;

import com.xmcx.audio.dump.cloudmusic.CloudMusicDumper;
import com.xmcx.audio.dump.qqmusic.QQMusicDumper;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Dumper chain
 */
public class DumperChain {

    private final List<AbstractDumper> dumpers = Arrays.asList(new CloudMusicDumper(), new QQMusicDumper());

    public boolean isSupported(File file) {
        for (AbstractDumper dumper : dumpers) {
            if (dumper.isSupported(file)) {
                return true;
            }
        }
        return false;
    }

    public Void dump(File file) {
        for (AbstractDumper dumper : dumpers) {
            if (dumper.isSupported(file)) {
                dumper.dump(file);
                return null;
            }
        }
        return null;
    }

}
