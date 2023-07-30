package com.xmcx.audio.dump.cloudmusic;

/**
 * RC4
 * <p>
 * This is not a standard implementation, and Cipher or third-party toolkits cannot be used.
 * If the {@code key} is the same, it can be reused.
 */
public class CloudMusicRC4 {

    private static final int BOX_LENGTH = 0x100;

    private final byte[] box;

    public CloudMusicRC4(byte[] key) {
        byte[] tmp = new byte[BOX_LENGTH];
        for (int i = 0; i < BOX_LENGTH; i++) {
            tmp[i] = (byte) i;
        }

        for (int i = 0, j = 0; i < BOX_LENGTH; i++) {
            j = (j + tmp[i] + key[i % key.length]) & 0xff;
            byte swap = tmp[i];
            tmp[i] = tmp[j];
            tmp[j] = swap;
        }

        box = new byte[BOX_LENGTH];

        for (int i = 0; i < BOX_LENGTH; i++) {
            box[i] = tmp[(tmp[i] + tmp[(i + tmp[i]) & 0xff]) & 0xff];
        }
    }

    /**
     * encryption and decryption
     */
    public byte[] crypto(byte[] src) {
        byte[] dest = new byte[src.length];
        for (int j = 0; j < src.length; j++) {
            dest[j] = (byte) (src[j] ^ box[(j + 1) & 0xff]);
        }
        return dest;
    }

}
