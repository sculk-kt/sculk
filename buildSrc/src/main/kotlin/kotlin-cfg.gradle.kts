import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.kyori.indra.licenser.spotless.IndraSpotlessLicenserExtension

plugins {
    kotlin("jvm")
    id("io.github.goooler.shadow")
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/public/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test"))
}

val shade: Configuration by configurations.creating
configurations.implementation {
    extendsFrom(shade)
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Version" to project.version
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

plugins.apply("com.diffplug.spotless")
extensions.configure<SpotlessExtension> {
    val overrides = mapOf(
        "ktlint_standard_no-wildcard-imports" to "disabled",
        "ktlint_standard_filename" to "disabled",
        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
    )

    val ktlintVer = "1.3.1"

    kotlin {
        ktlint(ktlintVer).editorConfigOverride(overrides)
    }
    kotlinGradle {
        ktlint(ktlintVer).editorConfigOverride(overrides)
    }
}

plugins.apply("net.kyori.indra.licenser.spotless")
extensions.configure<IndraSpotlessLicenserExtension> {
    licenseHeaderFile(rootProject.file("NOTICE.txt"))
    newLine(true)
}

val shadowJar by tasks.existing(ShadowJar::class) {
    configurations = listOf(shade)
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "OSGI-INF/**", "*.profile", "module-info.class", "ant_tasks/**")
    mergeServiceFiles()

    archiveClassifier.set(null as String?)
}