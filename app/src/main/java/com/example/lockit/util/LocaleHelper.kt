package com.example.lockit.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

/**
 * Lightweight per-app language switching that works on every API level without AppCompat.
 *
 * The chosen language tag is persisted in SharedPreferences and applied by wrapping the
 * activity's base context with an overridden locale (see MainActivity.attachBaseContext).
 * Switching language re-creates the activity so all resources reload.
 */
object LocaleHelper {
    private const val PREFS = "lockit_settings"
    private const val KEY_LANG = "app_language"

    /** Returns the saved language tag (e.g. "en", "pl"), or "" to follow the system. */
    fun getLanguage(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANG, "").orEmpty()

    fun persistLanguage(context: Context, lang: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, lang)
            .apply()
    }

    /** Wraps a base context with the persisted locale. Call from Activity.attachBaseContext. */
    fun wrap(context: Context): Context {
        val lang = getLanguage(context)
        if (lang.isBlank()) return context

        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /** Persists the language and recreates the hosting activity so the UI reloads in it. */
    fun setLanguage(context: Context, lang: String) {
        if (lang == getLanguage(context)) return
        persistLanguage(context, lang)
        findActivity(context)?.recreate()
    }

    private fun findActivity(context: Context): Activity? {
        var ctx: Context? = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}
