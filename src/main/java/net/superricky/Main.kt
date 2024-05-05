package net.superricky

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.Win32Exception
import com.sun.jna.platform.win32.WinReg
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import kotlin.system.exitProcess

var ARK_INSTALLATION_PATH: String? = "C:/Users/ricde/OneDrive/Desktop/VirtualArkInstall"

fun main() {
    println("Megala Pakela")

    val patches: MutableList<Patch> = mutableListOf()

    patches.add(Patch(
        "BaseScalability",
        false,
        mapOf(Pair("BaseScalability.ini", "/Engine/Config/BaseScalability.ini")),
        "This is a total overhaul of the scalability groups used in ARK:SE with a focus on increasing visual quality at the high end, and performance at the low end. The Epic settings in this rework are built to push circa-2020 GPUs much harder than the defaults, while the Medium and Low settings should open up more performance to lower-end hardware than is possible with the defaults.",
        "https://steamcommunity.com/sharedfiles/filedetails/?id=1914356037",
        "https://steamcommunity.com/id/lordbean",
        "lordbean"
        ))
    patches.add(Patch(
        "Engine",
        false,
        mapOf(Pair("Engine.ini", "/ShooterGame/Saved/Config/WindowsNoEditor/Engine.ini")),
        "This is a total overhaul of the scalability groups used in ARK:SE with a focus on increasing visual quality at the high end, and performance at the low end. The Epic settings in this rework are built to push circa-2020 GPUs much harder than the defaults, while the Medium and Low settings should open up more performance to lower-end hardware than is possible with the defaults.",
        "https://steamcommunity.com/sharedfiles/filedetails/?id=2356992556",
        "https://steamcommunity.com/id/AndreikaRPM",
        "Few"
    ))
    patches.add(Patch(
        "DefaultBaseEngine",
        false,
        mapOf(Pair("DefaultEngine.ini", "/ShooterGame/Config/DefaultEngine.ini"), Pair("BaseEngine.ini", "/Engine/Config/BaseEngine.ini")),
        "This is a total overhaul of the scalability groups used in ARK:SE with a focus on increasing visual quality at the high end, and performance at the low end. The Epic settings in this rework are built to push circa-2020 GPUs much harder than the defaults, while the Medium and Low settings should open up more performance to lower-end hardware than is possible with the defaults.",
        "https://steamcommunity.com/sharedfiles/filedetails/?id=1914356037",
        "https://steamcommunity.com/id/lordbean",
        "lordbean"
    ))

    println()
    println("Attempting to detect valid ARK: SE installations...")
    println("Finding Steam Directory...")
    val steamInstallPath = getSteamInstallPath()

    var containingLibraryPath: String? = null

    var arkPath: String? = null

    steamInstallPath?.let {
        println("Finding SteamLibrary containing Ark: SE install...")
        containingLibraryPath = getContainingLibraryPath(it)
    } ?: {
        ARK_INSTALLATION_PATH = getManualArkPath()
    }

    containingLibraryPath?.let {
        println("Finding Ark: SE Installation Directory...")
        arkPath = getArkPath(it)
    } ?: {
        ARK_INSTALLATION_PATH?.let {} ?: {
            ARK_INSTALLATION_PATH = getManualArkPath()
        }
    }

    arkPath?.let {
        println("Successfully detected ARK: SE install at \"$it\"")
        while (true) {
            println("Is this the ARK: SE install you wish to modify (y/n)?")
            print("Ark-Performance-Evolved> ")
            val command = readln()
            when {
                command.equals("y", ignoreCase = true) -> {
                    println("Selected Install: \"$it\"")
                    break
                }

                command.equals("n", ignoreCase = true) -> {
                    getManualArkPath()
                    break
                }

                else -> {
                    println("Invalid Response!")
                    println("Is \"$it\" the ARK: SE install you wish to modify (y/n)?")
                }
            }
        }

    } ?: {
        ARK_INSTALLATION_PATH?.let {} ?: {
            ARK_INSTALLATION_PATH = getManualArkPath()
        }
    }

    println()
    println("Entering User Interface, type \"HELP\" for help.")

    while (true) {
        print("Ark-Performance-Evolved> ")
        val command = readln()

        when {
            command.equals("CANCEL", ignoreCase = true) -> {
                println("Closing interface, no patches will be applied...")
                exitProcess(0)
            }

            command.equals("DONE", ignoreCase = true) -> {
                println("Closing interface...")
                break;
            }

            command.equals("LIST", ignoreCase = true) -> {
                for (patch in patches) {
                    patch.printData()
                }
            }

            command.equals("CHANGE_ARK_PATH", ignoreCase = true) -> {
                getManualArkPathFromInterface()?.let {
                    ARK_INSTALLATION_PATH = it
                }
            }

            command.equals("HELP", ignoreCase = true) || command.equals("?", ignoreCase = true) -> {
                println("Avaliable Commands (Command Case dosen't matter):")
                println("HELP / ?: Shows this screen.")
                println("LIST: Shows all avaliable patches.")
                println("CHANGE_ARK_PATH: Changes the ARK: SE install directory, to whatever directory you specify (this does not move files, only changes which directory we will target the patches to).")
                println("GET_ARK_PATH: Gets the ARK: SE install directory which will have the patches applied to")
                println("DONE: Quits this interface and applies the patches.")
                println("CANCEL: Quits this interface and doesn't apply the patches.")
                println("<PATCH NAME>: Toggles the enabled status of said patch.")
            }

            else -> {
                val patchToToggle = patches.find { it.name.equals(command, ignoreCase = true) }
                patchToToggle?.let {
                    it.enabled = !it.enabled
                    println("Toggled patch: " + it.name + " - New Status: " + it.enabled)
                } ?: println("Invalid patch name. Please enter a valid patch name or \"DONE\" / \"CANCEL\" to exit.")
            }
        }
    }

    for (patch in patches) {
        if (patch.enabled) {
            patch.applySelf()
        }
    }

    println("All patches have been applied, enjoy gaming!")
    print("Press any key to continue..."); readln()
}

data class Patch(
    val name: String,
    var enabled: Boolean = false,
    val files: Map<String?, String>,
    val description: String,
    val source: String,
    val authorLink: String,
    val author: String) {

    fun printData() {
        println()
        println("Patch: $name")
        println("Enabled: $enabled")
        println("Description: $description")
        println("Source: $source")
        println("Author: $author ($authorLink)")
        for (entry in files) {
            if (files.count() > 1) {
                println("Files Affected: $entry.value")
            } else {
                println("File Affected: $entry.value")
            }
        }
    }

    fun applySelf() {
        for (entry in files) {
            println()
            val patched_file_data = entry.key?.let { getResourceAsText("/$it") }

            if (patched_file_data == null) {
                println("FATAL: Failed to apply patch $name, could not find associated resource for the patch.")
                continue;
            }

            val filePathToBeReplaced = ARK_INSTALLATION_PATH + entry.value

            try {
                File(filePathToBeReplaced).writeText(patched_file_data)
                println("Successfully applied patch $name")
            } catch (e: FileNotFoundException) {
                println("WARNING: " + e.message)
                println("Couldn't find the file that was targeted by patch $name. Is the ARK: SE directory corrupted?")
                println("We will automatically create the appropriate folders / files, but if you encounter any issues try verifying your ARK: SE install")

                val file = File(filePathToBeReplaced)
                file.parentFile.mkdirs()
                file.createNewFile()
                file.writeText(patched_file_data)

                println("Successfully? applied patch $name")
            }
        }
    }
}

fun getResourceAsText(path: String): String? =
    object {}.javaClass.getResource(path)?.readText()

fun getManualArkPathFromInterface(): String? {
    while (true) {
        println()
        println("Enter the direct path to your ARK: SE installation, or \"CANCEL\" to cancel this operation and return back to the interface without modifying the ARK: SE path.")
        println("Example: \"E:\\SteamLibrary\\steamapps\\common\\ARK\"")

        print("Ark-Performance-Evolved> ")
        val tempArkPath = readln()

        if (tempArkPath.equals("CANCEL", ignoreCase = true)) {
            println("Closing interface, no patches will be applied...")
            exitProcess(0)
        }

        if (!File(tempArkPath).exists()) {
            println("That path does not exist!")
            continue
        }

        println()
        println("Are you sure this the ARK: SE install you wish to modify (y/n)?")
        while (true) {
            print("Ark-Performance-Evolved> ")
            val command1 = readln()
            when {
                command1.equals("y", ignoreCase = true) -> {
                    println("Selected Install: \"$tempArkPath\"")
                    return tempArkPath
                }

                command1.equals("n", ignoreCase = true) -> {
                    break
                }

                command1.equals("CANCEL", ignoreCase = true) -> {
                    println("Closing interface, the ARK: SE path has not been modified...")
                    return null
                }

                else -> {
                    println("Invalid Response!")
                    println("Are you sure \"$tempArkPath\" is the ARK: SE install you wish to modify (y/n)?")
                }
            }
        }
    }
}

fun getManualArkPath(): String {
    while (true) {
        println()
        println("Enter the direct path to your ARK: SE installation, or \"CANCEL\" to close the application without applying any patches.")
        println("Example: \"E:\\SteamLibrary\\steamapps\\common\\ARK\"")

        print("Ark-Performance-Evolved> ")
        val tempArkPath = readln()

        if (tempArkPath.equals("CANCEL", ignoreCase = true)) {
            println("Closing interface, no patches will be applied...")
            exitProcess(0)
        }

        if (!File(tempArkPath).exists()) {
            println("That path does not exist!")
            continue
        }

        println()
        println("Are you sure this the ARK: SE install you wish to modify (y/n)?")
        while (true) {
            print("Ark-Performance-Evolved> ")
            val command1 = readln()
            when {
                command1.equals("y", ignoreCase = true) -> {
                    println("Selected Install: \"$tempArkPath\"")
                    return tempArkPath
                }

                command1.equals("n", ignoreCase = true) -> {
                    break
                }

                command1.equals("CANCEL", ignoreCase = true) -> {
                    println("Closing interface, no patches will be applied...")
                    exitProcess(0)
                }

                else -> {
                    println("Invalid Response!")
                    println("Are you sure \"$tempArkPath\" is the ARK: SE install you wish to modify (y/n)?")
                }
            }
        }
    }
}

fun getArkPath(containingLibraryPath: String): String? {
    val arkDirectoryPath = "${containingLibraryPath.replace("\\", "/")}/steamapps/common/ARK"
    val arkDirectory = File(arkDirectoryPath)

    return if (arkDirectory.exists()) arkDirectoryPath
    else null
}

fun getContainingLibraryPath(steamInstallPath: String): String? {
    val steamLibrary = parseSteamLibraryVDF(steamInstallPath)

    val libraryFolders = steamLibrary.getJSONArray("libraryfolders")

    for (i in 0 until libraryFolders.length()) {
        val folder = libraryFolders.getJSONObject(i)
        val apps = folder.optJSONObject("apps") ?: continue

        if (apps.has("346110")) {
            return folder.getString("path")
        }
    }

    return null
}

fun parseSteamLibraryVDF(steamInstallPath: String): JSONObject {
    val file = File("$steamInstallPath/steamapps/libraryfolders.vdf")
    val steamLibraryVDF = file.readText()

    val parsedJSONObject = VDF.toJSONObject(steamLibraryVDF, true)

    return parsedJSONObject
}

fun getSteamInstallPath(): String? {
    try {
        val steamInstallPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\Valve\\Steam", "InstallPath")
        println("Detected Steam Installation path at \"$steamInstallPath\"!")
        return steamInstallPath
    } catch (e: Win32Exception) {
        println("Failed to find Steam Installation path: ${e.message}")
        return null;
    }
}