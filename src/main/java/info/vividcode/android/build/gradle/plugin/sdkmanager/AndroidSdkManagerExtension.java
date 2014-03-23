package info.vividcode.android.build.gradle.plugin.sdkmanager;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.gradle.api.Action;
import org.gradle.api.Project;

public class AndroidSdkManagerExtension {

    private final Project mProject;

    public AndroidSdkManagerExtension(Project project) {
        mProject = project;
    }

    public void updateSdkWithFilterWithAgreeingLicenseAutomatically(String filter) {
        ProcessUserAgent.Factory puaFactory =
                new AutomaticallyResponsingProcessUserAgent.Factory(
                        Pattern.compile("Do you accept the license .*"), "y");
        // Get tools/android command file in Android SDK.
        Path sdkDirPath = AndroidSdkManagerUtils.findAndroidSdkDir(mProject);
        Path execFilePath =
                AndroidSdkManagerUtils.findSdkToolsAndroidExecFile(sdkDirPath);
        // Execute `android update sdk ...` command.
        SdkAndroidCommandExecuter executer =
                new SdkAndroidCommandExecuter(execFilePath, puaFactory);
        executer.executeUpdateSdkCommandWithFilter(filter);
    }

    public void updateSdkPlatformAndBuildTools() {
        String compileSdkVersion =
                AndroidSdkManagerUtils.getAndroidCompileSdkVersion(mProject);
        String buildToolsRevision =
                AndroidSdkManagerUtils.getAndroidBuildToolsRevision(mProject);
        updateSdkWithFilterWithAgreeingLicenseAutomatically(
                compileSdkVersion + "," + "build-tools-" + buildToolsRevision);
    }

    public void updateSdkPlatformAndBuildToolsAfterEvaluate() {
        mProject.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                updateSdkPlatformAndBuildTools();
            }
        });
    }

}
