/*
 * Copyright 2025 Google LLC
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

plugins {
    id("com.android.application")
}

val WATCH_FACE_PKG_EXT = ".watchfacepush.defaultwf"

android {
    namespace = rootProject.property("appNamespace").toString() + WATCH_FACE_PKG_EXT
    compileSdk = 36

    defaultConfig {
        applicationId = rootProject.property("appNamespace").toString() + WATCH_FACE_PKG_EXT
        minSdk = 33
        targetSdk = 33
        versionCode = rootProject.property("appVersionCode").toString().toInt()
        versionName = rootProject.property("appVersionName").toString()
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = false
        }
        debug {
            isMinifyEnabled = true
        }
    }
}

