package com.example.lockit.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val PREFS = "lockit_settings"
    private const val KEY_LANG = "app_language"

    fun getLanguage(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANG, "").orEmpty()

    fun persistLanguage(context: Context, lang: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, lang)
            .apply()
    }

    fun wrap(context: Context): Context {
        val lang = getLanguage(context)
        if (lang.isBlank()) return context

        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

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
