package info.vividcode.android.build.gradle.plugin.sdkmanager;

import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Class which is read output of process and respond automatically to it if necessary.
 */
public class AutomaticallyResponsingProcessUserAgent implements ProcessUserAgent {

    public static class Factory implements ProcessUserAgent.Factory {
        private final Pattern mPattern;
        private final String mResponse;

        /**
         * @param pattern Pattern of the output message to request user input.
         * @param response Fixed user input to use when user input is requested.
         */
        public Factory(Pattern pattern, String response) {
            mPattern = pattern;
            mResponse = response;
        }

        @Override
        public ProcessUserAgent createProcessUserAgent(Process process) {
            InputStream processStdOutput = process.getInputStream();
            InputStream processErrOutput = process.getErrorStream();
            OutputStream processInput = process.getOutputStream();
            return new AutomaticallyResponsingProcessUserAgent(
                    mPattern, mResponse,
                    processStdOutput, processErrOutput, processInput);
        }
    }

    final InputStreamReader mProcessStdOutput;
    final InputStreamReader mProcessErrOutput;
    final OutputStream mProcessInput;
    boolean mAlreadyStarted = false;

    private static abstract class AbstractConsumingTask implements Runnable, ReaderInputConsumer.Callback {
        private final Reader mIn;
        AbstractConsumingTask(Reader i) {
            mIn = i;
        }
        @Override
        public final void run() {
            try {
                new ReaderInputConsumer(mIn, this).consumeSynchronously();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        @Override
        public abstract void onOutputReceived(String str);
        @Override
        public void onLineEnded() {
            // do nothing
        }
    }

    private static class ProcStdoutConsumingTask extends AbstractConsumingTask {
        private final OutputStream mProcInput;
        private StringBuffer mCurrentLine = new StringBuffer();
        private final Pattern mRequestPattern;
        private final String mResponse;
        ProcStdoutConsumingTask(Pattern requestPattern, String response,
                Reader procStdout, OutputStream procInput) {
            super(procStdout);
            mRequestPattern = requestPattern;
            mResponse = response;
            mProcInput = procInput;
        }
        @Override
        public void onOutputReceived(String str) {
            System.out.print(str);
            System.out.flush();

            mCurrentLine.append(str);
            if (mRequestPattern.matcher(mCurrentLine).matches()) {
                try {
                    String response = mResponse + "\n";
                    mProcInput.write(response.getBytes(StandardCharsets.UTF_8));
                    mProcInput.flush();
                    System.out.print(response);
                    System.out.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void onLineEnded() {
            mCurrentLine.setLength(0);
        }
    }

    private static class ProcErroutConsumingTask extends AbstractConsumingTask {
        ProcErroutConsumingTask(Reader procStdout) {
            super(procStdout);
        }
        @Override
        public void onOutputReceived(String str) {
            System.out.print(str);
            System.out.flush();
        }
    }

    private final Pattern mRequestPattern;
    private final String mResponse;

    public AutomaticallyResponsingProcessUserAgent(
            Pattern requestPattern, String response,
            InputStream procStdOutput, InputStream procErrOutput, OutputStream procInput) {
        mRequestPattern = requestPattern;
        mResponse = response;
        mProcessStdOutput = new InputStreamReader(procStdOutput);
        mProcessErrOutput = new InputStreamReader(procErrOutput);
        mProcessInput = procInput;
    }

    private synchronized void checkIfFirstTimeOrThrow() {
        if (mAlreadyStarted) {
            throw new RuntimeException("`communicateSynchronously` method already called");
        }
        mAlreadyStarted = true;
    }

    private volatile Thread[] mThreads;

    @Override
    public void communicateSynchronously() {
        checkIfFirstTimeOrThrow();

        mThreads = new Thread[] {
            new Thread(new ProcStdoutConsumingTask(
                    mRequestPattern, mResponse, mProcessStdOutput, mProcessInput)),
            new Thread(new ProcErroutConsumingTask(mProcessErrOutput)),
        };
        for (Thread t : mThreads) t.start();
    }

    private void stopThreads() {
        for (Thread t : mThreads) t.interrupt();
        try {
            for (Thread t : mThreads) t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() throws IOException {
        stopThreads();
        mProcessStdOutput.close();
        mProcessErrOutput.close();
        mProcessInput.close();
    }

}
