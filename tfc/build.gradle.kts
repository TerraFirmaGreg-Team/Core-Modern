import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    java
    idea
    id("org.cadixdev.licenser") version "0.6.1"
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    id("org.spongepowered.mixin") version "0.7.+"
}

// Toolchain versions
val minecraftVersion: String = "1.20.1"
// Don't bump this unless completely necessary - this is the NeoForge + Forge compatible version
// In future we probably want to track NeoForge versions, especially post-1.20 breaking change window
val forgeVersion: String = "47.1.3"
val mixinVersion: String = "0.8.5"

// Dependency versions
val jeiVersion: String = "15.2.0.21"
val patchouliVersion: String = "1.20.1-81-FORGE"
val jadeVersion: String = "4614153"
val topVersion: String = "4629624"

val modId: String = "tfc"
val modVersion: String = "3.2.25"

// Optional dev-env properties
val mappingsChannel: String = project.findProperty("mappings_channel") as String? ?: "official"
val mappingsVersion: String = project.findProperty("mappings_version") as String? ?: minecraftVersion
val minifyResources: Boolean = project.findProperty("minify_resources") as Boolean? ?: true
val useAdvancedClassRedef: Boolean = project.findProperty("use_advanced_class_redefinition") as Boolean? ?: false

println("Using mappings $mappingsChannel / $mappingsVersion with version $modVersion")

base {
    archivesName.set("TerraFirmaCraft-Modern-Fork")
    group = "net.dries007.tfc"
    version = modVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://maven.blamejared.com") // Patchouli & JEI
    maven(url = "https://modmaven.dev") // Mirror for JEI
    maven(url = "https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
    flatDir {
        dirs("libs")
    }
}

dependencies {
    minecraft("net.minecraftforge", "forge", version = "$minecraftVersion-$forgeVersion")

    // JEI
    compileOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion"))
    compileOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion"))
    runtimeOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion"))

    // Patchouli
    // We need to compile against the full JAR, not just the API, because we do some egregious hacks.
    compileOnly(fg.deobf("vazkii.patchouli:Patchouli:$patchouliVersion"))
    runtimeOnly(fg.deobf("vazkii.patchouli:Patchouli:$patchouliVersion"))

    // Jade / The One Probe
    compileOnly(fg.deobf("curse.maven:jade-324717:${jadeVersion}"))
    compileOnly(fg.deobf("curse.maven:top-245211:${topVersion}"))

    // Only use Jade at runtime
    runtimeOnly(fg.deobf("curse.maven:jade-324717:${jadeVersion}"))
    // runtimeOnly(fg.deobf("curse.maven:top-245211:${topVersion}"))

    if (System.getProperty("idea.sync.active") != "true") {
        annotationProcessor("org.spongepowered:mixin:${mixinVersion}:processor")
    }

    // Cyanide
    // runtimeOnly(fg.deobf("curse.maven:cyanide-forge-541676:4584675"))

    // Misc
    //runtimeOnly(fg.deobf("curse.maven:konkrete-410295:4583492")) // Dep. for Panorama
    //runtimeOnly(fg.deobf("curse.maven:panoramica-426082:4019292"))
    //runtimeOnly(fg.deobf("curse.maven:embeddium-908741:4819807"))
    //runtimeOnly(fg.deobf("curse.maven:rubidium-574856:4767529"))
    // runtimeOnly(fg.deobf("curse.maven:create-328085:5838779"))
    //runtimeOnly(fg.deobf("curse.maven:corpse-316582:5157034"))
    //runtimeOnly(fg.deobf("curse.maven:distant-horizons-508933:5390046"))
    //runtimeOnly(fg.deobf("curse.maven:firmalife-453394:5456804"))

    // JUnit
    // There is not a testImplementation-like configuration, AFAIK, that is available at minecraft runtime, so we use minecraftLibrary
    minecraftLibrary("org.junit.jupiter:junit-jupiter-api:5.9.2")
    minecraftLibrary("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

minecraft {
    mappings(mappingsChannel, mappingsVersion)
    accessTransformer(file("src/resources/META-INF/accesstransformer.cfg"))

    runs {
        all {
            args("-mixin.config=$modId.mixins.json")

            property("forge.logging.console.level", "debug")
            property("forge.enabledGameTestNamespaces", modId)

            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "$projectDir/build/createSrgToMcp/output.srg")

            jvmArgs("-ea", "-Xmx4G", "-Xms4G")

            if (useAdvancedClassRedef) {
                jvmArg("-XX:+AllowEnhancedClassRedefinition")
            }

            ideaModule("${project.name}.test")

            mods.create(modId) {
                source(sourceSets.main.get())
                source(sourceSets.test.get())
            }
        }

        register("client") {
            workingDirectory(project.file("run/client"))
        }

        register("server") {
            workingDirectory(project.file("run/server"))

            arg("--nogui")
        }

        register("gameTestServer") {
            workingDirectory(project.file("run/gametest"))

            arg("--nogui")
        }

        register("data") {
            workingDirectory(project.file("run/data"))
            args("--mod", modId, "--all", "--output", file("src/generated/resources/"), "--existing", file("src/resources/"))
        }
    }
}

sourceSets.main {
    resources {
        srcDir("src/generated/resources/")
    }
}

// Automatically apply a license header when running checkLicense / updateLicense
license {
    header(project.file("HEADER.txt"))

    include("**/*.java")
    exclude("net/dries007/tfc/world/noise/FastNoiseLite.java") // Fast Noise
}

mixin {
    add(sourceSets.main.get(), "$modId.refmap.json")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("failed")

            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    jar {
        manifest {
            attributes["Implementation-Version"] = project.version
            attributes["MixinConfigs"] = "$modId.mixins.json"
        }
    }
}

