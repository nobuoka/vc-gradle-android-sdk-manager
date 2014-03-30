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
