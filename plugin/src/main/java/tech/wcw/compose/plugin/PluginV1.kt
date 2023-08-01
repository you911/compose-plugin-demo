package tech.wcw.compose.plugin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        Log.i(tag, "pluginView v1 重组")
        Box(
            modifier = Modifier
                .background(Color.Red)
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(text = "收到宿主传参 $param")
        }
    }

    @Preview
    @Composable
    fun prev() {
        pluginView("preview")
    }
}