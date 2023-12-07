import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created on 2023.09.15T11:10
 *
 * @author xiaolu
 */
public class LryTest {

    @Test
    public void test() {
        String path="/Users/xmcx/files/Lyric";
        File dir = new File(path);
        for (File file : dir.listFiles()) {
            try {
                if (Files.readAllLines(file.toPath(), StandardCharsets.UTF_8).stream().anyMatch(e->e.contains("khuli"))) {
                    System.out.println(file.getName());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
