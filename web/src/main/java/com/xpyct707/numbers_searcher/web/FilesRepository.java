package com.xpyct707.numbers_searcher.web;

import com.xpyct707.numbers_searcher.logic.NumbersSearcher;
import com.xpyct707.numbers_searcher.model.RequestHistory;
import com.xpyct707.numbers_searcher.repository.RequestHistoryRepository;
import com.xpyct707.numbers_searcher.web_service.Code;
import com.xpyct707.numbers_searcher.web_service.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FilesRepository {
    @Value("${data.file.dir:data-files}")
    private String dataFilesDirectory;

    @Value("${execution.timeout:15}")
    private long timeoutInMinutes;

    @Autowired
    private RequestHistoryRepository requestHistoryRepository;

    public static void main(String[] args) {
        FilesRepository filesRepository = new FilesRepository();
        filesRepository.dataFilesDirectory = "d:\\GIT\\NumbersSearcher\\test-data\\";
        filesRepository.findNumber(840118442).getFileNames().forEach(System.out::println);
    }

    public Result findNumber(Integer number) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Set<CompletableFuture<TaskExecutionResult>> tasks = createSearchingTasks(number, executor);

        try {
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).get(timeoutInMinutes, TimeUnit.MINUTES);
        }  catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException | TimeoutException e) {
            Result result = new Result();
            result.setCode(Code.ERROR);
            result.setError(e.getMessage());
            writeResultToDatabase(result, number);
            return result;
        } finally {
            executor.shutdown();
        }

        Result result = extractResultFromCompletedTasks(tasks);
        writeResultToDatabase(result, number);
        return result;
    }

    private Set<CompletableFuture<TaskExecutionResult>> createSearchingTasks(int number, Executor executor) {
        return getInputFilesPathsStream()
                .map(path -> createCheckFileContainsNumberTask(path, number, executor))
                .collect(Collectors.toSet());
    }

    private Stream<Path> getInputFilesPathsStream() {
        try {
            return Files.list(Paths.get(dataFilesDirectory))
                    .filter(path -> path.getFileName().toString().endsWith(".data"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<TaskExecutionResult> createCheckFileContainsNumberTask(
            Path filePath, int number, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new TaskExecutionResult(filePath, new NumbersSearcher().isFileContainsNumber(filePath, number));
            } catch (IOException e) {
                return new TaskExecutionResult(filePath, e);
            }
        }, executor);
    }

    private Result extractResultFromCompletedTasks(Set<CompletableFuture<TaskExecutionResult>> tasks) {
        Set<String> fileNames = tasks.stream()
                .map(extractFutureResult)
                .filter(taskExecutionResult -> taskExecutionResult.taskResult)
                .map(taskExecutionResult -> taskExecutionResult.inputFilePath.getFileName())
                .map(Path::toString)
                .collect(Collectors.toSet());
        Result result = new Result();
        result.getFileNames().addAll(fileNames);
        result.setCode(Code.OK);
        return result;
    }

    private void writeResultToDatabase(Result result, int number) {
        requestHistoryRepository.save(new RequestHistory(
                result.getCode(),
                number,
                String.join(",", result.getFileNames()),
                result.getError()));
    }

    private Function<CompletableFuture<TaskExecutionResult>, TaskExecutionResult> extractFutureResult = (future) -> {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    };

    private static class TaskExecutionResult {
        private Path inputFilePath;
        private Boolean taskResult;
        private Exception exception;

        TaskExecutionResult(Path inputFilePath, boolean taskResult) {
            this.inputFilePath = inputFilePath;
            this.taskResult = taskResult;
        }

        TaskExecutionResult(Path inputFilePath, Exception exception) {
            this.inputFilePath = inputFilePath;
            this.exception = exception;
        }
    }
}
