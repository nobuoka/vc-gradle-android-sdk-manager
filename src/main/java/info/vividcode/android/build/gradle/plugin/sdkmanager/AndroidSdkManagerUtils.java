package info.vividcode.android.build.gradle.plugin.sdkmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.gradle.api.Project;

class AndroidSdkManagerUtils {

    /**
     * Candidates for the tools/android file in Android SDK.
     * "android" for Linux and OS X, "android.bat" for Windows.
     */
    private static final String[] SDK_ANDROID_FILE_NAME_CANDIDATES =
            { "android", "android.bat" };

    static Path findSdkToolsAndroidExecFile(Path sdkDir) {
        for (String fileName : SDK_ANDROID_FILE_NAME_CANDIDATES) {
            Path candidateFilePath = sdkDir.resolve("tools").resolve(fileName);
            if (Files.exists(candidateFilePath)) {
                return candidateFilePath;
            }
        }
        throw new RuntimeException("Executable file `android` not found");
    }

    private static String readLocalPropertiesValue(Project project, String key) {
        File localPropFile = project.getRootProject().file("local.properties");
        if (!localPropFile.exists()) {
            return "";
        }

        Properties properties = new Properties();
        try (BufferedReader reader =
                Files.newBufferedReader(localPropFile.toPath(), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            //e.printStackTrace();
            return "";
        }

        return properties.getProperty(key);
    }

    static Path findAndroidSdkDir(Project project) {
        String sdkDir;
        if ((sdkDir = readLocalPropertiesValue(project, "sdk.dir")) != "") {
            return new File(sdkDir).toPath();
        } else if ((sdkDir = System.getenv("ANDROID_HOME")) != null) {
            return new File(sdkDir).toPath();
        }
        throw new RuntimeException("Can't find Android SDK directory");
    }

}
