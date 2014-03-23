package info.vividcode.android.build.gradle.plugin.sdkmanager;

import java.util.HashMap;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;

public class AndroidSdkManagerPluginTest {

    @Test
    public void applyPluginToProject() {
        Project project = ProjectBuilder.builder().build();
        HashMap<String, String> m = new HashMap<>();
        m.put("plugin", "vc-android-sdk-manager");
        project.apply(m);

        Assert.assertTrue("Plugin applyed to project without error", true);

        Object ext = project.getExtensions().getByName("androidSdkManager");
        Assert.assertNotNull("`extension.androidSdkManager` is set", ext);
    }

}
