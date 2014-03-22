package info.vividcode.android.build.gradle.plugin.sdkmanager;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class AndroidSdkManagerPluginTest {

    @Test
    public void applyPluginToProject() {
        Project project = ProjectBuilder.builder().build();
        HashMap<String, String> m = new HashMap<>();
        m.put("plugin", "vc-android-sdk-manager");
        project.apply(m);

        assertTrue("Plugin applyed to project without error", true);
    }

}
