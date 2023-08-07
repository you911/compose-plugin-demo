package tech.wcw.compose.plugin.demo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.wcw.compose.plugin.demo.ui.theme.ComposePluginDemoTheme
import java.lang.reflect.Method

class MainActivity : ComponentActivity() {
    val tag = "MainActivity"
    var pluginV1Obj by mutableStateOf<Any?>(null)
    var pluginV1Method by mutableStateOf<Method?>(null)
    var pluginV1Method2 by mutableStateOf<Method?>(null)
    var applyV1Success by mutableStateOf(false)
    var applyV2Success by mutableStateOf(false)
    var pluginV2Compose by mutableStateOf<@Composable () -> Unit>({})
    var pluginV2Compose2 by mutableStateOf<@Composable () -> Unit>({})
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
        Column(modifier = Modifier.fillMaxWidth()) {
            var localData by remember {
                mutableStateOf("Hello!")
            }
            var param by remember {
                mutableStateOf("old value")
            }
            Text(text = localData)
            Button(onClick = {
                PluginManager.loadPlugin(context = context)
            }) {
                Text(text = "加载插件dex")
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
            localView(param = param)
            Button(onClick = {
                localData = "宿主刷新 ${System.currentTimeMillis()}"
            }) {
                Text(text = "更新宿主展示数据")
            }

            if (applyV1Success) {
                //参数2 用于参数比较和跳过重组，后续单开一篇博文介绍这个参数
                val start = System.currentTimeMillis()
                pluginV1Method!!.invoke(pluginV1Obj, param, currentComposer, 0)
                Log.i(tag, "PluginV1 插件invoke耗时 ${System.currentTimeMillis() - start}")
                pluginV1Method2!!.invoke(pluginV1Obj, currentComposer, 0)


            }

            if (applyV2Success) {
                pluginV2Compose()
                pluginV2Compose2()
            }
            SideEffect {
                Log.i(tag, "Column 重组完成")
            }
        }

    }

    @Composable
    fun localView(param: String) {
        val start = System.currentTimeMillis()
        Log.i(tag, "localView 重组")
        Box(
            modifier = Modifier
                .background(Color.Green)
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(text = "写在宿主参数 $param")
        }
        SideEffect {
            Log.i(tag, "localView 耗时 ${System.currentTimeMillis() - start}")
        }
    }

    private fun applyPluginV1() {
        if (applyV1Success && pluginV1Method != null && pluginV1Method != null && pluginV1Method2 != null) {
            return
        }
        val start = System.currentTimeMillis()
        val plugin = PluginManager.loadClass("tech.wcw.compose.plugin.PluginV1")
        Log.i(tag, "applyPluginV1: PluginV1 class加载耗时 ${System.currentTimeMillis() - start}")
        plugin?.let {
            val methodLoadStart = System.currentTimeMillis()
            val method: Method =
                plugin.getDeclaredMethod(
                    "pluginView",
                    String::class.java,
                    Composer::class.java,
                    Int::class.java
                )
            method.isAccessible = true
            Log.i(
                tag,
                "applyPluginV1: PluginV1 method 加载耗时 ${System.currentTimeMillis() - methodLoadStart}"
            )
            val newInstanceStart = System.currentTimeMillis()
            pluginV1Obj = plugin.newInstance()
            Log.i(
                tag,
                "applyPluginV1: PluginV1 newInstance 耗时 ${System.currentTimeMillis() - newInstanceStart}"
            )
            Log.i(
                tag,
                "applyPluginV1: PluginV1 从加载到newInstance总耗时 ${System.currentTimeMillis() - start}"
            )
            pluginV1Method = method

            val method2: Method =
                plugin.getDeclaredMethod(
                    "pluginView2",
                    Composer::class.java,
                    Int::class.java
                )
            method.isAccessible = true
            pluginV1Method2 = method2
            applyV1Success = true
        }
    }

    private fun applyPluginV2() {
        if (applyV2Success) {
            return
        }
        val start = System.currentTimeMillis()
        val plugin = PluginManager.loadClass("tech.wcw.compose.plugin.PluginV2")
        Log.i(tag, "applyPluginV2: PluginV2 class加载耗时 ${System.currentTimeMillis() - start}")
        plugin?.let {
            val methodLoadStart = System.currentTimeMillis()
            val method: Method = plugin.getDeclaredMethod("getPluginView")
            method.isAccessible = true
            Log.i(
                tag,
                "applyPluginV2: PluginV2 method加载耗时 ${System.currentTimeMillis() - methodLoadStart}"
            )
            val newInstanceStart = System.currentTimeMillis()
            val obj = plugin.newInstance()
            Log.i(
                tag,
                "applyPluginV2: PluginV2 newInstance耗时 ${System.currentTimeMillis() - newInstanceStart}"
            )
            pluginV2Compose = method.invoke(obj) as (@Composable () -> Unit)
            val getComposeStart = System.currentTimeMillis()
            Log.i(
                tag,
                "applyPluginV2: PluginV2 获取Composable函数耗时 ${System.currentTimeMillis() - getComposeStart}"
            )
            Log.i(
                tag,
                "applyPluginV2: PluginV2 加载总耗时 ${System.currentTimeMillis() - start}"
            )
            val method2: Method = plugin.getDeclaredMethod("getPluginView2")
            method.isAccessible = true
            pluginV2Compose2 = method2.invoke(obj) as (@Composable () -> Unit)
            applyV2Success = true
            applyV2Success = true
        }
    }
}





