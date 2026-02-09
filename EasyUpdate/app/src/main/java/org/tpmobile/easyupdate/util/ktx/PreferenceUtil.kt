package org.tpmobile.easyupdate.util.ktx

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import org.tpmobile.easyupdate.MyApp

/**
 * 版权：天地行论坛 tiandixing.org
 * 日期：2020年3月14日
 * 修改：第1次 2021年5月26日
 */
///////////////////////////////
//1.
///////////////////////////////
inline fun <reified T> getPref(@StringRes prefId: Int, defaultValue: T): T {
    PreferenceManager.getDefaultSharedPreferences(MyApp.appContext).run {
        val key = MyApp.appContext.getString(prefId)
        return when (defaultValue) {
            is String -> getString(key, defaultValue)
            is Boolean -> getBoolean(key, defaultValue)
            is Int -> getInt(key, defaultValue)
            is Long -> getLong(key, defaultValue)
            is Float -> getFloat(key, defaultValue)
            else -> throw IllegalArgumentException("类型不被支持")
        } as T
    }
}

inline fun <reified T> getPref(key: String, defaultValue: T): T {
    PreferenceManager.getDefaultSharedPreferences(MyApp.appContext).run {
        return when (defaultValue) {
            is String -> getString(key, defaultValue)
            is Boolean -> getBoolean(key, defaultValue)
            is Int -> getInt(key, defaultValue)
            is Long -> getLong(key, defaultValue)
            is Float -> getFloat(key, defaultValue)
            else -> throw IllegalArgumentException("类型不被支持")
        } as T
    }
}

fun setPref(@StringRes preferenceId: Int, value: Any) {
    MyApp.appContext.run {
        val key = getString(preferenceId)
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                else -> throw IllegalArgumentException("类型不被支持")
            }
            commit()
        }
    }
}

fun setPref(key: String, value: Any) {
    MyApp.appContext.run {
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                else -> throw IllegalArgumentException("类型不被支持")
            }
            commit()
        }
    }
}

fun removePref(@StringRes preferenceId: Int) {
    val key = MyApp.appContext.getString(preferenceId)
    PreferenceManager.getDefaultSharedPreferences(MyApp.appContext).edit {
        remove(key)
        commit()
    }
}

fun removePref(key: String) {
    PreferenceManager.getDefaultSharedPreferences(MyApp.appContext).edit {
        remove(key)
        commit()
    }
}

///////////////////////////////
//2.
///////////////////////////////
inline fun <reified T> Context.getPref(@StringRes prefId: Int, defaultValue: T): T {
    val key = this.getString(prefId, "")
    PreferenceManager.getDefaultSharedPreferences(this).run {
        return when (defaultValue) {
            is String -> getString(key, defaultValue)
            is Boolean -> getBoolean(key, defaultValue)
            is Int -> getInt(key, defaultValue)
            is Long -> getLong(key, defaultValue)
            is Float -> getFloat(key, defaultValue)
            else -> throw IllegalArgumentException("类型不被支持")
        } as T
    }
}

inline fun <reified T> Context.getPref(key: String, defaultValue: T): T {
    PreferenceManager.getDefaultSharedPreferences(this).run {
        return when (defaultValue) {
            is String -> getString(key, defaultValue)
            is Boolean -> getBoolean(key, defaultValue)
            is Int -> getInt(key, defaultValue)
            is Long -> getLong(key, defaultValue)
            is Float -> getFloat(key, defaultValue)
            else -> throw IllegalArgumentException("类型不被支持")
        } as T
    }
}

fun Context.setPref(@StringRes preferenceId: Int, value: Any) {
    val key = getString(preferenceId)
    PreferenceManager.getDefaultSharedPreferences(this).edit {
        when (value) {
            is String -> putString(key, value)
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            else -> throw IllegalArgumentException("类型不被支持")
        }
        commit()
    }
}

fun Context.setPref(key: String, value: Any) {
    PreferenceManager.getDefaultSharedPreferences(this).edit {
        when (value) {
            is String -> putString(key, value)
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            else -> throw IllegalArgumentException("类型不被支持")
        }
        commit()
    }
}

fun Context.removePref(@StringRes preferenceId: Int) {
    val key = getString(preferenceId)
    PreferenceManager.getDefaultSharedPreferences(this).edit {
        remove(key)
        commit()
    }
}

fun Context.removePref(key: String) {
    PreferenceManager.getDefaultSharedPreferences(this).edit {
        remove(key)
        commit()
    }
}

///////////////////////////////
//3.
///////////////////////////////
fun getPrefDefaultBooleanValue(@StringRes defaultId: Int): Boolean {
    return getPrefDefaultStringValue(defaultId) == "true"
}

fun getPrefDefaultStringValue(@StringRes defaultId: Int): String {
    return MyApp.appContext.getString(defaultId)
}

