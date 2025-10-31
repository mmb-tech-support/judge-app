import com.github.spotbugs.snom.SpotBugsTask

plugins {
    id("com.android.application")
    id("checkstyle")
    id("pmd")
    id("com.github.spotbugs") version "6.4.4"
}

android {
    compileSdk = 36
    namespace = "ru.mmb.sportiduinomanager"

    defaultConfig {
        applicationId = "ru.mmb.sportiduinomanager"
        minSdk = 21
        targetSdk = 36
        androidResources {
            localeFilters += listOf("ru", "en")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    signingConfigs {
        // TODO: Configure signing
        // release {
        //     storeFile file(SPORTIDUINO_STORE_FILE)
        //     storePassword SPORTIDUINO_STORE_PASSWORD
        //     keyAlias SPORTIDUINO_KEY_ALIAS
        //     keyPassword SPORTIDUINO_KEY_PASSWORD
        // }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-Xlint:-options") // suppress specific warnings
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    buildFeatures {
        buildConfig = true
    }

    lint {
        disable.add("UsingOnClickInXml")
    }

}

// Checkstyle
checkstyle {
    toolVersion = "10.25.0"
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
}
tasks.register<Checkstyle>("checkstyle") {
    group = "verification"
    source("src")
    include("**/*.java")
    classpath = files()
}
tasks.named("check") {
    dependsOn("checkstyle")
}

// Pmd
pmd {
    toolVersion = "7.14.0"
    ruleSets = emptyList()
    ruleSetFiles = files("$rootDir/config/pmd/rules-pmd.xml")
}

tasks.register<Pmd>("pmd") {
    group = "verification"
    source("src")
    include("**/*.java")
}

tasks.named("check") {
    dependsOn("pmd")
}

// Spotbugs
spotbugs {
    toolVersion.set("4.9.3")
    excludeFilter.set(file("$rootDir/config/spotbugs/spotbugs-exclude.xml"))
    reportsDir.set(file("$rootDir/app/build/reports/spotbugs"))
}

tasks.withType<SpotBugsTask>().configureEach {
    reports {
        create("xml") {
            required.set(true)
        }
        create("html") {
            required.set(true)
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    spotbugsPlugins(libs.findsecbugs.plugin)

    implementation(libs.acra.core)
    implementation(libs.acra.http)
    implementation(libs.acra.toast)
}