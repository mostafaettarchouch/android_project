// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // If you use Hilt, ensure it's in libs.versions.toml as well
    id("com.google.dagger.hilt.android") version "2.50" apply false
}
