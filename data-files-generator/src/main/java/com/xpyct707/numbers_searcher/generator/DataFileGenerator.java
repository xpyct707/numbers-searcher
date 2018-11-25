package com.xpyct707.numbers_searcher.generator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

//TODO Add logging
public class DataFileGenerator {
    public static void generateDataFiles(Path outputDirectory, int filesNumber) throws IOException {
        if (!Files.exists(outputDirectory)) {
            Files.createDirectory(outputDirectory);
        }
        Runnable createDataFileTask = () -> {
            try {
                new DataFileGenerator().writeStreamToFile(ThreadLocalRandom.current().ints(),
                        outputDirectory.resolve(ThreadLocalRandom.current().nextInt() + ".data"));
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
        final long size1Gb = 1073741824L;
        final char comma = ',';
        try (Writer writer = new LimitedOutputStreamWriter(
                Files.newOutputStream(destFile), StandardCharsets.UTF_8, size1Gb)) {
            inputStream.mapToObj(String::valueOf).forEach(s -> {
                try {
                    writer.write(s);
                    writer.write(comma);
                } catch (IOException e) {
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
        DataFileGenerator.generateDataFiles(Paths.get("test-data"), 14);
    }
}
