package tech.wcw.plugin.alone

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tech.wcw.compose.plugin.PluginV1
import tech.wcw.compose.plugin.PluginV2

/**
 * @author: tech_wcw@163.com
 * @date: 2023/8/1
 */
class EnterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PluginV1().pluginView("preview")
            PluginV2().pluginView()
        }
    }
}