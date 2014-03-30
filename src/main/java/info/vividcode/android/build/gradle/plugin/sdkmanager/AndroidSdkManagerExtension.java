package info.vividcode.android.build.gradle.plugin.sdkmanager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AndroidSdkManagerExtension {

    private final Project mProject;

    /** See: http://www.gradle.org/docs/current/userguide/logging.html */
    private final Logger mLogger = LoggerFactory.getLogger("vc-android-sdk-manager");

    public AndroidSdkManagerExtension(Project project) {
        mProject = project;
    }

    private SdkAndroidCommandExecuter createSdkAndroidCommandExecuter() {
        Path sdkDirPath = AndroidSdkManagerUtils.findAndroidSdkDir(mProject);
        Path execFilePath =
                AndroidSdkManagerUtils.findSdkToolsAndroidExecFile(sdkDirPath);
        return new SdkAndroidCommandExecuter(execFilePath);
    }

    private static String joinStrings(List<String> strs, char sep) {
        StringBuilder sb = new StringBuilder();
        if (strs.size() == 0) return "";
        boolean isFirst = true;
        for (String s : strs) {
            if (!isFirst) {
                sb.append(sep);
            } else {
                isFirst = false;
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private boolean getAcceptLicenseAutomaticallyValueFromOpts(Map<String, Object> opts) {
        final String ARG_ACCEPT_LICENSE_AUTOMATICALLY = "acceptLicenseAutomatically";
        if (!opts.containsKey(ARG_ACCEPT_LICENSE_AUTOMATICALLY)) {
            return false;
        } else {
            Object argVal = opts.get(ARG_ACCEPT_LICENSE_AUTOMATICALLY);
            if (argVal instanceof Boolean) {
                return (boolean) argVal;
            } else {
                throw new IllegalArgumentException(
                        "The type of value of named argument `" +
                        ARG_ACCEPT_LICENSE_AUTOMATICALLY + "` must be Boolean.");
            }
        }
    }

    public void updateSdkComponents(Map<String, Object> opts, List<String> targets) {
        SdkAndroidCommandExecuter executer = createSdkAndroidCommandExecuter();
        Set<String> names = executer.executeListSdkCommandAndGetAvailableComponentNames();
        List<String> tt = new ArrayList<>();
        for (String t : targets) {
            if (names.contains(t)) {
                tt.add(t);
            } else {
                mLogger.warn("Android SDK component `" + t + "` is already installed or not available.");
            }
        }
        boolean acceptLicenseAutomatically = getAcceptLicenseAutomaticallyValueFromOpts(opts);
        executeAndroidUpdateSdkCommand(executer, tt, acceptLicenseAutomatically);
    }

    private void executeAndroidUpdateSdkCommand(
            SdkAndroidCommandExecuter executer, List<String> targets,
            boolean acceptLicenseAutomatically) {
        if (targets.size() == 0) return;

        ProcessUserAgent.Factory puaFactory;
        if (acceptLicenseAutomatically) {
            puaFactory = new AutomaticallyResponsingProcessUserAgent.Factory(
                    Pattern.compile("Do you accept the license .*"), "y");
        } else {
            puaFactory = new ConsoleProxyProcessUserAgent.Factory();
        }
        if (targets.size() > 0) {
            String filter = joinStrings(targets, ',');
            executer.executeUpdateSdkCommandWithFilter(filter, puaFactory);
        }
    }

    public void updateSdkPlatformAndBuildTools(Map<String,Object> opts) {
        List<String> targets = new ArrayList<>();
        SdkAndroidCommandExecuter executer = createSdkAndroidCommandExecuter();
        Set<String> names = executer.executeListSdkCommandAndGetAvailableComponentNames();

        // SDK Platform
        String compileSdkVersion =
                AndroidSdkManagerUtils.getAndroidCompileSdkVersion(mProject);
        if (names.contains(compileSdkVersion)) {
            targets.add(compileSdkVersion);
        } else {
            mLogger.warn("SDK Platform `" + compileSdkVersion + "` is already installed or not available.");
        }
        // Build tools
        String buildToolsRevision =
                AndroidSdkManagerUtils.getAndroidBuildToolsRevision(mProject);
        String buildToolsFilterName = "build-tools-" + buildToolsRevision;
        if (!AndroidSdkManagerUtils.checkSpecifiedBuildToolsInstalled(mProject, buildToolsRevision)) {
            targets.add(buildToolsFilterName);
        } else {
            mLogger.warn("Build tools `" + buildToolsFilterName + "` is already installed.");
        }

        boolean acceptLicenseAutomatically = getAcceptLicenseAutomaticallyValueFromOpts(opts);
        executeAndroidUpdateSdkCommand(executer, targets, acceptLicenseAutomatically);
    }

    public void updateSdkPlatformAndBuildToolsAfterEvaluate(final Map<String,Object> opts) {
        mProject.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                updateSdkPlatformAndBuildTools(opts);
            }
        });
    }

}
