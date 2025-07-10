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
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

evaluationDependsOn(":wear:watchface")

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    id("kotlinx-serialization")
}

// Watch Face Push requires API level 36 and above.
android {
    namespace = rootProject.property("appNamespace").toString()
    compileSdk = 36

    defaultConfig {
        applicationId = rootProject.property("appNamespace").toString()
        minSdk = 36
        targetSdk = 36
        versionCode = rootProject.property("appVersionCode").toString().toInt()
        versionName = rootProject.property("appVersionName").toString()
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("release") {
            assets.srcDirs(layout.buildDirectory.dir("intermediates/watchfaceAssets/release"))
            res.srcDirs(layout.buildDirectory.file("generated/wfTokenRes/release/res/"))
        }
        getByName("debug") {
            assets.srcDirs(layout.buildDirectory.dir("intermediates/watchfaceAssets/debug"))
            res.srcDirs(layout.buildDirectory.file("generated/wfTokenRes/debug/res/"))
        }
    }
}

configurations {
    create("cliToolConfiguration") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.accompanist.permissions)
    implementation(libs.ui)
    implementation(libs.wear.compose.ui.tooling)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.compose.navigation)
    implementation(libs.horologist.compose.layout)
    implementation(libs.material.icons.core)
    implementation(libs.material.icons.extended)
    implementation(libs.watchface.push)
    implementation(libs.core.ktx)
    implementation(libs.watchface.complications.data.source.ktx)
    implementation(libs.work.runtime.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.play.services.wearable)

    "cliToolConfiguration"(libs.validator.push.cli)

    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}

androidComponents.onVariants { variant ->
    val capsVariant = variant.name.replaceFirstChar { it.uppercase() }

    val copyTaskProvider = tasks.register<Copy>("copyWatchface${capsVariant}Output") {
        val wfTask = project(":wear:watchface").tasks.named("assemble$capsVariant")
        dependsOn(wfTask)
        val buildDir = project(":wear:watchface").layout.buildDirectory.asFileTree.matching {
            include("**/*.apk")
        }
        from(buildDir)
        into(layout.buildDirectory.dir("intermediates/watchfaceAssets/${variant.name}"))

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        eachFile {
            path = "default_watchface.apk"
        }
        includeEmptyDirs = false
    }

    val tokenTask = tasks.register<ProcessFilesTask>("generateToken${capsVariant}Res") {
        val tokenFile =
            layout.buildDirectory.file("generated/wfTokenRes/${variant.name}/res/values/wf_token.xml")

        inputFile.from(copyTaskProvider.map { it.outputs.files.singleFile })
        outputFile.set(tokenFile)
        cliToolClasspath.set(project.configurations["cliToolConfiguration"])
        rootPackage.set(rootProject.property("appNamespace").toString())
    }

    afterEvaluate {
        tasks.named("pre${capsVariant}Build").configure {
            dependsOn(tokenTask)
        }
    }
}

abstract class ProcessFilesTask : DefaultTask() {
    @get:InputFiles
    abstract val inputFile: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val rootPackage: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val cliToolClasspath: Property<FileCollection>

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun taskAction() {
        val apkFile = inputFile.singleFile.resolve("default_watchface.apk")

        val stdOut = ByteArrayOutputStream()
        val stdErr = ByteArrayOutputStream()

        execOperations.javaexec {
            classpath = cliToolClasspath.get()
            mainClass = "com.google.android.wearable.watchface.validator.cli.DwfValidation"

            args(
                "--apk_path=${apkFile.absolutePath}",
                "--package_name=${rootPackage.get()}",
            )
            standardOutput = stdOut
            errorOutput = stdErr
            isIgnoreExitValue = true
        }

        val outputAsText = stdOut.toString()
        val errorAsText = stdErr.toString()

        if (outputAsText.contains("Failed check")) {
            println(outputAsText)
            if (errorAsText.isNotEmpty()) {
                println(errorAsText)
            }
            throw GradleException("Watch face validation failed")
        }

        val match = Pattern.compile("generated token: (\\S+)").matcher(stdOut.toString())
        if (match.find()) {
            val token = match.group(1)
            val output = outputFile.get().asFile
            output.parentFile.mkdirs()
            val tokenResText = """<resources>
                         |    <string name="default_wf_token">$token</string>
                         |</resources>
                       """.trimMargin()
            output.writeText(tokenResText)
        } else {
            throw TaskExecutionException(
                this,
                GradleException("No token generated for watch face!"),
            )
        }
    }
}