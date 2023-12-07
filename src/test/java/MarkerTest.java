import com.xmcx.audio.dump.cloudmusic.CloudMusicKey;
import com.xmcx.audio.utils.CodecUtil;
import lombok.SneakyThrows;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created on 2023.10.19T16:34
 *
 * @author xiaolu
 */
public class MarkerTest {

    @Test
    public void testRemark() {
        testRemark("");
    }

    private void testRemark(String remark) {
        // remove "163 key(don't modify):"
        remark = remark.substring(22);
        byte[] data = remark.getBytes(StandardCharsets.UTF_8);
        data = CodecUtil.base64Decrypt(data);
        data = CodecUtil.aesEcbDecrypt(data, CloudMusicKey.META_KEY);
        // remove "music:"
        data = Arrays.copyOfRange(data, 6, data.length);
        System.out.println(new String(data, StandardCharsets.UTF_8));
    }

    @SneakyThrows
    @Test
    public void testFile() {
        String file = "/Users/xmcx/Music/网易云音乐/music/chinese/upgraded/莫文蔚 - 这世界那么多人.flac";
        AudioFile audio = AudioFileIO.read(new File(file));
        Tag tag = audio.getTag();
        String remark = getRemark(tag);
        testRemark(remark);
    }

    private String getRemark(Tag tag) {
        if (tag instanceof AbstractID3v2Tag) {
            return tag.getFirst(FieldKey.COMMENT);
        } else if (tag instanceof FlacTag) {
            return tag.getFirst(VorbisCommentFieldKey.DESCRIPTION.getFieldName());
        }
        return null;
    }

}
