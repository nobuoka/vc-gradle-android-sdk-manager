package info.vividcode.android.build.gradle.plugin.sdkmanager;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Executer for the tools/android command in Android SDK.
 */
class SdkAndroidCommandExecuter {

    /** File path of the tools/android command. */
    private final String mExecFilePath;

    private final ProcessUserAgent.Factory mProcessUAFactory;

    public SdkAndroidCommandExecuter(
            Path execFilePath, ProcessUserAgent.Factory processUAFactory) {
        mExecFilePath = execFilePath.toFile().getAbsolutePath();
        mProcessUAFactory = processUAFactory;
    }

    Process executeCommand(String[] cmd) throws IOException {
        return Runtime.getRuntime().exec(cmd);
    }

    public void executeUpdateSdkCommandWithFilter(String filter) {
        String[] cmd = new String[] {
            mExecFilePath,
            "update", "sdk",
            "--no-ui",
            "--all",
            "--filter", filter,
        };
        final Process updateSdkProc;
        try {
            updateSdkProc = executeCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        try (final ProcessUserAgent pua = mProcessUAFactory.createProcessUserAgent(updateSdkProc)) {
            Thread t = new Thread() {
                @Override public void run() {
                    pua.communicateSynchronously();
                }
            };
            t.start();
            try {
                updateSdkProc.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                t.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // TODO error handling
        } catch (IOException err) {
            err.printStackTrace();
            throw new RuntimeException(err);
        }
    }

}
