/*
 * Copyright 2014 NOBUOKA Yu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.vividcode.android.build.gradle.plugin.sdkmanager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.vividcode.android.sdk.client.AndroidCommandExecutor;
import info.vividcode.android.sdk.client.AutoRespondingProcessIoHandler;
import info.vividcode.android.sdk.client.ConsoleProxyProcessIoHandler;
import info.vividcode.android.sdk.client.ProcessIoHandler;

public class AndroidSdkManagerExtension {

    private final Project mProject;

    /** See: http://www.gradle.org/docs/current/userguide/logging.html */
    private final Logger mLogger = LoggerFactory.getLogger("vc-android-sdk-manager");

    public AndroidSdkManagerExtension(Project project) {
        mProject = project;
    }

    private AndroidCommandExecutor createAndroidCommandExecutor() {
        Path sdkDirPath = AndroidSdkManagerUtils.findAndroidSdkDir(mProject);
        Path execFilePath =
                AndroidSdkManagerUtils.findSdkToolsAndroidExecFile(sdkDirPath);
        return new AndroidCommandExecutor(execFilePath);
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

    public void updateSdkComponents(List<String> targets) {
        updateSdkComponents(Collections.<String,Object>emptyMap(), targets);
    }

    public void updateSdkComponents(Map<String, Object> opts, List<String> targets) {
        AndroidCommandExecutor executor = createAndroidCommandExecutor();
        Set<String> names = executor.listSdk();
        List<String> tt = new ArrayList<>();
        for (String t : targets) {
            if (names.contains(t)) {
                tt.add(t);
            } else {
                mLogger.warn("Android SDK component `" + t + "` is already installed or not available.");
            }
        }
        boolean acceptLicenseAutomatically = getAcceptLicenseAutomaticallyValueFromOpts(opts);
        executeAndroidUpdateSdkCommand(executor, tt, acceptLicenseAutomatically);
    }

    private void executeAndroidUpdateSdkCommand(
            AndroidCommandExecutor executor, List<String> targets,
            boolean acceptLicenseAutomatically) {
        if (targets.size() == 0) return;

        ProcessIoHandler.Factory f;
        if (acceptLicenseAutomatically) {
            f = new AutoRespondingProcessIoHandler.Factory(
                    Pattern.compile("Do you accept the license .*"), "y");
        } else {
            f = new ConsoleProxyProcessIoHandler.Factory();
        }
        if (targets.size() > 0) {
            String filter = joinStrings(targets, ',');
            try {
                executor.updateSdkWithFilter(filter, f);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void updateSdkPlatformAndBuildTools() {
        updateSdkPlatformAndBuildTools(Collections.<String,Object>emptyMap());
    }

    public void updateSdkPlatformAndBuildTools(Map<String,Object> opts) {
        List<String> targets = new ArrayList<>();
        AndroidCommandExecutor executor = createAndroidCommandExecutor();
        Set<String> names = executor.listSdk();

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
        executeAndroidUpdateSdkCommand(executor, targets, acceptLicenseAutomatically);
    }

    public void updateSdkPlatformAndBuildToolsAfterEvaluate() {
        updateSdkPlatformAndBuildToolsAfterEvaluate(Collections.<String,Object>emptyMap());
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
