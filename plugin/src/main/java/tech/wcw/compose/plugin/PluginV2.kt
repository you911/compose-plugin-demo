package tech.wcw.compose.plugin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
class PluginV2 {
    private val tag = "PluginV2"
    val pluginView: (@Composable () -> Unit) = {
        Log.i(tag, "pluginView v2 重组")
        Box(
            modifier = Modifier
                .background(Color.Blue)
                .fillMaxWidth()
                .height(40.dp)
        )
    }

    @Preview
    @Composable
    fun prev() {
        pluginView()
    }
}