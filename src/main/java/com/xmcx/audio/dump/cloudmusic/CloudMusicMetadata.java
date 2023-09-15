package com.xmcx.audio.dump.cloudmusic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Metadata
 *
 * <pre>
 *   {
 *     "musicId": 150422,
 *     "musicName": "今天你要嫁给我",
 *     "artist": [
 *       [
 *         "蔡依林",
 *         7219
 *       ],
 *       [
 *         "陶喆",
 *         5196
 *       ]
 *     ],
 *     "albumId": 15184,
 *     "album": "太美丽",
 *     "albumPicDocId": "109951166916020363",
 *     "albumPic": "https://p3.music.126.net/Fq9QNt2SYKvPEL3ipfVc9g==/109951166916020363.jpg",
 *     "bitrate": 919920,
 *     "mp3DocId": "eb2adeb0ce417d3ef77a900b75436af6",
 *     "duration": 272066,
 *     "mvId": 5643648,
 *     "flag": 4,
 *     "alias": ["电影《我要我们在一起》主题曲"],
 *     "transNames": [
 *       "Marry Me Today"
 *     ],
 *     "format": "flac"
 *   }
 * </pre>
 */
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class CloudMusicMetadata {

    private Long musicId;
    @ToString.Include(rank = 2)
    private String musicName;
    private List<List<Object>> artist;
    private Long albumId;
    @ToString.Include(rank = 1)
    private String album;
    private String albumPicDocId;
    private String albumPic;
    @JsonIgnore
    private byte[] albumImage;
    private Long bitrate;
    private String mp3DocId;
    private Long duration;
    private Long mvId;
    private Long flag;
    private List<String> alias;
    private List<String> transNames;
    @ToString.Include
    private String format;

    /**
     * 163 key(don't modify):***
     */
    @JsonIgnore
    private String remark;
    // other fields

    @ToString.Include(rank = 3)
    public String[] artistsName() {
        return Optional.ofNullable(artist).orElse(Collections.emptyList())
                .stream().map(e -> (String) e.get(0)).toArray(String[]::new);
    }

}
