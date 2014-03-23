package info.vividcode.android.build.gradle.plugin.sdkmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.gradle.api.Project;

class AndroidSdkManagerUtils {

    static String getAndroidCompileSdkVersion(Project project) {
        return getPropertyFromAndroidExt(
                project, "getCompileSdkVersion", "android.compileSdkVersion");
    }

    static String getAndroidBuildToolsRevision(Project project) {
        return getPropertyFromAndroidExt(
                project, "getBuildToolsRevision", "android.buildToolsRevision");
    }

    // Build tools components don't appear available component list,
    // so check the directory existence.
    static boolean checkSpecifiedBuildToolsInstalled(
            Project project, String buildToolsRevision) {
        Path sdkDir = findAndroidSdkDir(project);
        Path platformDir = sdkDir.resolve("build-tools").resolve(buildToolsRevision);
        return platformDir.toFile().exists();
    }

    private static String getPropertyFromAndroidExt(
            Project project, String methodName, String propAccessForErrorMessage) {
        Object androidExt = project.getExtensions().getByName("android");
        if (androidExt == null) {
            throw new RuntimeException("There is no `android` extension.");
        }
        Object val;
        try {
            Method m = androidExt.getClass().getMethod(methodName);
            val = m.invoke(androidExt);
        } catch (NoSuchMethodException | SecurityException |
                IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e) {
            throw new RuntimeException("`" + propAccessForErrorMessage + "` couldn't be invoked", e);
        }
        return val.toString();
    }

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
