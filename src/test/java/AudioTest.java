import com.fasterxml.jackson.core.type.TypeReference;
import com.xmcx.audio.dump.cloudmusic.CloudMusicKey;
import com.xmcx.audio.dump.cloudmusic.CloudMusicMetadata;
import com.xmcx.audio.utils.CodecUtil;
import com.xmcx.audio.utils.FilenameUtil;
import com.xmcx.audio.utils.JsonUtil;
import lombok.SneakyThrows;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.jaudiotagger.tag.reference.Languages;
import org.jaudiotagger.tag.reference.PictureTypes;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created on 2023.09.07T16:49
 *
 * @author xiaolu
 */
public class AudioTest {

    @SneakyThrows
    @Test
    public void clearMetadata() {
        String path = "/Users/xmcx/Downloads/Alexis Leon Smith - My Confession [mqms2].mp3";
        AudioFile audioFile = AudioFileIO.read(new File(path));
        Tag tag = audioFile.getTag();
        // deleteFields(tag);
        audioFile.commit();
    }

    @SneakyThrows
    @Test
    public void test() {
        String path = "/Users/xmcx/IdeaProjects/audio-dump-dev/src/main/resources/杨宗纬,叶蓓 - 我们好像在哪见过.flac";
        AudioFile audioFile = AudioFileIO.getDefaultAudioFileIO().readFile(new File(path));
        Tag tag = audioFile.getTag();
        System.out.println(tag);
    }

    @Test
    public void transfer() {
        String metaPath = "Lata Mangeshkar - Aankhein Khuli.mp3";
        String dataPath = "Udit Narayan,Lata Mangeshkar,Ishaan - Aankhen Khuli.flac";
        transfer(dataPath, metaPath);
    }

    @SneakyThrows
    private void transfer(String dataPath, String metaPath) {
        String parentPath = "/Users/xmcx/files";
        File dataFile = new File(parentPath + File.separator + dataPath);
        File metaFile = new File(parentPath + File.separator + metaPath);
        File mergeFile = new File(parentPath + File.separator + FilenameUtil.getBasename(metaPath) + "." + FilenameUtil.getExtension(dataPath));
        Files.copy(dataFile.toPath(), mergeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        AudioFileIO audioFileIO = AudioFileIO.getDefaultAudioFileIO();
        AudioFile dataAudio = audioFileIO.readFile(dataFile);
        AudioFile metaAudio = audioFileIO.readFile(metaFile);
        AudioFile mergeAudio = audioFileIO.readFile(mergeFile);

        Tag dataTag = dataAudio.getTag();
        Tag metaTag = metaAudio.getTag();
        Tag mergeTag = mergeAudio.getTag();

        deleteFields(mergeTag);
        fixArtwork(mergeTag, metaTag);
        fixFields(mergeTag, metaTag, dataTag);

        mergeAudio.setTag(mergeTag);
        mergeAudio.commit();
    }

    private void deleteFields(Tag tag) {
        Set<String> fieldSet = new HashSet<>();
        Iterator<TagField> fields = tag.getFields();
        while (fields.hasNext()) {
            fieldSet.add(fields.next().getId());
        }
        fieldSet.forEach(tag::deleteField);
        tag.deleteArtworkField();
    }

    @SneakyThrows
    private void fixFields(Tag mergeTag, Tag metaTag, Tag dataTag) {
        Map<String, Object> mergeMap = mergeMap(metaTag, dataTag);
        if (mergeMap == null) {
            return;
        }
        mergeTag.setField(FieldKey.ALBUM, (String) mergeMap.get("album"));
        mergeTag.setField(FieldKey.TITLE, (String) mergeMap.get("musicName"));
        // noinspection unchecked
        mergeTag.setField(FieldKey.ARTIST, new CloudMusicMetadata().setArtist((List<List<Object>>) mergeMap.get("artist")).artistsName());

        String mergeRemark = remark(mergeMap);
        if (mergeTag instanceof AbstractID3v2Tag) {
            AbstractID3v2Tag id3v2Tag = (AbstractID3v2Tag) mergeTag;
            AbstractID3v2Frame commentField = (AbstractID3v2Frame) id3v2Tag.createField(FieldKey.COMMENT, mergeRemark);
            commentField.getBody().setObjectValue(DataTypes.OBJ_LANGUAGE, Languages.MEDIA_MONKEY_ID);
            id3v2Tag.addField(commentField);
        } else if (mergeTag instanceof FlacTag) {
            ((FlacTag) mergeTag).setField(VorbisCommentFieldKey.DESCRIPTION.getFieldName(), mergeRemark);
        }
    }

    private Map<String, Object> mergeMap(Tag metaTag, Tag dataTag) {
        String metaRemark = getRemark(metaTag);
        String dataRemark = getRemark(dataTag);
        if (metaRemark == null || dataRemark == null) {
            return null;
        }
        Map<String, Object> metaMap = decryptRemark(metaRemark);
        Map<String, Object> dataMap = decryptRemark(dataRemark);

        Map<String, Object> mergeMap = new LinkedHashMap<>(metaMap);
        mergeMap.put("bitrate", dataMap.get("bitrate"));
        mergeMap.put("duration", dataMap.get("duration"));
        mergeMap.put("format", dataMap.get("format"));
        return mergeMap;
    }

    private String remark(Map<String, Object> map) {
        String remark = JsonUtil.writeString(map);
        remark = "music:" + remark;
        remark = new String(CodecUtil.base64Encrypt(CodecUtil.aesEcbEncrypt(remark.getBytes(StandardCharsets.UTF_8), CloudMusicKey.META_KEY)), StandardCharsets.UTF_8);
        return "163 key(don't modify):" + remark;
    }

    private String getRemark(Tag tag) {
        if (tag instanceof AbstractID3v2Tag) {
            return tag.getFirst(FieldKey.COMMENT);
        } else if (tag instanceof FlacTag) {
            return tag.getFirst(VorbisCommentFieldKey.DESCRIPTION.getFieldName());
        }
        return null;
    }

    private Map<String, Object> decryptRemark(String remark) {
        // remove "163 key(don't modify):"
        remark = remark.substring(22);
        byte[] data = remark.getBytes(StandardCharsets.UTF_8);
        data = CodecUtil.base64Decrypt(data);
        data = CodecUtil.aesEcbDecrypt(data, CloudMusicKey.META_KEY);
        // remove "music:"
        data = Arrays.copyOfRange(data, 6, data.length);
        return JsonUtil.readObject(data, new TypeReference<Map<String, Object>>() {
        });
    }

    @SneakyThrows
    private void fixField(Tag mergeTag, Tag metaTag, FieldKey field) {
        List<TagField> values = metaTag.getFields(field);
        if (values == null) {
            return;
        }
        mergeTag.setField(field, values.stream().map(e -> {
            try {
                return new String(e.getRawContent(), StandardCharsets.UTF_8);
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
        }).toArray(String[]::new));
    }

    @SneakyThrows
    private void fixArtwork(Tag mergeTag, Tag metaTag) {
        byte[] albumImage = metaTag.getFirstArtwork().getBinaryData();
        StandardArtwork artwork = new StandardArtwork();
        artwork.setBinaryData(albumImage);
        // or URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(albumImage))
        artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(albumImage));
        artwork.setPictureType(PictureTypes.DEFAULT_ID);
        mergeTag.setField(artwork);
    }
}
