package com.nivixx.ndatabase.tests.promise;

import com.nivixx.ndatabase.api.Promise;
import com.nivixx.ndatabase.core.promise.AsyncThreadPool;
import com.nivixx.ndatabase.core.promise.pipeline.PromiseEmptyResultPipeline;
import com.nivixx.ndatabase.core.promise.pipeline.PromiseResultPipeline;
import com.nivixx.ndatabase.platforms.appplatform.AppDBLogger;
import com.nivixx.ndatabase.platforms.appplatform.AppSyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmptyResultPromiseTest {

    @Mock
    private Runnable noExceptionHandleCallbackMock;
    @Mock
    private Runnable noExceptionHandleCallbackMock2;

    @Mock
    private Consumer<Throwable> exceptionHandleCallbackMock;

    private CompletableFuture<Void> dbOperation;

    private DBLogger dbLogger;
    private SyncExecutor syncExecutor;
    private AsyncThreadPool asyncThreadPool;

    @Before
    public void init() {
        dbLogger = Mockito.mock(DBLogger.class);
        syncExecutor = new AppSyncExecutor();
        asyncThreadPool = new AsyncThreadPool(3);
        dbOperation = new CompletableFuture<>();
    }

    @Test
    public void operationSuccess_thenSync_callBackWithExceptionHandling() {
        Promise.AsyncEmptyResult promise = new PromiseEmptyResultPipeline<>(dbOperation, syncExecutor, asyncThreadPool, dbLogger);
        promise.thenSync(exceptionHandleCallbackMock);
        dbOperation.complete(null);
        awaitResult(() -> verify(exceptionHandleCallbackMock, times(1)).accept(any()));
    }

    @Test
    public void operationSuccess_thenAsync_callBackWithExceptionHandling() {
        Promise.AsyncEmptyResult promise = new PromiseEmptyResultPipeline<>(dbOperation, syncExecutor, asyncThreadPool, dbLogger);
        promise.thenAsync(exceptionHandleCallbackMock);
        dbOperation.complete(null);
        awaitResult(() -> verify(exceptionHandleCallbackMock, times(1)).accept(any()));
    }

    @Test
    public void operationSuccess_thenSync_callBackWithoutExceptionHandling() {
        Promise.AsyncEmptyResult promise = new PromiseEmptyResultPipeline<>(dbOperation, syncExecutor, asyncThreadPool, dbLogger);
        promise.thenSync(noExceptionHandleCallbackMock);
        dbOperation.complete(null);
        awaitResult(() -> verify(noExceptionHandleCallbackMock, times(1)).run());
    }

    @Test
    public void operationSuccess_thenAsync_callBackWithoutExceptionHandling() {
        Promise.AsyncEmptyResult promise = new PromiseEmptyResultPipeline<>(dbOperation, syncExecutor, asyncThreadPool, dbLogger);
        promise.thenAsync(noExceptionHandleCallbackMock);
        dbOperation.complete(null);
        awaitResult(() -> verify(noExceptionHandleCallbackMock, times(1)).run());
    }

    @Test
    public void operationException_thenSync_callBackWithExceptionHandling() {
        Promise.AsyncEmptyResult promise = new PromiseEmptyResultPipeline<>(dbOperation, syncExecutor, asyncThreadPool, dbLogger);
        promise.thenSync(exceptionHandleCallbackMock);
        dbOperation.completeExceptionally(new RuntimeException());
        awaitResult(() -> verify(exceptionHandleCallbackMock, times(1)).accept(any(Throwable.class)));
    }

    @Test
    public void operationException_thenASync_callBackWithExceptionHandling() {
        Promise.AsyncEmptyResult promise = new PromiseEmptyResultPipeline<>(dbOperation, syncExecutor, asyncThreadPool, dbLogger);
        promise.thenAsync(exceptionHandleCallbackMock);
        dbOperation.completeExceptionally(new RuntimeException());
        awaitResult(() -> verify(exceptionHandleCallbackMock, times(1)).accept(any(Throwable.class)));
    }

    @Test
    public void operationException_thenSync_callBackWithoutExceptionHandling() {
        Promise.AsyncEmptyResult promise = new PromiseEmptyResultPipeline<>(dbOperation, syncExecutor, asyncThreadPool, dbLogger);
        promise.thenSync(noExceptionHandleCallbackMock);
        dbOperation.completeExceptionally(new RuntimeException());
        awaitResult(() -> verifyZeroInteractions(noExceptionHandleCallbackMock));
    }

    @Test
    public void operationException_thenASync_callBackWithoutExceptionHandling() {
        Promise.AsyncEmptyResult promise = new PromiseEmptyResultPipeline<>(dbOperation, syncExecutor, asyncThreadPool, dbLogger);
        promise.thenAsync(noExceptionHandleCallbackMock);
        dbOperation.completeExceptionally(new RuntimeException());
        awaitResult(() -> verifyZeroInteractions(noExceptionHandleCallbackMock));
    }


    @Test
    public void callPromiseTwice_refuseSecondCall() {
        Promise.AsyncEmptyResult promise = new PromiseEmptyResultPipeline<>(dbOperation, syncExecutor, asyncThreadPool, dbLogger);
        promise.thenAsync(noExceptionHandleCallbackMock);
        promise.thenAsync(noExceptionHandleCallbackMock2);
        dbOperation.complete(null);
        awaitResult(() -> verify(noExceptionHandleCallbackMock, times(1)).run());
        awaitResult(() -> verifyZeroInteractions(noExceptionHandleCallbackMock2));
        awaitResult(() -> verify(dbLogger, times(1)).logWarn(any(String.class)));
    }

    private void awaitResult(Runnable runnable) {
        Awaitility.await()
                .pollDelay(10, TimeUnit.MILLISECONDS)
                .atMost(1000, TimeUnit.MILLISECONDS)
                .until(() -> {
                    try {
                        runnable.run();
                        return true;
                    } catch (AssertionError ae) {
                        ae.printStackTrace();
                        return false;
                    }
                });
    }
}
