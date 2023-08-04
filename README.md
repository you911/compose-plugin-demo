## 写在前面

对Composable编译期处理的源码阅读相关验证和文档请切换到param分支。

## 什么是Compose

Jetpack Compose是谷歌官方推荐的Android UI实现方式，避免了Android传统View在绘制、编写、性能等方面的种种缺点，具体使用方法请参考官方文档。

## Compose与插件化

想必大家都接触过或者了解过插件化开发，没接触过的小伙伴们要补补课啦。在以往插件化开发过程中，清单文件中要有Activity（宿主中）占位，还要hook、反射手段把宿主的Activity生命周期传递到插件中,这一过程往往因为Android碎片化，机型适配问题严重，对插件化开发造成重重阻碍，采用ComposeUI方式，所有视图都是composable-widget（这一点和跨平台的Flutter很像）,ComposeUI给插件化也带来了优化空间,在宿主中加载插件的某一个Composable组件更为灵活，性能更高。注意：Android
View方式也可以实现本文的功能，但是View绘制效率低，性能差。利用Compose实现插件化的原理与传统插件化是一致的，主要是省略了黑科技创建Activity,传递生命周期的过程。

## 实践

[先摆上github源码地址](https://github.com/you911/compose-plugin-demo.git)

### 项目创建

1. 创建一个项目，没什么新鲜东西，这里就不放图了
2. 创建插件项目：在项目里新增module,我这里命名为plugin了，在项目根目录gradle.properties文件中新增变量配置`runAlone=true`
   用于配置管理plugin是否可以独立运行
3. 修改plugin模块的gradle配置如下：

```
plugins {
    id 'org.jetbrains.kotlin.android'
}
def aloneRun = runAlone.toBoolean()
//根据runalone配置，动态添加gradle的plugin
if (aloneRun) {
    plugins.apply('com.android.application')
} else {
    plugins.apply('com.android.library')
}
...
  defaultConfig {
        if (aloneRun) {
            //plugin模块作为application时的applicationId
            applicationId "tech.wcw.compose.plugin"
        }
  }
...
        //plugin模块作为application时添加源文件目录和清单文件，方便开发和测试（compose方式可以通过@Preview预览，按需添加）
        if (aloneRun) {
            sourceSets {
                main.java.srcDirs += 'src/alone/java'
                main.manifest.srcFile 'src/alone/AndroidManifest.xml'
            }
        }
```

### 插件实现

- 方式一： 此处有坑！！！声明的Composable组件编译后携带（Composer,Int）参数，后一个参数与重组息息相关，在调用时如果不能正确传参，可能会产生奇怪的bug。
  直接声明Composable组件，通过@Composable注解声明了Composable组件。

```kotlin
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
```

- 方式二： lambda表达式间接调用,即将Composable组件声明为一个函数表达式，在编译后，java中表示为FunctionN(0~23)
  ,此处与Composable组件是否传参有关，可以在编译后的文件中查看具体类型

```kotlin
val pluginView: (@Composable () -> Unit) = {
    Log.i(tag, "pluginView v2 重组")
    Box(
        modifier = Modifier
            .background(Color.Blue)
            .fillMaxWidth()
            .height(40.dp)
    )
}
```

### 宿主实现

1. 将plugin打包放入assets中（demo演示用，实际开发应该是从服务器下载）
2. 获取dex,创建classLoader (demo简单处理，未进行合并dex)

```kotlin
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
```

3.加载插件，通过ClassLoader获取到Class,通过反射得到对应的方法，传参调用即可。 注意：方式一**自己传参Composer和changed,后者会对重组有影响，一定要注意**！
方式二不用自己传参，交给compose黑科技处理了。

```kotlin
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
```

___

## 补充内容

获取到插件内组件的method(方式一)
或者插件内Composable（方式二）的引用后，插件内组件的使用与普通方法是几乎一致的，我的疏忽，没有把github源码中的使用代码贴出来。此外demo源码中补充了SideEffect、LauncherEffect的代码块，用于验证Compose组件重组过程。 <br />
文章内容见下文：

### 宿主内使用方法

#### 方式一

采用方式一获取到的method，需要我们自己传参composer:Composer和changed:
Int，特别是后者，与Composable组件声明时的函数参数有关，且会影响重组过程，后续单独写一篇关于这个参数的总结。在前文中，我们已经获取到pluginV1Method和pluginV1Obj，直接利用反射方法invoke在宿主内的组件中使用即可

```kotlin
if (applyV1Success) {
    pluginV1Method!!.invoke(pluginV1Obj, param, currentComposer, 0b000)
}
```

#### 方式二

方式二获取到的pluginV2Compose是一个Composable的表达式，这个使用方式就简单了，与声明在宿主内组件的使用方法一致

```kotlin
if (applyV2Success) {
    pluginV2Compose()
}
```

### Composse插件化对Effect的影响

在插件内的Effect是否能正常运行呢？在插件的组件中添加LauncherEffect、SideEffect后是能正常执行的。具体运行情况可以下载[demo](https://github.com/you911/compose-plugin-demo.git)或者自己实现下。

```kotlin
//声明effect
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
//打印结果
    2023 - 08-04 10:43:32.730 13207-13207 PluginV1                tech.wcw.compose.plugin.demo         I  pluginView2 SideEffect内打印
    2023 - 08-04 10:43:32.750 13207-13207 PluginV1                tech.wcw.compose.plugin.demo         I  pluginView2 LaunchedEffect打印
```

### 性能

两种方式都使用了反射，毫无疑问是对性能肯定是有一定影响的，在使用时要避免重复反射。现在的设备性能都还不错，在不滥用反射，合理利用资源的情况下，对性能的影响是微乎其微的。<br />
获取class和反射newInstance、获取method、Comppose组件添加了相关打印，demo中都有，小伙伴们可以自己运行下试试。

```kotlin
2023 - 08 - 04 10:52:32.501 13662 - 13662 MainActivity tech.wcw.compose.plugin.demo I applyPluginV1: PluginV1 class加载耗时 1
2023 - 08 - 04 10:52:32.504 13662 - 13662 MainActivity tech.wcw.compose.plugin.demo I applyPluginV1: PluginV1 method 加载耗时 2
2023 - 08 - 04 10:52:32.533 13662 - 13662 MainActivity tech.wcw.compose.plugin.demo I applyPluginV1: PluginV1 newInstance 耗时 28
2023 - 08 - 04 10:52:32.534 13662 - 13662 MainActivity tech.wcw.compose.plugin.demo I applyPluginV1: PluginV1 从加载到newInstance总耗时 34
```

### 最后

如果文内或源码内有错误，欢迎大家指正和批评
