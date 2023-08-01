package tech.wcw.compose.plugin.demo

import android.annotation.SuppressLint
import android.content.Context
import dalvik.system.DexClassLoader
import java.io.File
import java.lang.reflect.Array.newInstance
import java.lang.reflect.Field

/**
 * @author: tech_wcw@163.com
 * @date: 2023/7/31
 */
class PluginManager {
    companion object {
        var pluginClassLoader: DexClassLoader? = null

        fun loadPlugin(context: Context) {
            val inputStream = context.assets.open("plugin.apk")
            val filesDir = context.externalCacheDir
            val apkFile = File(filesDir?.absolutePath, "plugin.apk")
            apkFile.writeBytes(inputStream.readBytes())

            val dexFile = File(filesDir, "dex")
            if (!dexFile.exists()) dexFile.mkdirs()
            println("dexPath: $dexFile")
            pluginClassLoader = DexClassLoader(
                apkFile.absolutePath,
                dexFile.absolutePath,
                null,
                this.javaClass.classLoader
            )
        }

        fun loadClass(className: String): Class<*>? {
            try {
                return pluginClassLoader?.loadClass(className)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

    }
}