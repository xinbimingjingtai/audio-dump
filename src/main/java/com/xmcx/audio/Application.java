package com.xmcx.audio;

import com.xmcx.audio.dump.DumperStrategy;
import com.xmcx.audio.dump.wrapper.FileWrapper;
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

/**
 * app
 */
public class Application {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            LoggerUtil.info("Usage: java [options] -jar <dump-executable.jar> <directory|absolute-path>" +
                            "%n  Note: if param contains whitespace character, use quotes surround with it" +
                            "%n  Options:%n%s%n%s%n%s%n%s%n%s",
                    DUMP_MODE, REMARK_TOGGLE, LOG_FILE_TOGGLE, LOG_FILE_DIRECTORY, LOG_FILENAME);
            return;
        }

        DumperStrategy dumperStrategy = new DumperStrategy();

        /* single file */
        File file = new File(args[0]);
        if (!file.isDirectory()) {
            FileWrapper fileWrapper = new FileWrapper(file);
            if (dumperStrategy.isSupported(fileWrapper)) {
                dumperStrategy.dump(fileWrapper);
            } else {
                LoggerUtil.warn("File '%s' is unsupported", fileWrapper.filename);
            }
            return;
        }

        /* multiple files */
        try (Stream<Path> paths = Files.walk(file.toPath()).filter(path -> !Files.isDirectory(path))) {
            Stream<FileWrapper> stream = paths.map(path -> new FileWrapper(path.toFile())).filter(dumperStrategy::isSupported);
            int counter, procNum;
            if (DUMP_MODE.isToggled() && (procNum = Runtime.getRuntime().availableProcessors()) > 1) {
                ExecutorService executorService = Executors.newFixedThreadPool(procNum << 1);
                Function<FileWrapper, Callable<Void>> mapper = fileWrapper -> () -> dumperStrategy.dump(fileWrapper);
                List<Callable<Void>> callables = stream.map(mapper).collect(Collectors.toList());
                counter = callables.size();
                executorService.invokeAll(callables);
                executorService.shutdown();
            } else {
                List<FileWrapper> list = stream.collect(Collectors.toList());
                for (FileWrapper fileWrapper : list) {
                    dumperStrategy.dump(fileWrapper);
                }
                counter = list.size();
            }
            // 转储目录完成，已转储10个文件
            LoggerUtil.info("Dump directory '%s' complete, %s files have been dumped", args[0], counter);
        } catch (Exception e) {
            LoggerUtil.warn("Directory '%s' is unavailable: '%s'", args[0], e.getMessage());
        }
    }

}
