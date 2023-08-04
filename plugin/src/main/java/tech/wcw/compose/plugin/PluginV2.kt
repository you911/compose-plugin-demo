package tech.wcw.compose.plugin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * @author: tech_wcw@163.com
 * @date: 2023/8/1
 */
class PluginV2 {
    private val tag = "PluginV2"
    val pluginView: (@Composable () -> Unit) = {
        val start = System.currentTimeMillis()
        Log.i(tag, "pluginView v2 重组")
        Box(
            modifier = Modifier
                .background(Color.Blue)
                .fillMaxWidth()
                .height(40.dp)
        )
        SideEffect {
            Log.i(tag, "pluginView 耗时 ${System.currentTimeMillis() - start}")
        }
    }
    val pluginView2: (@Composable () -> Unit) = {
        val ret = remember {
            mutableStateOf(System.currentTimeMillis())
        }

        Button(onClick = {
            ret.value = System.currentTimeMillis()
        }) {
            Text(text = "插件2内组件 点击自更新 ${ret.value}")
            LaunchedEffect(ret.value) {
                Log.i(tag, "pluginView2 LaunchedEffect打印")
            }
            SideEffect {
                Log.i(tag, "pluginView2 SideEffect内打印")
            }
        }
    }

    @Preview
    @Composable
    fun prev() {
        pluginView()
    }
}