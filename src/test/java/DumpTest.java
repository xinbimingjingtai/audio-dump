import com.xmcx.audio.dump.cloudmusic.CloudMusicDumper;
import com.xmcx.audio.dump.wrapper.FileWrapper;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * Created on 2023.09.07T16:44
 *
 * @author xiaolu
 */
public class DumpTest {

    @Test
    public void test() {
        String path = "";
        new CloudMusicDumper().dump(new FileWrapper(new File(path)));
    }

}
