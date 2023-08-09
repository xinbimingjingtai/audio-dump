package com.xmcx.audio.dump.cloudmusic;

import com.xmcx.audio.dump.AbstractDumper;
import com.xmcx.audio.dump.wrapper.Wrapper;
import com.xmcx.audio.utils.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * cloud music dumper
 */
public class CloudMusicDumper extends AbstractDumper {

    private final List<String> ncmExtensions = Collections.singletonList("ncm");
    private final List<String> supportedExtensions = new ArrayList<>();

    {
        supportedExtensions.addAll(ncmExtensions);
    }

    @Override
    public List<String> supportedExtensions() {
        return supportedExtensions;
    }

    /**
     * Dump ncm
     * <pre>
     *     1.  Verify this ncm magic is equals {@link CloudMusicKey#MAGIC}; no effect with dump
     *     2.  Skip unused 2 bytes
     *     3.  Read key
     *     4.  Read metadata
     *     5.  Read crc32
     *     6.  Skip unused 5 bytes
     *     7.  Read album image
     *     8.  Read music
     *     9.  Write music
     *     10. Fix tag
     * </pre>
     */
    @Override
    protected void doDump(Wrapper wrapper) {
        verifyMagic(wrapper);
        // unused 2 bytes
        IoUtil.skipN(wrapper.fis, 2);
        byte[] key = readKey(wrapper);
        wrapper.metadata = readMetadata(wrapper);
        readCRC32(wrapper);
        // unused 5 bytes
        IoUtil.skipN(wrapper.fis, 5);
        wrapper.metadata.setAlbumImage(readAlbumImage(wrapper));
        byte[] music = readMusic(wrapper, key);
        wrapper.musicFile = writeMusic(wrapper, wrapper.metadata, music);
    }

    /**
     * Verify this ncm magic is equals 'NcmKey.MAGIC'
     */
    private void verifyMagic(Wrapper wrapper) {
        byte[] magic = new byte[8];
        IoUtil.readBytes(wrapper.fis, magic);
        boolean verified = Arrays.equals(magic, CloudMusicKey.MAGIC);
        LoggerUtil.log(verified ? Level.INFO : Level.WARNING, "Verify '%s' magic: '%s'", wrapper.filename, verified ? "Correct" : "Incorrect");
    }

    /**
     * Read data length(stored in 4 bytes)
     */
    private int readBytesLength(Wrapper wrapper) {
        byte[] length = new byte[4];
        IoUtil.readBytes(wrapper.fis, length);
        return ByteBuffer.wrap(length).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get();
    }

    /**
     * Read data by length(4 bytes for data length + data)
     *
     * @see #readBytesLength(Wrapper)
     */
    private byte[] readBytesData(Wrapper wrapper, String dataName) {
        LoggerUtil.info("Read '%s' %s", wrapper.filename, dataName);
        int length = readBytesLength(wrapper);
        if (length < 0) {
            length = 0;
        }
        byte[] data = new byte[length];
        IoUtil.readBytes(wrapper.fis, data);
        return data;
    }

    /**
     * Read key
     */
    private byte[] readKey(Wrapper wrapper) {
        byte[] key = readBytesData(wrapper, "key");
        for (int i = 0; i < key.length; ++i) {
            key[i] ^= 0x64;
        }
        key = CodecUtil.aesEcbDecrypt(key, CloudMusicKey.CORE_KEY);
        // remove "neteasecloudmusic"
        key = Arrays.copyOfRange(key, 17, key.length);
        return key;
    }

    /**
     * Read metadata
     */
    private CloudMusicMetadata readMetadata(Wrapper wrapper) {
        byte[] metadata = readBytesData(wrapper, "metadata");
        for (int i = 0; i < metadata.length; ++i) {
            metadata[i] ^= 0x63;
        }

        String remark = new String(metadata, StandardCharsets.UTF_8);

        // remove "163 key(don't modify):"
        metadata = Arrays.copyOfRange(metadata, 22, metadata.length);
        // base64
        metadata = CodecUtil.base64Decrypt(metadata);
        // aes/ecb
        metadata = CodecUtil.aesEcbDecrypt(metadata, CloudMusicKey.META_KEY);
        // remove "music:"
        metadata = Arrays.copyOfRange(metadata, 6, metadata.length);

        CloudMusicMetadata value = JsonUtil.readObject(metadata, CloudMusicMetadata.class);
        value.setRemark(remark);
        return value;
    }

    /**
     * Read crc32
     */
    private void readCRC32(Wrapper wrapper) {
        LoggerUtil.info("Read '%s' crc32", wrapper.filename);
        readBytesLength(wrapper);
        // TODO 2023-06-16 verify crc32
    }

    /**
     * Read album image
     */
    private byte[] readAlbumImage(Wrapper wrapper) {
        return readBytesData(wrapper, "album image");
    }

    /**
     * Read music
     */
    private byte[] readMusic(Wrapper wrapper, byte[] key) {
        LoggerUtil.info("Read '%s' music", wrapper.filename);

        byte[] music = new byte[(int) wrapper.file.length()];
        int length = IoUtil.readBytes(wrapper.fis, music);
        music = Arrays.copyOf(music, length);
        return new CloudMusicRC4(key).crypto(music);
    }

    /**
     * Write music
     */
    private File writeMusic(Wrapper wrapper, CloudMusicMetadata metadata, byte[] music) {
        LoggerUtil.info("Write '%s' music", wrapper.filename);

        File musicFile = new File(wrapper.file.getParent(), FilenameUtil.filename(wrapper.basename, metadata.getFormat().toLowerCase()));
        IoUtil.writeBytes(musicFile, music);
        return musicFile;
    }

}
