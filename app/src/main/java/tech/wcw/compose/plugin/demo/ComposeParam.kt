package tech.wcw.compose.plugin.demo

import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview

/**
 * @author: tech_wcw@163.com
 * @date: 2023/8/4
 */
fun normal() {
    Log.i("ComposeParam", "这是个普通函数")
}

fun normalWithParam(param: String) {
    Log.i("ComposeParam", "这是个带参数函数")
}

fun normalWithNullableParam(param: String?) {
    Log.i("ComposeParam", "这是个参数可为null函数")
}

fun normalWithParams(
    p0: String,
    p1: String,
    p2: String,
    p3: String,
    p4: String,
    p5: String,
    p6: String,
    p7: String,
    p8: String,
    p9: String,
    p10: String,
    p11: String,
) {
    Log.i("ComposeParam", "这是个参数可为null的带多参数函数")
}

var param = "test"

@Preview
@Composable
fun prev() {
    composeWithParam(param)
}

@Composable
fun compose() {
    Text(text = "这是个普通composable函数")
}


@Composable
fun composeWithParam(param: String) {
    Text(text = "这是个带参数${param}composable函数")
}

@Composable
fun composeWithNullableParam(param: String?) {
    Text(text = "这是个参数可为null的带参数${param}composable函数")
}

@Composable
fun composeWithParams(
    p0: String,
    p1: String,
    p2: String,
    p3: String,
    p4: String,
    p5: String,
    p6: String,
    p7: String,
    p8: String,
    p9: String,
    p10: String,
    p11: String,
) {
    Text(text = "这是个参数可为null的带多参数的的composable函数")

}
