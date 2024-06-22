package top.azimkin

import java.io.File
import java.io.FileWriter
import java.util.*

fun main() {
    print("Enter Plugin name: ")
    val pluginName = readln()
    print("Enter group: ")
    val packageGroup = readln()
    print("Enter API version: ")
    val version = readln()
    println("Use paperweight? (Y/N): ")
    val usepw = readln()

    when (usepw.lowercase(Locale.getDefault())) {
        "y" -> createPluginProject(pluginName, packageGroup, version, true)
        "n" -> createPluginProject(pluginName, packageGroup, version, false)
    }
}

fun createPluginProject(pluginName: String, pluginPackage: String, version: String, useNMS: Boolean) {
    val rootFolder = File("../$pluginName")
    rootFolder.mkdir()
    createGradleFiles(rootFolder, pluginName, pluginPackage, version, useNMS)
    createMainFile(rootFolder, pluginName, pluginPackage)
    createPluginYml(rootFolder, pluginName, pluginPackage, version)

    println("DONE")
}

fun createMainFile(rootFolder: File, pluginName: String, pluginPackage: String) {
    print("Creating main file... ")
    var file = File(rootFolder, "src/main/kotlin")
    file.mkdirs()
    file = File(file, "$pluginName.kt")
    file.createNewFile()
    val writer = FileWriter(file)
    writer.write(
        "package $pluginPackage\n" +
                "\n" +
                "import org.bukkit.plugin.java.JavaPlugin\n" +
                "\n" +
                "class $pluginName : JavaPlugin() {\n" +
                "    companion object {\n" +
                "        lateinit var inst: $pluginName\n" +
                "    }\n" +
                "\n" +
                "    override fun onEnable() {\n" +
                "        inst = this\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    override fun onDisable() {\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "}\n"
    )

    writer.close()
    println("DONE")
}

fun createGradleFiles(
    rootFolder: File,
    pluginName: String,
    pluginPackage: String,
    apiVersion: String,
    useNMS: Boolean
) {
    println("Creating gradle files... ")
    var file = File(rootFolder, "build.gradle.kts")
    file.createNewFile()
    var writer = FileWriter(file)
    print("  build.gradle.kts ... ")
    writer.write(
        "plugins {\n" +
                "    kotlin(\"jvm\") version \"1.9.24\"\n" +
                "    java\n" +
                (if (useNMS) "    id(\"io.papermc.paperweight.userdev\") version \"1.5.11\"\n" else "") +
                "}\n" +
                "\n" +
                "group = \"$pluginPackage\"\n" +
                "version = \"0.1\"\n" +
                "\n" +
                "repositories {\n" +
                "    mavenCentral()\n" +
                (if (useNMS) "    gradlePluginPortal()\n" else "") +
                "    maven(\"https://repo.papermc.io/repository/maven-public/\")\n" +
                "}\n" +
                "\n" +
                "dependencies {\n" +
                "    implementation(fileTree(\"libs\") { include(\"*.jar\") })\n" +
                (if (useNMS) "    paperweight.paperDevBundle(\"$apiVersion-R0.1-SNAPSHOT\")\n"
                else "    compileOnly(\"io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT\")\n") +
                "}\n" +
                "\n" +
                "tasks.compileJava.configure {\n" +
                "    options.encoding = \"UTF-8\"\n" +
                "}\n" +
                "\n" +
                "tasks.processResources {\n" +
                "    val props = mapOf(\"version\" to project.version)\n" +
                "    inputs.properties(props)\n" +
                "    filteringCharset = \"UTF-8\"\n" +
                "    filesMatching(\"plugin.yml\") {\n" +
                "        expand(props)\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "tasks.register(\"moveToServer\") {\n" +
                "    dependsOn(\":build\")\n" +
                "    val serverPluginsFolderPath: String? by project\n" +
                "    if (serverPluginsFolderPath == null || serverPluginsFolderPath == \"\")\n" +
                "        throw GradleException(\"PLEASE SPECIFY SERVER PLUGINS FOLDER IN gradle.properties !\")\n" +
                "    doLast {\n" +
                "        copy {\n" +
                "            from(\"\${project.projectDir}/build/libs/\${project.name}-\${project.version}.jar\")\n" +
                "            into(serverPluginsFolderPath!!)\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "kotlin {\n" +
                "    jvmToolchain(17)\n" +
                "}\n" +
                "\n" +
                (if (useNMS) "tasks.assemble {\n" +
                        "    dependsOn(tasks.reobfJar)\n" +
                        "}" else "")
    )
    writer.close()
    println("DONE")

    print("  gradle.properties ... ")
    file = File(rootFolder, "gradle.properties")
    writer = FileWriter(file)
    writer.write(
        "kotlin.code.style=official\n" +
                "\n" +
                "serverPluginFolderPath=\"\"\n"
    )
    writer.close()
    println("DONE")

    print("  settings.gradle.kts ... ")
    file = File(rootFolder, "settings.gradle.kts")
    writer = FileWriter(file)
    writer.write(
        "plugins {\n" +
                "    id(\"org.gradle.toolchains.foojay-resolver-convention\") version \"0.5.0\"\n" +
                "}\n" +
                "rootProject.name = \"$pluginName\"\n"
    )
    writer.close()
    println("DONE")
}

fun createPluginYml(rootFolder: File, pluginName: String, pluginPackage: String, version: String) {
    print("Creating plugin.yml ... ")
    var file = File(rootFolder, "src/main/resources")
    file.mkdirs()
    file = File(file, "plugin.yml")
    file.createNewFile()
    val writer = FileWriter(file)
    writer.write(
        "name: $pluginName\n" +
                "version: \${version}\n" +
                "main: $pluginPackage.$pluginName\n" +
                "api-version: ${versionToApiVersion(version)}\n" +
                "prefix: $pluginName\n" +
                "#author: NAME\n" +
                "#description: desc\n" +
                "#website: https://example.com/\n" +
                "\n" +
                "libraries:\n" +
                "  - \"org.jetbrains.kotlin:kotlin-stdlib:1.9.24\"\n\n"
    )
    writer.close()
    println("DONE")
}

fun versionToApiVersion(version: String): String {
    val sp = version.split(".")
    return sp[0] + "." + sp[1]
}








