package com.xmcx.audio;

import com.xmcx.audio.dump.DumperChain;
import com.xmcx.audio.utils.LoggerUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xmcx.audio.dump.Option.*;

public class Application {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            LoggerUtil.info("Usage: java [options] -jar <dump-executable.jar> <directory|absolute-path>" +
                            "%n  Note: if param contains whitespace character, use quotes surround with it" +
                            "%n  Options:%n%s%n%s%n%s%n%s%n%s",
                    DUMP_MODE, REMARK_TOGGLE, LOG_FILE_TOGGLE, LOG_FILE_DIRECTORY, LOG_FILENAME);
            return;
        }

        DumperChain dumperChain = new DumperChain();

        /* single ncm file */
        File file = new File(args[0]);
        if (!file.isDirectory()) {
            if (dumperChain.isSupported(file)) {
                dumperChain.dump(file);
            } else {
                LoggerUtil.warn("File '%s' is unsupported", file.getName());
            }
            return;
        }

        /* multiple ncm files */
        try (Stream<Path> stream = Files.walk(file.toPath()).filter(path -> !Files.isDirectory(path) && dumperChain.isSupported(path.toFile()))) {
            int counter;
            if (DUMP_MODE.isToggled()) {
                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() << 1);
                Function<Path, Callable<Void>> mapper = path -> () -> dumperChain.dump(path.toFile());
                List<Callable<Void>> callables = stream.map(mapper).collect(Collectors.toList());
                counter = callables.size();
                executorService.invokeAll(callables);
                executorService.shutdown();
            } else {
                List<Path> paths = stream.collect(Collectors.toList());
                for (Path path : paths) {
                    dumperChain.dump(path.toFile());
                }
                counter = paths.size();
            }
            // 转储目录完成，已转储10个文件
            LoggerUtil.info("Dump directory '%s' complete, %s files have been dumped", args[0], counter);
        } catch (Exception e) {
            LoggerUtil.warn("Directory '%s' is unavailable: '%s'", args[0], e.getMessage());
        }
    }

}
