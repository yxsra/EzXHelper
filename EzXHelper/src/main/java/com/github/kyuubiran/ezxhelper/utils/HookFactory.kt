@file:Suppress("unused")

package com.github.kyuubiran.ezxhelper.utils

import com.github.kyuubiran.ezxhelper.utils.interfaces.IMethodHookCallback
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.Unhook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.util.function.Consumer

class HookFactory {
    private val target: Member
    private var beforeHook: IMethodHookCallback? = null
    private var afterHook: IMethodHookCallback? = null

    private constructor(method: Method) {
        target = method
    }

    private constructor(constructor: Constructor<*>) {
        target = constructor
    }

    fun before(callback: IMethodHookCallback?) {
        beforeHook = callback
    }

    fun after(callback: IMethodHookCallback?) {
        afterHook = callback
    }

    fun replace(callback: (param: XC_MethodHook.MethodHookParam) -> Any?) {
        beforeHook = IMethodHookCallback { param -> param.result = callback(param) }
    }

    fun interrupt() {
        beforeHook = IMethodHookCallback { param -> param.result = null }
    }

    fun returnConstant(constant: Any?) {
        beforeHook = IMethodHookCallback { param -> param.result = constant }
    }

    private fun create(): Unhook = XposedBridge.hookMethod(target, object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            beforeHook?.onMethodHooked(param)
        }

        override fun afterHookedMethod(param: MethodHookParam) {
            afterHook?.onMethodHooked(param)
        }
    })

    @Suppress("ClassName")
    companion object `-Static` {
        @JvmSynthetic
        fun Method.createHook(block: HookFactory.() -> Unit): Unhook = HookFactory(this).also(block).create()

        @JvmSynthetic
        fun Constructor<*>.createHook(block: HookFactory.() -> Unit): Unhook = HookFactory(this).also(block).create()

        @JvmSynthetic
        fun Iterable<Method>.createHooks(block: HookFactory.() -> Unit): List<Unhook> = map { it.createHook(block) }

        @JvmSynthetic
        fun Array<Method>.createHooks(block: HookFactory.() -> Unit): List<Unhook> = map { it.createHook(block) }

        @JvmSynthetic
        fun Iterable<Constructor<*>>.createHooks(block: HookFactory.() -> Unit): List<Unhook> = map { it.createHook(block) }

        @JvmSynthetic
        fun Array<Constructor<*>>.createHooks(block: HookFactory.() -> Unit): List<Unhook> = map { it.createHook(block) }

        @JvmStatic
        fun createHook(m: Method, block: Consumer<HookFactory>): XC_MethodHook.Unhook =
            HookFactory(m).also { block.accept(it) }.create()

        @JvmStatic
        fun createHook(c: Constructor<*>, block: Consumer<HookFactory>): XC_MethodHook.Unhook =
            HookFactory(c).also { block.accept(it) }.create()

        @JvmStatic
        fun createHooks(m: Iterable<Method>, block: Consumer<HookFactory>): List<XC_MethodHook.Unhook> =
            m.map { createHook(it, block) }

        @JvmStatic
        fun createHooks(m: Array<Method>, block: Consumer<HookFactory>): List<XC_MethodHook.Unhook> =
            m.map { createHook(it, block) }

        @JvmStatic
        fun createHooks(c: Iterable<Constructor<*>>, block: Consumer<HookFactory>): List<XC_MethodHook.Unhook> =
            c.map { createHook(it, block) }

        @JvmStatic
        fun createHooks(c: Array<Constructor<*>>, block: Consumer<HookFactory>): List<XC_MethodHook.Unhook> =
            c.map { createHook(it, block) }
    }
}
