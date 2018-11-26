package com.xpyct707.numbers_searcher.generator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Slf4j
public class DataFileGenerator {
    private long fileSize = FileUtils.ONE_GB;

    public DataFileGenerator() {
        //Empty
    }

    public DataFileGenerator(long fileSize) {
        this.fileSize = fileSize;
    }

    public static void generateDataFiles(Path outputDirectory, int filesNumber) throws IOException {
        if (!Files.exists(outputDirectory)) {
            Files.createDirectory(outputDirectory);
            log.debug(String.format(Locale.UK, "Created directory '%s'", outputDirectory.toAbsolutePath()));
        }
        Runnable createDataFileTask = () -> {
            Path filePath = outputDirectory.resolve(ThreadLocalRandom.current().nextInt() + ".data");
            try {
                log.info(String.format(Locale.UK, "Generating file '%s'.", filePath));
                new DataFileGenerator().writeStreamToFile(ThreadLocalRandom.current().ints(), filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < filesNumber; i++) {
            executor.submit(createDataFileTask);
        }
        executor.shutdown();
    }

    private void writeStreamToFile(IntStream inputStream, Path destFile) throws IOException {
        final char comma = ',';
        log.debug(String.format(Locale.UK, "Begin to write file '%s'.", destFile));
        try (Writer writer = new LimitedOutputStreamWriter(
                Files.newOutputStream(destFile), StandardCharsets.UTF_8, fileSize)) {
            inputStream.mapToObj(String::valueOf).forEach(s -> {
                try {
                    writer.write(s);
                    writer.write(comma);
                } catch (IOException e) {
                    log.info(String.format(Locale.UK, "File '%s' was generated.", destFile));
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static final class LimitedOutputStreamWriter extends OutputStreamWriter {
        private final long maxBytes;
        private long       bytesWritten;

        LimitedOutputStreamWriter(OutputStream out, Charset cs, long maxBytes) {
            super(out, cs.newEncoder());
            this.maxBytes = maxBytes;
        }

        @Override
        public void write(String str) throws IOException {
            ensureCapacity(str.getBytes(StandardCharsets.UTF_8).length);
            super.write(str);
        }

        private void ensureCapacity(int len) throws IOException {
            long newBytesWritten = this.bytesWritten + len;
            if (newBytesWritten > this.maxBytes) {
                throw new IOException("File size exceeded: " + newBytesWritten + " > " + this.maxBytes);
            }
            this.bytesWritten = newBytesWritten;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 2) {
            printUsage();
            return;
        }
        if (args.length < 2) {
            Path defaultPath = Paths.get("data-files");
            int defaultFilesNumber = 20;
            log.info("Default parameters values will be used.");
            DataFileGenerator.generateDataFiles(defaultPath, defaultFilesNumber);
        } else {
            DataFileGenerator.generateDataFiles(Paths.get(args[0]), Integer.valueOf(args[1]));
        }
    }

    private static void printUsage() {
        System.out.println("Parameters:");
        System.out.println("<path to destination folder> <files number>");
        System.out.println("Defaults:");
        System.out.println("data-files 20");
    }
}
