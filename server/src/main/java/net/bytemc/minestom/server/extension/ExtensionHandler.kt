package net.bytemc.minestom.server.extension

import net.bytemc.minestom.server.ByteServer
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.jar.JarFile

class ExtensionHandler {
    private var path: Path = Path.of("extensions")

    fun list() {
        val files = path.toFile().listFiles()

        if(files != null) {
            for (file in files.filter { it.name.endsWith(".jar") }) {
                val jarFile = JarFile(file)
                val classLoader = URLClassLoader(arrayOf(file.toURL()), ByteServer.instance.javaClass.classLoader)


            }
        }
    }
}