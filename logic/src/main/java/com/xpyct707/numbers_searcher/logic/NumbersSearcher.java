package com.xpyct707.numbers_searcher.logic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class NumbersSearcher {
    private long bufferSize = FileUtils.ONE_MB;

    public NumbersSearcher() {}

    public NumbersSearcher(long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isFileContainsNumber(Path filePath, int number) throws IOException {
        //FIXME Handle splitted edges correctly
        validateFilePath(filePath);
        try (FileChannel fileChannel = FileChannel.open(filePath)) {
            log.debug(String.format("Start to scan file '%s'.", filePath));
            while (isFileUnread(fileChannel)) {
                log.debug(String.format("Loading next region of file '%s'.", filePath));
                CharBuffer charBuffer = loadNextFileRegionToBuffer(fileChannel, bufferSize);
                if (isStreamContainsNumber(Stream.of(charBuffer.toString()), number)) {
                    log.info(String.format(Locale.UK, "File '%s' contains number '%d'.", filePath, number));
                    return true;
                }
            }
            log.info(String.format(Locale.UK, "File '%s' doesn't contain number '%d'.", filePath, number));
            return false;
        }
    }

    private void validateFilePath(Path filePath) {
        if (Files.notExists(filePath)) {
            String message = String.format(Locale.UK, "File '%s' doesn't exist.", filePath);
            log.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isFileUnread(FileChannel inputChannel) throws IOException {
        return inputChannel.position() != inputChannel.size();
    }

    private CharBuffer loadNextFileRegionToBuffer(FileChannel inputChannel, long readSize) throws IOException {
        long currentPosition = inputChannel.position();
        readSize = checkAndCorrectReadSize(currentPosition, readSize, inputChannel.size());
        MappedByteBuffer mappedByteBuffer = inputChannel.map(FileChannel.MapMode.READ_ONLY, currentPosition, readSize);
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
