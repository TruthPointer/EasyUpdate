package org.tpmobile.easyupdate.util

import android.util.Log
import org.tpmobile.easyupdate.BuildConfig
import java.util.*
import kotlin.text.format
import kotlin.text.isEmpty
import kotlin.text.lastIndexOf
import kotlin.text.substring

object Logger {

    var customTagPrefix = ""

    var isDebug = BuildConfig.MY_DEBUG

    private fun generateTag(): String {
        val caller = Throwable().stackTrace[2]
        var tag = "%s.%s(L:%d)"
        var callerClazzName = caller.className
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1)
        tag = String.format(
            Locale.getDefault(),
            tag,
            callerClazzName,
            caller.methodName,
            caller.lineNumber
        )
        tag = if (customTagPrefix.isEmpty()) tag else "$customTagPrefix:$tag"
        return tag
    }

    fun d(content: String) {
        if (!isDebug) return
        val tag = generateTag()

        Log.d(tag, content)
    }

    fun d(content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = generateTag()

        Log.d(tag, content, tr)
    }

    fun e(content: String) {
        if (!isDebug) return
        val tag = generateTag()

        Log.e(tag, content)
    }

    fun e(content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = generateTag()

        Log.e(tag, content, tr)
    }

    fun i(content: String) {
        if (!isDebug) return
        val tag = generateTag()

        Log.i(tag, content)
    }

    fun i(content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = generateTag()

        Log.i(tag, content, tr)
    }

    fun v(content: String) {
        if (!isDebug) return
        val tag = generateTag()

        Log.v(tag, content)
    }

    fun v(content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = generateTag()

        Log.v(tag, content, tr)
    }

    fun w(content: String) {
        if (!isDebug) return
        val tag = generateTag()

        Log.w(tag, content)
    }

    fun w(content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = generateTag()

        Log.w(tag, content, tr)
    }

    fun w(tr: Throwable) {
        if (!isDebug) return
        val tag = generateTag()

        Log.w(tag, tr)
    }


    fun wtf(content: String) {
        if (!isDebug) return
        val tag = generateTag()

        Log.wtf(tag, content)
    }

    fun wtf(content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = generateTag()

        Log.wtf(tag, content, tr)
    }

    fun wtf(tr: Throwable) {
        if (!isDebug) return
        val tag = generateTag()

        Log.wtf(tag, tr)
    }

    fun sop(content: String) {
        if (!isDebug) return
        print(content)
    }

    fun sopln(content: String) {
        if (!isDebug) return
        println(content)
    }

    /////////
    fun d(tagIn: String = "", content: String) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.d(tag, content)
    }

    fun d(tagIn: String = "", content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.d(tag, content, tr)
    }

    fun e(tagIn: String = "", content: String) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.e(tag, content)
    }

    fun e(tagIn: String = "", content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.e(tag, content, tr)
    }

    fun i(tagIn: String = "", content: String) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.i(tag, content)
    }

    fun i(tagIn: String = "", content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.i(tag, content, tr)
    }

    fun v(tagIn: String = "", content: String) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.v(tag, content)
    }

    fun v(tagIn: String = "", content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.v(tag, content, tr)
    }

    fun w(tagIn: String = "", content: String) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.w(tag, content)
    }

    fun w(tagIn: String = "", content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.w(tag, content, tr)
    }

    fun wtf(tagIn: String = "", content: String) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.wtf(tag, content)
    }

    fun wtf(tagIn: String = "", content: String, tr: Throwable) {
        if (!isDebug) return
        val tag = if (tagIn.isEmpty()) generateTag() else tagIn

        Log.wtf(tag, content, tr)
    }

    fun sop(tagIn: String = "", content: String) {
        if (!isDebug) return
        print("$tagIn: $content")
    }

    fun sopln(tagIn: String = "", content: String) {
        if (!isDebug) return
        println("$tagIn: $content")
    }

}

