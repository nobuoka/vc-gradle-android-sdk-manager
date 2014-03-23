package info.vividcode.android.build.gradle.plugin.sdkmanager;

import java.io.IOException;

/**
 * Class which is read output of process and respond to it if necessary.
 */
interface ProcessUserAgent extends AutoCloseable {

    /**
     * Read output of process and respond to it if necessary.
     */
    void communicateSynchronously();

    /**
     * Close input stream and output streams of the process, and other
     * resources if necessary.
     */
    @Override
    void close() throws IOException;

    interface Factory {
        ProcessUserAgent createProcessUserAgent(Process process);
    }

}
