package com.xmcx.audio.dump;

import com.xmcx.audio.dump.cloudmusic.CloudMusicKey;
import com.xmcx.audio.dump.cloudmusic.CloudMusicMetadata;
import com.xmcx.audio.dump.wrapper.FileWrapper;
import com.xmcx.audio.dump.wrapper.Wrapper;
import com.xmcx.audio.utils.CodecUtil;
import com.xmcx.audio.utils.IoUtil;
import com.xmcx.audio.utils.JsonUtil;
import com.xmcx.audio.utils.LoggerUtil;
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

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Abstract dumper
 */
public abstract class AbstractDumper {

    /**
     * The extensions(lower case) supported by this dumper
     */
    public abstract List<String> supportedExtensions();

    public final void dump(FileWrapper fileWrapper) {
        if (!supportedExtensions().contains(fileWrapper.extension)) {
            LoggerUtil.info("Dumper '%s' is unsupported the file '%s'", this.getClass().getSimpleName(), fileWrapper.filename);
            return;
        }
        LoggerUtil.info(">>> Dumping '%s' <<<", fileWrapper.filename);
        try (Wrapper wrapper = new Wrapper(fileWrapper)) {
            doDump(wrapper);
            fetchMetadata(wrapper);
            fixTag(wrapper);
            LoggerUtil.info(">>> Dumped '%s' <<<", fileWrapper.filename);
        } catch (Exception e) {
            LoggerUtil.warn("Dump '%s' fail: '%s', please check that the file format is correct", fileWrapper.filename, e.getMessage());
        }
    }

    /**
     * Do dump
     */
    protected abstract void doDump(Wrapper wrapper);

    /**
     * Fetch metadata
     */
    protected void fetchMetadata(Wrapper wrapper) {
        // cloud music is already fetched
        if (wrapper.metadata != null) {
            return;
        }
        // TODO 2023.07.31 fetch metadata from music.163.com
    }

    /**
     * Fix tag
     */
    protected void fixTag(Wrapper wrapper) {
        if (wrapper.metadata == null) {
            return;
        }
        LoggerUtil.info("Fix '%s' tag", wrapper.filename);

        try {
            AudioFile audioFile = AudioFileIO.getDefaultAudioFileIO().readFile(wrapper.musicFile);
            Tag tag = audioFile.getTag();
            deleteFields(tag);
            fixFields(wrapper, tag, wrapper.metadata);
            fixArtwork(wrapper, tag, wrapper.metadata);
            audioFile.setTag(tag);
            audioFile.commit();
        } catch (Exception e) {
            LoggerUtil.warn("Fix '%s' tag fail: '%s'", wrapper.filename, e.getMessage());
        }
    }

    /**
     * Delete fields
     * <p>
     * Can not delete field with iterator, otherwise occur {@code ConcurrentModificationException}
     */
    protected void deleteFields(Tag tag) {
        Set<String> fieldSet = new HashSet<>();
        Iterator<TagField> fields = tag.getFields();
        while (fields.hasNext()) {
            fieldSet.add(fields.next().getId());
        }
        fieldSet.forEach(tag::deleteField);
        tag.deleteArtworkField();
    }

    /**
     * Fix fields
     */
    @SneakyThrows
    protected void fixFields(Wrapper wrapper, Tag tag, CloudMusicMetadata metadata) {
        tag.setField(FieldKey.ALBUM, metadata.getAlbum());
        for (String artistName : metadata.artistsName()) {
            tag.addField(FieldKey.ARTIST, artistName);
        }
        tag.setField(FieldKey.TITLE, metadata.getMusicName());

        if (Option.REMARK_TOGGLE.isToggled()) {
            return;
        }
        String remark = metadata.getRemark();
        if (remark == null || remark.isEmpty()) {
            remark = JsonUtil.writeString(metadata);
            remark = "music:" + remark;
            remark = new String(CodecUtil.base64Encrypt(CodecUtil.aesEcbEncrypt(remark.getBytes(StandardCharsets.UTF_8), CloudMusicKey.META_KEY)), StandardCharsets.UTF_8);
            remark = "163 key(don't modify):" + remark;
        }
        if (tag instanceof AbstractID3v2Tag) {
            AbstractID3v2Tag id3v2Tag = (AbstractID3v2Tag) tag;
            AbstractID3v2Frame commentField = (AbstractID3v2Frame) id3v2Tag.createField(FieldKey.COMMENT, remark);
            commentField.getBody().setObjectValue(DataTypes.OBJ_LANGUAGE, Languages.MEDIA_MONKEY_ID);
            id3v2Tag.addField(commentField);
        } else if (tag instanceof FlacTag) {
            ((FlacTag) tag).setField(VorbisCommentFieldKey.DESCRIPTION.getFieldName(), remark);
        } else {
            // 不支持
            LoggerUtil.warn("Fix '%s' remark fail: unsupported format '%s'", wrapper.filename, metadata.getFormat());
        }
    }

    /**
     * Delete fields in tag
     */
    @SneakyThrows
    protected void fixArtwork(Wrapper wrapper, Tag tag, CloudMusicMetadata metadata) {
        byte[] albumImage = metadata.getAlbumImage();
        if (albumImage.length == 0) {
            try {
                albumImage = IoUtil.readUrl(metadata.getAlbumPic());
            } catch (Exception e) {
                LoggerUtil.warn("Fix '%s' tag fail: '%s', album pic: '%s'", wrapper.filename, e.getMessage(), metadata.getAlbumPic());
                return;
            }
        }
        if (albumImage.length == 0) {
            LoggerUtil.warn("Fix '%s' tag fail: no image data", wrapper.filename);
            return;
        }
        StandardArtwork artwork = new StandardArtwork();
        artwork.setBinaryData(albumImage);
        // or URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(albumImage))
        artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(albumImage));
        artwork.setPictureType(PictureTypes.DEFAULT_ID);
        tag.setField(artwork);
    }

}
