package info.vividcode.android.build.gradle.plugin.sdkmanager;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AndroidSdkManagerPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create(
                "androidSdkManager", AndroidSdkManagerExtension.class, project);
    }

}
