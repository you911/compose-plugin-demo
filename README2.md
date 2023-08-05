# 编译器对Composable函数做了什么

在研究Composable实现插件化中，反射获取插件中的Composable函数时发现Composable函数多了两个参数（多出的参数数量>
=2个，与组件参数有关，下面会提到)想必Compose的重组与这两个参数有着密切联系，让我们一起研究一下。
<br />**注意！：经过查看源码，发现不一定会有Composer参数，详见“编译期间Composable字节码处理”小节**，

## 普通函数

首先我们在回顾下kotlin函数与其反编译生成的java代码区别。
<br />分别声明无参、有参数、参数可空等情况的函数

```kotlin
fun normal() {
    "这是个普通方法".log()
}

fun normalWithParam(param: String) {
    "这是个带参数方法 $param".log()
}

fun normalWithNullableParam(param: String?) {
    "这是个参数可为null的方法 $param".log()
}
```

查看反编译结果如下：

```java
//省略了一些无关紧要的部分
public final class ComposeParamKt {
    public static final void normal() {
        Log.i("ComposeParam", "这是个普通函数");
    }

    public static final void normalWithParam(@NotNull String param) {
        Intrinsics.checkNotNullParameter(param, "param");
        Log.i("ComposeParam", "这是个带参数函数");
    }

    public static final void normalWithNullableParam(@Nullable String param) {
        Log.i("ComposeParam", "这是个参数可为null函数");
    }
}
```

由此可见，普通方法的反编译后的结果与kotlin中是几乎一致的，差异主要在语法差异。

## Composable函数

### 无参Composable函数

首先声明一个最基本的Composable函数

```kotlin
@Composable
fun compose() {
    Text(text = "这是个普通composable函数")
}
```

反编译结果如下:

```java
public final class ComposeParamKt {
    public static final void compose(Composer paramComposer, int paramInt) {
        paramComposer = paramComposer.startRestartGroup(-920674677);
        ComposerKt.sourceInformation(paramComposer, "C(compose)41@682L32:ComposeParam.kt#n1emkn");
        if (paramInt != 0 || !paramComposer.getSkipping()) {
            TextKt.Text - fLXpl1I(LiveLiterals$ComposeParamKt.INSTANCE.String$arg - 0$call - Text$fun - compose(), null, 0L, 0L, null, null, null, 0L, null, null, 0L, 0, false, 0, null, null, paramComposer, 0, 0, 65534);
        } else {
            paramComposer.skipToGroupEnd();
        }
        ScopeUpdateScope scopeUpdateScope = paramComposer.endRestartGroup();
        if (scopeUpdateScope == null)
            return;
        scopeUpdateScope.updateScope(new ComposeParamKt$compose$1(paramInt));
    }

    //这里是kotlin的函数对应FunctionN的接口
    @Metadata(k = 3, mv = {1, 6, 0}, xi = 48)
    static final class ComposeParamKt$compose$1 extends Lambda implements Function2<Composer, Integer, Unit> {
        ComposeParamKt$compose$1(int param1Int) {
            super(2);
        }

        public final void invoke(Composer param1Composer, int param1Int) {
            ComposeParamKt.compose(param1Composer, this.$$changed | 0x1);
        }
    }
}
```

<br />
比较来看，比kotlin中多生成了两个参数，暂时先不管这俩的作用，先看一下代码块里面的关键代码，startRestartGroup和endRestartGroup是"一对"，用来记录可组合函数的组,并在调用endRestartGroup时根据传递给updateScope的lambda启动可按需重新组合的组。我怎么知道的？方法注释上写的明明白白的，遇事不决，先看sdk文档。
<br /> 条件判断if(paramInt != 0 || !paramComposer.getSkipping())，结合上下文，是根据条件是否跳过重组。
<br />经过分析代码块里的逻辑，那么两个参数的职责就是前者负责控制重组（Composer的文档很详细，本文不进行深入研究），根据后者判断是否跳过重组。主体逻辑如下：

```
    //伪代码
    //记录组
    paramComposer = paramComposer.startRestartGroup(xxx);
    if (paramInt != 0 || !paramComposer.getSkipping()) {
        //组件内容
    }else{
        //跳到组最后
    }
    //记录结束
    ScopeUpdateScope scopeUpdateScope = paramComposer.endRestartGroup();
    if (scopeUpdateScope == null)
        return;
    //可按需重新组合的组
    scopeUpdateScope.updateScope(new ComposeParamKt$compose$1(paramInt));
```

那么如果是有参数的呢?一个有参数的Composable函数往往会因为参数的变化而重组，这段逻辑里没有体现啊，接下来我们看下有参的有参Composable函数

### 有参Composable函数

声明有参Composable函数如下：

```kotlin
@Composable
fun composeWithParam(param: String) {
    Text(text = "这是个带参数${param}composable函数")
}
```

反编译结果如下：

```java
public final class ComposeParamKt {
    public static final void composeWithNullableParam(String paramString, Composer paramComposer, int paramInt) {
        paramComposer = paramComposer.startRestartGroup(999288386);
        ComposerKt.sourceInformation(paramComposer, "C(composeWithNullableParam)51@880L50:ComposeParam.kt#n1emkn");
        int i = paramInt;
        int j = i;
        if ((paramInt & 0xE) == 0) {
            if (paramComposer.changed(paramString)) {
                j = 4;
            } else {
                j = 2;
            }
            j = i | j;
        }
        if ((j & 0xB ^ 0x2) != 0 || !paramComposer.getSkipping()) {
            TextKt.Text - fLXpl1I(LiveLiterals$ComposeParamKt.INSTANCE.String$0$str$arg - 0$call - Text$fun - composeWithNullableParam() + paramString + LiveLiterals$ComposeParamKt.INSTANCE.String$2$str$arg - 0$call - Text$fun - composeWithNullableParam(), null, 0L, 0L, null, null, null, 0L, null, null, 0L, 0, false, 0, null, null, paramComposer, 0, 0, 65534);
        } else {
            paramComposer.skipToGroupEnd();
        }
        ScopeUpdateScope scopeUpdateScope = paramComposer.endRestartGroup();
        if (scopeUpdateScope == null)
            return;
        scopeUpdateScope.updateScope(new ComposeParamKt$composeWithNullableParam$1(paramString, paramInt));
    }
}
```

<br />主体的逻辑与无参时是一致的，主要差别在是否跳过重组的判断条件中，即

```
        if ((paramInt & 0xE) == 0) {
            if (paramComposer.changed(paramString)) {
                j = 4;
            } else {
                j = 2;
            }
            j = i | j;
        }
        if ((j & 0xB ^ 0x2) != 0 || !paramComposer.getSkipping()) {
        ...
        }
```

划重点paramComposer.changed(paramString)，所以函数的参数变化时往往导致重组。
<br />本以为到此结束了，但是看到"(paramInt & 0xE) == 0"和"(j & 0xB ^ 0x2) != 0"
，我觉得事情没有那么简单，接下来我们研究下Composable函数不同情况下编译产生的int参数值

## Composable函数不同情况下编译产生的int参数值

注意：在开发过程中我们通常声明一个@Preview的Composable函数来预览效果，但是这种方式获取到的反编译结果与实际打包运行的结果是不一样的。
<br />我们在setContent中分别调用上文提到的无参、有参函数。

调用无参函数反编译结果:

```java
public final class ComposeParamKt {
    //省略无关代码 ...
    public static final void prev(Composer paramComposer, int paramInt) {
        paramComposer = paramComposer.startRestartGroup(1924327752);
        ComposerKt.sourceInformation(paramComposer, "C(prev)44@787L9:ComposeParam.kt#n1emkn");
        if (paramInt != 0 || !paramComposer.getSkipping()) {
            compose(paramComposer, 0);
        } else {
            paramComposer.skipToGroupEnd();
        }
        ScopeUpdateScope scopeUpdateScope = paramComposer.endRestartGroup();
        if (scopeUpdateScope == null)
            return;
        scopeUpdateScope.updateScope(new ComposeParamKt$prev$1(paramInt));
    }
    //省略无关代码 ...

    @Metadata(k = 3, mv = {1, 6, 0}, xi = 48)
    static final class ComposeParamKt$compose$1 extends Lambda implements Function2<Composer, Integer, Unit> {
        ComposeParamKt$compose$1(int param1Int) {
            super(2);
        }

        public final void invoke(Composer param1Composer, int param1Int) {
            ComposeParamKt.compose(param1Composer, this.$$changed | 0x1);
        }
    }
}
```

修改setContent方法，调用有参Composable函数

反编译后结果为：

```
  public static final void prev(Composer paramComposer, int paramInt) {
    paramComposer = paramComposer.startRestartGroup(1924327752);
    ComposerKt.sourceInformation(paramComposer, "C(prev)44@787L26:ComposeParam.kt#n1emkn");
    if (paramInt != 0 || !paramComposer.getSkipping()) {
      composeWithParam(LiveLiterals$ComposeParamKt.INSTANCE.String$arg-0$call-composeWithParam$fun-prev(), paramComposer, 0);
    } else {
      paramComposer.skipToGroupEnd();
    } 
    ScopeUpdateScope scopeUpdateScope = paramComposer.endRestartGroup();
    if (scopeUpdateScope == null)
      return; 
    scopeUpdateScope.updateScope(new ComposeParamKt$prev$1(paramInt));
  }
  
```

WTF!
也是0？这样逆推下去，哪怕有规律也不一定靠谱，对比AndroidView的项目，不难发现编译实现Composable转化的过程应该就在org.jetbrains.kotlin.android这一插件中，我们直接看插件源码吧。[插件源码地址](git@github.com:JetBrains/android.git)<br />
源码的module有点多，我就不放截图了，根据module名字就可以看出compose相关的插件是在compose-ide-plugin中。插件要对字节码离不开transform,我们重点查找这类代码，在androidx.compose.compiler.plugins.kotlin.lower.ComposableFunctionBodyTransformer就是关键代码。方便大家
查看[这里直接放下这个类的url](https://github.com/JetBrains/android/blob/master/compose-ide-plugin/compiler-hosted-src/androidx/compose/compiler/plugins/kotlin/lower/ComposableFunctionBodyTransformer.kt)

首先看到的就是ParamState，结合注释，我们不难发现，这是与Composable和$changed值有关。我们看下这个枚举类定义：

```kotlin
/**
 * An enum of the different "states" a parameter of a composable function can have relating to
 * comparison propagation. Each state is represented by two bits in the `$changed` bitmask.
 */
enum class ParamState(val bits: Int) {
    /**
     * Indicates that nothing is certain about the current state of the parameter. It could be
     * different than it was during the last execution, or it could be the same, but it is not
     * known so the current function looking at it must call equals on it in order to find out.
     * This is the only state that can cause the function to spend slot table space in order to
     * look at it.
     */
    Uncertain(0b000),

    /**
     * This indicates that the value is known to be the same since the last time the function was
     * executed. There is no need to store the value in the slot table in this case because the
     * calling function will *always* know whether the value was the same or different as it was
     * in the previous execution.
     */
    Same(0b001),

    /**
     * This indicates that the value is known to be different since the last time the function
     * was executed. There is no need to store the value in the slot table in this case because
     * the calling function will *always* know whether the value was the same or different as it
     * was in the previous execution.
     */
    Different(0b010),

    /**
     * This indicates that the value is known to *never change* for the duration of the running
     * program.
     */
    Static(0b011),
    Unknown(0b100),
    Mask(0b111);

    fun bitsForSlot(slot: Int): Int = bitsForSlot(bits, slot)
}

```
根据注释，每个状态由“$changed”按位表示,在调用处会根据插件分析的参数情况，设置适当的值。但是根据前文，我们反编译发现多出的是Composer和int参数，如果按位，实际上一个int参数最多才表示声明Composable函数时0~10个参数的情况，那么参数大于10个呢？我们继续往下看：
```kotlin
const val SLOTS_PER_INT = 10
/**
 * Calculates the number of 'changed' params needed based on the function's parameters.
 *
 * @param realValueParams The number of params defined by the user, those that are not implicit
 * (no extension or context receivers) or synthetic (no %composer, %changed or %defaults).
 * @param thisParams The number of implicit params, i.e. [IrFunction.thisParamCount]
 */
fun changedParamCount(realValueParams: Int, thisParams: Int): Int {
    val totalParams = realValueParams + thisParams
    if (totalParams == 0) return 1 // There is always at least 1 changed param
    return ceil(
        totalParams.toDouble() / SLOTS_PER_INT.toDouble()
    ).toInt()
}
```
这里会根据声明参数数量计算字节码操纵后的参数数量，可以看到最起码会有1个changed参数。
有了这个类，我们就可以在[Compose实现插件化](https://juejin.cn/post/7262219723294900282)中合理的调用插件代码了。
<br />但是这就完了吗？没有！我们看下插件对函数的处理。
### 编译期间Composable字节码处理
附上一段原文方法注释内容，这里概述了编译插件对Composable函数的处理。
```
/**
 * This IR Transform is responsible for the main transformations of the body of a composable
 * function.
 *
 * 1. Control-Flow Group Generation
 * 2. Default arguments
 * 3. Composable Function Skipping
 * 4. Comparison Propagation
 * 5. Recomposability
 * 6. Source location information (when enabled)
 *
 * Control-Flow Group Generation
 * =============================
 *
 * This transform will insert groups inside of the bodies of Composable functions
 * depending on the control-flow structures that exist inside of them.
 *
 * There are 3 types of groups in Compose:
 *
 * 1. Replaceable Groups
 * 2. Movable Groups
 * 3. Restart Groups
 *
 * Generally speaking, every composable function *must* emit a single group when it executes.
 * Every group can have any number of children groups. Additionally, we analyze each executable
 * block and apply the following rules:
 *
 * 1. If a block executes exactly 1 time always, no groups are needed
 * 2. If a set of blocks are such that exactly one of them is executed exactly once (for example,
 * the result blocks of a when clause), then we insert a replaceable group around each block.
 * 3. A movable group is only needed if the immediate composable call in the group has a Pivotal
 * property.
 *
 * Default Arguments
 * =================
 *
 * Composable functions need to have the default expressions executed inside of the group of the
 * function. In order to accomplish this, composable functions handle default arguments
 * themselves, instead of using the default handling of kotlin. This is also a win because we can
 * handle the default arguments without generating an additional function since we do not need to
 * worry about callers from java. Generally speaking though, compose handles default arguments
 * similarly to kotlin in that we generate a $default bitmask parameter which maps each parameter
 * index to a bit on the int. A value of "1" for a given parameter index indicated that that
 * value was *not* provided at the callsite, and the default expression should be used instead.
 *
 *     @Composable fun A(x: Int = 0) {
 *       f(x)
 *     }
 *
 * gets transformed into
 *
 *     @Composable fun A(x: Int, $default: Int) {
 *       val x = if ($default and 0b1 != 0) 0 else x
 *       f(x)
 *     }
 *
 * Note: This transform requires [ComposerParamTransformer] to also be run in order to work
 * properly.
 *
 * Composable Function Skipping
 * ============================
 *
 * Composable functions can "skip" their execution if certain conditions are met. This is done by
 * appealing to the composer and storing previous values of functions and determining if we can
 * skip based on whether or not they have changed.
 *
 *     @Composable fun A(x: Int) {
 *       f(x)
 *     }
 *
 * gets transformed into
 *
 *     @Composable fun A(x: Int, $composer: Composer<*>, $changed: Int) {
 *       var $dirty = $changed
 *       if ($changed and 0b0110 === 0) {
 *         $dirty = $dirty or if ($composer.changed(x)) 0b0010 else 0b0100
 *       }
 *      if (%dirty and 0b1011 !== 0b1010 || !$composer.skipping) {
 *        f(x)
 *      } else {
 *        $composer.skipToGroupEnd()
 *      }
 *     }
 *
 * Note that this makes use of bitmasks for the $changed and $dirty values. These bitmasks work
 * in a different bit-space than the $default bitmask because two bits are needed to hold the
 * four different possible states of each parameter. Additionally, the lowest bit of the bitmask
 * is a special bit which forces execution of the function.
 *
 * This means that for the ith parameter of a composable function, the bit range of i*2 + 1 to
 * i*2 + 2 are used to store the state of the parameter.
 *
 * The states are outlines by the [ParamState] class.
 *
 * Comparison Propagation
 * ======================
 *
 * Because we detect changes in parameters of composable functions and have that data available
 * in the body of a composable function, if we pass values to another composable function, it
 * makes sense for us to pass on whatever information about that value we can determine at the
 * time. This type of propagation of information through composable functions is called
 * Comparison Propagation.
 *
 * Essentially, this comes down to us passing in useful values into the `$changed` parameter of
 * composable functions.
 *
 * When a composable function executes, we have the current known states of all of the function's
 * parameters in the $dirty variable. We can take bits off of this variable and pass them into a
 * composable function in order to tell that function what we know.
 *
 *     @Composable fun A(x: Int) {
 *       B(x, 123)
 *     }
 *
 * gets transformed into
 *
 *     @Composable fun A(x: Int, $composer: Composer<*>, $changed: Int) {
 *       var $dirty = ...
 *       // ...
 *       B(
 *           x,
 *           123,
 *           $composer,
 *           (0b110 and $dirty) or   // 1st param has same state that our 1st param does
 *           0b11000                 // 2nd parameter is "static"
 *       )
 *     }
 *
 * Recomposability
 * ===============
 *
 * Restartable composable functions get wrapped with "restart groups". Restart groups are like
 * other groups except the end call is more complicated, as it returns a null value if and
 * only if a subscription to that scope could not have occurred. If the value returned is
 * non-null, we generate a lambda that teaches the runtime how to "restart" that group. At a high
 * level, this transform comes down to:
 *
 *     @Composable fun A(x: Int) {
 *       f(x)
 *     }
 *
 * getting transformed into
 *
 *     @Composable fun A(x: Int, $composer: Composer<*>, $changed: Int) {
 *       $composer.startRestartGroup()
 *       // ...
 *       f(x)
 *       $composer.endRestartGroup()?.updateScope { next -> A(x, next, $changed or 0b1) }
 *     }
 *
 * Source information
 * ==================
 * To enable Android Studio and similar tools to inspect a composition, source information is
 * optionally generated into the source to indicate where call occur in a block. The first group
 * of every function is also marked to correspond to indicate that the group corresponds to a call
 * and the source location of the caller can be determined from the containing group.
 */
```
主要介绍了对Composable的处理，对Compoeable函数分为:可替换组、可移动组、重新启动组，且根据组和参数的情况，对字节码进行操作，生成一些Composable，内容很多，我们以后再分析。先看看重点：
 ```kotlin
  *
 *     @Composable fun A(x: Int = 0) {
 *       f(x)
 *     }
 *
 * gets transformed into
 *
 *     @Composable fun A(x: Int, $default: Int) {
 *       val x = if ($default and 0b1 != 0) 0 else x
 *       f(x)
 *     }
 ```
 对于这种**有默认参数的Composable函数，编译生成的是没有Composer参数的**，调用插件化时注意闭坑啊。
## 最后
如果文内或源码有错误，欢迎大家指正和批评。<br/>ps:走过路过的朋友，动动你们发财的小手，点赞支持，点点关注啊。😁
