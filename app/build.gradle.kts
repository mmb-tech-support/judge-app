import com.github.spotbugs.snom.SpotBugsTask

plugins {
    id("com.android.application")
    id("checkstyle")
    id("pmd")
    id("com.github.spotbugs") version "6.1.13"
}

android {
    compileSdk = 36
    namespace = "ru.mmb.sportiduinomanager"

    defaultConfig {
        applicationId = "ru.mmb.sportiduinomanager"
        minSdk = 21
        targetSdk = 36
        resourceConfigurations += listOf("ru", "en")
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
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
    }

    signingConfigs {
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
    reportsDir.set(file("$buildDir/reports/spotbugs"))
}

tasks.withType<SpotBugsTask>().configureEach {
    reports {
        getByName("xml").required.set(true)
        getByName("html").required.set(true)
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("com.google.android.material:material:1.13.0-beta01")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.14.0")

    val acraVersion = "5.12.0"
    implementation("ch.acra:acra-core:$acraVersion")
    implementation("ch.acra:acra-http:$acraVersion")
    implementation("ch.acra:acra-toast:$acraVersion")
}