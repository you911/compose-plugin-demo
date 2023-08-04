package tech.wcw.compose.plugin

import android.util.Log
import android.view.WindowInsets.Side
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
class PluginV1 {
    private val tag = "PluginV1"

    @Composable
    fun pluginView(param: String) {
        val start = System.currentTimeMillis()
        Log.i(tag, "pluginView v1 重组")
        Box(
            modifier = Modifier
                .background(Color.Red)
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(text = "收到宿主传参 $param")
            SideEffect {
                Log.i(tag, "pluginView Text 重组")
            }
        }
        SideEffect {
            Log.i(tag, "pluginView 耗时 ${System.currentTimeMillis() - start}")
        }
    }

    @Composable
    fun pluginView2() {
        Log.i(tag, "pluginView2 重组")
        val ret = remember {
            mutableStateOf(System.currentTimeMillis())
        }
        Button(onClick = {
            ret.value = System.currentTimeMillis()
        }) {
            Text(text = "插件内组件 点击自更新 ${ret.value}")
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
        pluginView("preview")
    }
}