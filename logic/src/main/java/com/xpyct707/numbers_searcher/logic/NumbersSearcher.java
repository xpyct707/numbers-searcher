package com.xpyct707.numbers_searcher.logic;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO Abb input data validation
//TODO Optimize by using info of available memory size from JVM
//TODO Handle all exceptions properly
public class NumbersSearcher {
    public static void main(String[] args) throws IOException {
        Path inputFileDir = Paths.get("d:\\GIT\\NumbersSearcher\\test-data\\");
        int numberToSearch = 840118442;

        new NumbersSearcher().isFileContainsNumber(inputFileDir, numberToSearch);
    }

    private long bufferSize = FileUtils.ONE_MB;

    public NumbersSearcher() {}

    public NumbersSearcher(long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isFileContainsNumber(Path filePath, int number) throws IOException {
        //FIXME Handle splitted edges correctly
        try (FileChannel fileChannel = FileChannel.open(filePath)) {
            while (isFileUnread(fileChannel)) {
                CharBuffer charBuffer = loadNextFileRegionToBuffer(fileChannel, bufferSize);
                if (isStreamContainsNumber(Stream.of(charBuffer.toString()), number)) {
                    System.out.println(String.format(Locale.UK,
                            "File '%s' contains number '%d'.", filePath, number));
                    return true;
                }
            }
            System.out.println(String.format(Locale.UK,
                    "File '%s' doesn't contain number '%d'.", filePath, number));
            return false;
        }
    }

    private boolean isFileUnread(FileChannel inputChannel) throws IOException {
        return inputChannel.position() != inputChannel.size();
    }

    private CharBuffer loadNextFileRegionToBuffer(FileChannel inputChannel, long readSize) throws IOException {
        long currentPosition = inputChannel.position();
        readSize = checkAndCorrectReadSize(currentPosition, readSize, inputChannel.size());
        MappedByteBuffer mappedByteBuffer = inputChannel
                .map(FileChannel.MapMode.READ_ONLY, currentPosition, readSize);
        inputChannel.position(currentPosition + readSize);
        return StandardCharsets.UTF_8.decode(mappedByteBuffer);
    }

    private long checkAndCorrectReadSize(long currentPosition, long readSize, long fileSize) {
        return currentPosition + readSize > fileSize ? fileSize - currentPosition : readSize;
    }

    private boolean isStreamContainsNumber(Stream<String> inputStream, int number) {
        Pattern splitPattern = Pattern.compile(",");
        return inputStream.flatMap(splitPattern::splitAsStream).anyMatch(String.valueOf(number)::equals);
    }
}
