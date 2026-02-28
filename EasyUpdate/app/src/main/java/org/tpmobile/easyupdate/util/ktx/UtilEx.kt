package org.tpmobile.easyupdate.util.ktx

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.core.text.HtmlCompat
import androidx.preference.PreferenceManager
import java.text.DecimalFormat

//-----------------------------------------------------------------------------------------------
fun String?.toIntWithDefaultZero(): Int {
    return this?.trim()?.let {
        if (it.matches(Regex("\\d+")))
            it.toInt()
        else
            0
    } ?: 0
}

//
fun Int.toHumanReadableSize(): String {
    return this.toLong().toHumanReadableSize()
}

fun Int.toHumanReadableSpeed(): String {
    return this.toLong().toHumanReadableSpeed()
}

fun Long.toHumanReadableSize(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val tb = gb / 1024.0

    val df1 = DecimalFormat("0.00")
    val df2 = DecimalFormat("0")
    return when {
        tb > 1 -> df1.format(tb).plus("TB")
        gb > 1 -> df1.format(gb).plus("GB")
        mb > 1 -> df1.format(mb).plus("MB")
        kb > 1 -> df2.format(kb).plus("KB")
        else -> this.toString().plus("B")
    }
}

fun Long.toHumanReadableSpeed(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val tb = gb / 1024.0

    val df1 = DecimalFormat("0.00")
    val df2 = DecimalFormat("0")
    return when {
        tb > 1 -> df1.format(tb).plus("TB/S")
        gb > 1 -> df1.format(gb).plus("GB/S")
        mb > 1 -> df1.format(mb).plus("MB/S")
        else -> df2.format(kb).plus("KB/S")
    }
}

////////////////////////////////////////
//ByteArray
////////////////////////////////////////
fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}

fun String.hex2ByteArray(): ByteArray {
    val s = this.replace(" ", "")
    val bs = ByteArray(s.length / 2)
    for (i in 0 until s.length / 2) {
        bs[i] = s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
    return bs
}

////////////////////////////////////////
//Boolean
////////////////////////////////////////
fun Boolean.toInt01(): Int = if (this) 1 else 0

////////////////////////////////////////
//Context <<<基礎部分>>>
////////////////////////////////////////
//1.
fun Context?.resIdToString(@StringRes resId: Int): String = this?.getString(resId) ?: ""

//20220420
fun Context?.resIdToString(@StringRes resId: Int, formatArgs: Array<String>): String =
    this?.getString(resId, *formatArgs) ?: ""

fun Context?.resIdToSpannableString(@StringRes resId: Int): SpannableString =
    SpannableString(this?.getString(resId) ?: "")

//2.Preference
fun Context.toSharedPreferenceByString(key: String, value: String) {
    PreferenceManager.getDefaultSharedPreferences(this).edit {
        putString(key, value)
    }
}

fun Context.fromSharedPreferenceByString(key: String, defaultValue: String) =
    PreferenceManager.getDefaultSharedPreferences(this)
        .getString(key, defaultValue)

fun Context.toSharedPreferenceByInt(key: String, value: Int) {
    PreferenceManager.getDefaultSharedPreferences(this).edit {
        putInt(key, value)
    }
}

fun Context.fromSharedPreferenceByInt(key: String, defaultValue: Int) =
    //使用 this.applicationContext 在Applicaton的attachBaseContext获取语言会为null，引发异常
    PreferenceManager.getDefaultSharedPreferences(this).getInt(key, defaultValue)

//3.
//4. Toast
//4.1 通用
fun Context?.toast(message: CharSequence, long: Boolean = true) {
    this?.let {
        Toast.makeText(
            this.applicationContext,
            message,
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).apply { show() }
    }
}

fun Context?.toast(
    @StringRes messageId: Int,
    long: Boolean = false,
) {
    this?.toast(getString(messageId), long)
}

fun Context?.toast(
    @StringRes messageId: Int,
    formatArgs: Array<String>,
    long: Boolean = false,
) {
    this?.toast(getString(messageId, *formatArgs), long)
}

//
fun Context?.toastHtml(
    html: String,
    htmlMode: Int = HtmlCompat.FROM_HTML_MODE_COMPACT,
    long: Boolean = false
) {
    this?.let {
        Toast.makeText(
            this.applicationContext,
            HtmlCompat.fromHtml(html, htmlMode),
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).apply { show() }
    }
}

fun Context?.toastHtml(
    @StringRes messageId: Int,
    htmlMode: Int = HtmlCompat.FROM_HTML_MODE_COMPACT,
    long: Boolean = false
) {
    this?.toastHtml(getString(messageId), htmlMode, long)
}


fun Context?.toastHtml(
    @StringRes messageId: Int,
    formatArgs: Array<String>,
    htmlMode: Int = HtmlCompat.FROM_HTML_MODE_COMPACT,
    long: Boolean = false
) {
    this?.toastHtml(getString(messageId, *formatArgs), htmlMode, long)
}

//////////////////////////
fun SpannableString.setForegroundColorSpan(
    color: Color,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
) {
    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
}

/**
 * @param color eg: Color.YELLOW
 */
fun SpannableString.setBackgroundColorSpan(
    color: Int,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
) {
    this.setSpan(BackgroundColorSpan(color), start, start + this.length, spannableStyle)
}

/**
 * @param style eg: Typeface.BOLD
 */
fun SpannableString.setStyleSpan(
    style: Int,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
) {
    this.setSpan(StyleSpan(style), start, start + this.length, spannableStyle)
}

/**
 * @param relativeSize eg: 2.5f
 */
fun SpannableString.setRelativeFontSpan(
    relativeSize: Float,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_INCLUSIVE_EXCLUSIVE
) {
    this.setSpan(RelativeSizeSpan(relativeSize), start, start + this.length, spannableStyle)
}

/**
 * @param typeface eg: monospace
 */
fun SpannableString.setTypeFaceSpan(
    typeface: String,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
) {
    this.setSpan(TypefaceSpan(typeface), start, start + this.length, spannableStyle)
}

/**
 * 文字注解为url
 * @param url eg: "https://m.minghui.org"
 */
fun SpannableString.setUrlSpan(
    url: String,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
) {
    this.setSpan(URLSpan(url), start, start + this.length, spannableStyle)
}
