package tech.wcw.compose.plugin.demo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import tech.wcw.compose.plugin.demo.ui.theme.ComposePluginDemoTheme
import java.lang.reflect.Method

class MainActivity : ComponentActivity() {
    var pluginV1Obj by mutableStateOf<Any?>(null)
    var pluginV1Method by mutableStateOf<Method?>(null)
    var applyV1Success by mutableStateOf(false)
    var applyV2Success by mutableStateOf(false)
    var pluginV2Compose by mutableStateOf<@Composable () -> Unit>({})
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val context = remember {
                applicationContext
            }
            ComposePluginDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android", context)
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String, context: Context) {
        var localData by remember {
            mutableStateOf("Hello!")
        }
        var param by remember {
            mutableStateOf("old value")
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = localData)
            Button(onClick = {
                PluginManager.loadPlugin(context = context)
            }) {
                Text(text = "加载插件")
            }

            Button(onClick = {
                applyPluginV1()
            }) {
                Text(text = "加载插件v1")
            }
            Button(onClick = {
                applyPluginV2()
            }) {
                Text(text = "加载插件v2")
            }
            Button(onClick = {
                param = "${System.currentTimeMillis()}"
            }) {
                Text(text = "更新向插件参数")
            }
            Button(onClick = {
                localData = "宿主刷新 ${System.currentTimeMillis()}"
            }) {
                Text(text = "更新宿主展示数据")
            }
            if (applyV1Success) {
                //参数2 用于参数比较和跳过重组
                pluginV1Method!!.invoke(pluginV1Obj, param, currentComposer, 0b000)
            }
            if (applyV2Success) {
                pluginV2Compose()
            }
        }
    }

    private fun applyPluginV1() {
        val plugin = PluginManager.loadClass("tech.wcw.compose.plugin.PluginV1")
        plugin?.let {
            val method: Method =
                plugin.getDeclaredMethod(
                    "pluginView",
                    String::class.java,
                    Composer::class.java,
                    Int::class.java
                )
            method.isAccessible = true
            pluginV1Obj = plugin.newInstance()
            pluginV1Method = method
            applyV1Success = true
        }
    }

    private fun applyPluginV2() {
        val plugin = PluginManager.loadClass("tech.wcw.compose.plugin.PluginV2")
        plugin?.let {
            val method: Method = plugin.getDeclaredMethod("getPluginView")
            method.isAccessible = true
            val obj = plugin.newInstance()
            pluginV2Compose = method.invoke(obj) as (@Composable () -> Unit)
            applyV2Success = true
        }
    }
}





