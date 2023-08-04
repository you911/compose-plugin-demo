# 编译器对Composable函数做了什么

知道Composable和suspend一样，在编译期做了处理，但是没并没深入研究，在研究Composable实现插件化中，反射获取插件中的Composable函数时发现Composable函数多了两个参数（多出的参数数量>
=2个，与组件参数有关，下面会提到)
, 想必Compose的重组与这两个参数有着密切联系，让我们一起研究一下。

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
源码的module有点多，我就不放截图了，根据module名字就可以看出compose相关的插件是在compose-ide-plugin中。插件要对字节码离不开transform,我们重点查找这类代码，在androidx.compose.compiler.plugins.kotlin.lower.ComposableTargetAnnotationsTransformer就是关键代码。方便大家
[这里直接放下这个类的url](https://github.com/JetBrains/android/blob/master/compose-ide-plugin/compiler-hosted-src/androidx/compose/compiler/plugins/kotlin/lower/ComposableFunctionBodyTransformer.kt)
<br />代码比较多，但是逻辑是很清晰的，visitFunctionInScope就是这块关键业务的切入口。代码如下:

```kotlin
 @OptIn(ObsoleteDescriptorBasedAPI::class)
private fun visitFunctionInScope(declaration: IrFunction): IrStatement {
    val scope = currentFunctionScope
    // if the function isn't composable, there's nothing to do
    if (!scope.isComposable) return super.visitFunction(declaration)
    val restartable = declaration.shouldBeRestartable()
    val isLambda = declaration.isLambda()

    val isTracked = declaration.returnType.isUnit()

    if (declaration.body == null) return declaration

    val changedParam = scope.changedParameter!!
    val defaultParam = scope.defaultParameter

    // restartable functions get extra logic and different types of groups from
    // non-restartable functions, and lambdas get no groups at all.
    return when {
        isLambda && isTracked -> visitComposableLambda(
            declaration,
            scope,
            changedParam
        )
        restartable && isTracked -> visitRestartableComposableFunction(
            declaration,
            scope,
            changedParam,
            defaultParam
        )
        else -> visitNonRestartableComposableFunction(
            declaration,
            scope,
            changedParam,
            defaultParam
        )
    }
}
```

阅读源码发现Composable的lambda表达式还单独处理了，这点我之前是不知道的。这块的区别等有时间好好研究下。我们先看看visitComposableLambda里怎么处理的：

```kotlin
  // we start off assuming that we *can* skip execution of the function
var canSkipExecution = declaration.returnType.isUnit() &&
        scope.allTrackedParams.none { stabilityOf(it.type).knownUnstable() }

// if the function can never skip, or there are no parameters to test, then we
// don't need to have the dirty parameter locally since it will never be different from
// the passed in `changed` parameter.
val dirty = if (canSkipExecution && scope.allTrackedParams.isNotEmpty())
// NOTE(lmr): Technically, dirty is a mutable variable, but we don't want to mark it
// as one since that will cause a `Ref<Int>` to get created if it is captured. Since
// we know we will never be mutating this variable _after_ it gets captured, we can
// safely mark this as `isVar = false`.
    changedParam.irCopyToTemporary(
        // LLVM validation doesn't allow us to have val here.
        isVar = !(context.platform.isJvm() || context.platform.isJs()),
        nameHint = "\$dirty",
        exactName = true
    )
else
    changedParam

scope.dirty = dirty
//省略...
val canSkipExecution = buildPreambleStatementsAndReturnIfSkippingPossible(
    body,
    skipPreamble,
    bodyPreamble,
    isSkippableDeclaration = true, // we start off assuming that we *can* skip execution of the function
    scope,
    dirty,
    changedParam,
    defaultParam,
    defaultScope,
)

```

这里的dirty就是编译生成的int参数,方法中有多处scope.allTrackedParams的调用，这是我们声明函数时的参数，上面的代码中涉及到一个很关键的函数"
buildPreambleStatementsAndReturnIfSkippingPossible"，我们看下这个方法内容：

```kotlin
      val parameters = scope.allTrackedParams
//省略...
parameters.forEachIndexed { slotIndex, param ->
    // varargs get handled separately because they will require their own groups
    if (param.isVararg) return@forEachIndexed
    val defaultIndex = scope.defaultIndexForSlotIndex(slotIndex)
    val defaultValue = param.defaultValue
    val isUnstable = stabilities[slotIndex].knownUnstable()
    val isUsed = scope.usedParams[slotIndex]

    when {
        !mightSkip || !isUsed -> {
            // nothing to do
        }
        dirty !is IrChangedBitMaskVariable -> {
            // this will only ever be true when mightSkip is false, but we put this
            // branch here so that `dirty` gets smart cast in later branches
        }
        isUnstable && defaultParam != null && defaultValue != null -> {
            // if it has a default parameter then the function can still potentially skip
            skipPreamble.statements.add(
                irIf(
                    condition = irGetBit(defaultParam, defaultIndex),
                    body = dirty.irOrSetBitsAtSlot(
                        slotIndex,
                        irConst(ParamState.Same.bitsForSlot(slotIndex))
                    )
                )
            )
        }
        !isUnstable -> {
            val defaultValueIsStatic = defaultExprIsStatic[slotIndex]
            val callChanged = irChanged(irGet(param))
            val isChanged = if (defaultParam != null && !defaultValueIsStatic)
                irAndAnd(irIsProvided(defaultParam, slotIndex), callChanged)
            else
                callChanged
            val modifyDirtyFromChangedResult = dirty.irOrSetBitsAtSlot(
                slotIndex,
                irIfThenElse(
                    context.irBuiltIns.intType,
                    isChanged,
                    // if the value has changed, update the bits in the slot to be
                    // "Different"
                    thenPart = irConst(ParamState.Different.bitsForSlot(slotIndex)),
                    // if the value has not changed, update the bits in the slot to
                    // be "Same"
                    elsePart = irConst(ParamState.Same.bitsForSlot(slotIndex))
                )
                //省略...
            )
            //省略...
        }
        //省略...
    }

}
```

方法体太长了，省略了很多代码，感兴趣的小伙伴可以自己看一下。
<br />这段方法在参数不同情况下，做编译生成的参数（dirty）做了调整，在生成的int参数中，按位记录了哪个参数是否发生变化了。
<br />skipPreamble.statements.add是动态添加了代码块，也就是前文中我们看到的反编译后比kotlin中声明函数多出来的if判断代码块。
<br />这里涉及到一个类ParamState，结合注释，我们不难发现，这是不同情况下的dirty值。我们看下这个枚举类定义：

```kotlin
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

有了这个类，我们就可以在插件化合理的调用插件代码了。但是按这个类中数据的构成，最多存储10个参数的变化情况，所以，参数大于10个的情况呢？

