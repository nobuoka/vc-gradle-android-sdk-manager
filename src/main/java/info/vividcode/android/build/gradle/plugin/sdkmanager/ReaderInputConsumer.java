package info.vividcode.android.build.gradle.plugin.sdkmanager;

import java.io.IOException;
import java.io.Reader;

class ReaderInputConsumer {

    private final Reader mReader;
    private final Callback mCallback;

    interface Callback {
        void onOutputReceived(String str);
        void onLineEnded();
    }

    ReaderInputConsumer(Reader reader, Callback callback) {
        mReader = reader;
        mCallback = callback;
    }

    private void callOnOutputReceived(String outputStr) {
        mCallback.onOutputReceived(outputStr);
    }

    private void callOnLineEnded() {
        mCallback.onLineEnded();
    }

    void consumeSynchronously() throws IOException {
        char[] chars = new char[2048];
        StringBuilder sb = new StringBuilder();
        int length = 0;
        while ((length = mReader.read(chars)) >= 0) {
            sb.append(chars, 0, length);
            outputEndedLines(sb);
            if (!mReader.ready()) {
                String out = sb.toString();
                sb.setLength(0);
                callOnOutputReceived(out);
                if (out.length() != 0 && out.charAt(out.length() - 1) == '\r') {
                    callOnLineEnded();
                }
            }
        }
    }

    private void outputEndedLines(StringBuilder sb) {
        while (true) {
            int idxLf = sb.indexOf("\n");
            int idxCr = sb.indexOf("\r");
            if (idxLf < 0 && idxCr < 0) break;
            if (idxCr + 1 == sb.length() && idxLf < 0) break;

            int sepIdx, nextIdx;
            if (idxLf >= 0 && idxCr >= 0) {
                if (idxLf + 1 == idxCr) {
                    sepIdx = idxCr;
                    nextIdx = idxCr + 1;
                } else {
                    sepIdx = nextIdx = Math.min(idxLf, idxCr) + 1;
                }
            } else {
                sepIdx = nextIdx = Math.max(idxLf, idxCr) + 1;
            }
            String line = sb.substring(0, sepIdx - 1) + "\n";
            sb.delete(0, nextIdx);
            callOnOutputReceived(line);
            callOnLineEnded();
        }
    }

}
