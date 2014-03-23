package info.vividcode.android.build.gradle.plugin.sdkmanager.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Alternative to an external process.
 */
public abstract class AbstractDummyProcess {

    private final PipedInputStream mProcStdout;
    private final PipedInputStream mProcErrout;
    private final PipedOutputStream mProcInput;

    private Thread mProcThread;
    private final CountDownLatch mStartSignal = new CountDownLatch(1);
    private final AtomicReference<Throwable> mErrorInThread =
            new AtomicReference<>();

    public AbstractDummyProcess(
            PipedInputStream procStdout, PipedInputStream procErrout,
            PipedOutputStream procInput) {
        mProcStdout = procStdout;
        mProcErrout = procErrout;
        mProcInput = procInput;
    }

    public void start() throws IOException {
        mProcThread = new Thread() {
            @Override
            public void run() {
                runProcess();
            }
        };
        mProcThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                mErrorInThread.set(e);
            }
        });
        mProcThread.start();
        waitForProcessStart();
    }

    private void waitForProcessStart() {
        try {
            mStartSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void join() {
        try {
            mProcThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean errorOccured() {
        return (mErrorInThread.get() == null ? false : true);
    }

    private void runProcess() {
        try (
                final PipedOutputStream procStdoutSrc = new PipedOutputStream(mProcStdout);
                PipedOutputStream procErroutSrc = new PipedOutputStream(mProcErrout);
                final BufferedReader procInputDst =
                        new BufferedReader(new InputStreamReader(
                                new PipedInputStream(mProcInput), StandardCharsets.UTF_8))) {
            mStartSignal.countDown();
            process(procStdoutSrc, procErroutSrc, procInputDst);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void process(
            OutputStream procStdoutSrc, OutputStream procErroutSrc,
            BufferedReader procInputDst) throws IOException;

}
