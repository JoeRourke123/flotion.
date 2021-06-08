plugins {
    kotlin("multiplatform") version "1.5.0"
}

group = "space.flotion.tools"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable("flotion-build") {
                entryPoint = "main"
            }
        }
    }
    sourceSets {
        all {
        	dependencies {
        		implementation("com.github.ajalt.clikt:clikt:3.1.0")
			}
		}
    }
}
