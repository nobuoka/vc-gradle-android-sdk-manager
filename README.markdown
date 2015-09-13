Gradle plugin: vc-android-sdk-manager
==================================================

This plugin enables you to update Android SDK components in the Gradle build script.

## Usage (version 0.1.x)

```groovy
// Write the dependency of the build script.
buildscript {
    repositories {
        maven {
            url 'https://raw.githubusercontent.com/nobuoka/vc-gradle-android-sdk-manager/mvn-staging/m2repo/'
        }
    }
    dependencies {
        classpath 'info.vividcode.android.build:android-sdk-manager:0.1.+'
    }
}
```

```groovy
// Apply plugin.
apply plugin: 'vc-android-sdk-manager'
    // then, you can use `androidSdkManager`
// Update SDK Components with filters.
// You may have to do this before applying android plugin.
androidSdkManager.updateSdkComponents(['tools', 'platform-tools'])
// Update SDK Platform and Build tools after project is evaluated.
androidSdkManager.updateSdkPlatformAndBuildToolsAfterEvaluate()
```

If you'd like to make the plugin accept the license of Android SDK automatically,
use the `acceptLicenseAutomatically` option.
Please use this option in your responsibility.

```groovy
// Example of `acceptLicenseAutomatically: true` option.
androidSdkManager.updateSdkComponents(['tools', 'platform-tools'],
        acceptLicenseAutomatically: true)
androidSdkManager.updateSdkPlatformAndBuildToolsAfterEvaluate(
        acceptLicenseAutomatically: true)
```

## Projects with similar purpose

* [JakeWharton/sdk-manager-plugin](https://github.com/JakeWharton/sdk-manager-plugin)
* [cookpad/gradle-android-sdk-manager](https://github.com/cookpad/gradle-android-sdk-manager) (Deprecated)

## License

This project is released under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
The LICENSE.txt file is a copy of the License.
