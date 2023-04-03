plugins {
    id("fabric-loom").version("1.1.8")
    id("maven-publish")
    id("io.freefair.lombok").version("6.6.2")
}

base {
    version = "$minecraft_version-fabric-$mod_version"
    group = maven_group
}

loom {
    mixin {
        defaultRefmapName.set("$mod_id.refmap.json")
    }
    //accessWidenerPath.set(file("src/main/resources/$mod_id.accesswidener"))

    runs.forEach { runSetting ->
        runSetting.property("mixin.debug.export=true")
        runSetting.property("mixin.dumpTargetOnFailure=true")
        runSetting.property("mixin.checks.interfaces=true")
        runSetting.property("mixin.hotSwap=true")
//        if (System.getProperty("java.vm.vendor").contains("JetBrains")) {
//            runSetting.vmArg("-XX:+AllowEnhancedClassRedefinition")
//            runSetting.vmArg("-XX:HotswapAgent=fatjar")
//        }
    }
}

repositories {
    maven("https://api.modrinth.com/maven")
    maven("https://maven.saps.dev/minecraft")
    maven("https://maven.architectury.dev")
    maven("https://maven.blamejared.com/")
    maven("https://maven.parchmentmc.org")
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$parchment_version@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:$loader_version")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    modImplementation("maven.modrinth:lazydfu:4SHylIO9")
    modImplementation("maven.modrinth:kiwi:MRPbB9Ra")
    annotationProcessor("maven.modrinth:kiwi:MRPbB9Ra")
    modImplementation("maven.modrinth:lychee:e98vdCGw")
    modImplementation("maven.modrinth:modmenu:gSoPJyVn")
    modImplementation("maven.modrinth:cloth-config:EXrxCjl6")
    modImplementation("maven.modrinth:jade:ZASePpsm")
    modImplementation("maven.modrinth:huge-structure-blocks:OsfLOdQa")

    modImplementation("dev.latvian.mods:kubejs-fabric:1902.6.0-build.119")
}

tasks.processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

val targetJavaVersion = 17
tasks.compileJava {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    base.archivesName.set(archives_base_name)
    withSourcesJar()
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("/data/pack")
    exclude("/data/loquat/dimension")
    exclude("/data/loquat/dimension_type")
    exclude("/data/loquat/worldgen/biome")
    from("LICENSE") {
        rename { "${it}_${archives_base_name}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components.getByName("java"))
        }
    }
    repositories {
    }
}
