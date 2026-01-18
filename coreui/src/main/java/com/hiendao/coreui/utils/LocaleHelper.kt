package com.hiendao.coreui.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.preference.PreferenceManager
import java.util.Locale

object LocaleHelper {

    fun onAttach(context: Context, defaultLanguage: String): Context {
        val lang = getPersistedData(context, defaultLanguage)
        return setLocale(context, lang)
    }

    fun onAttach(context: Context): Context {
        val lang = getPersistedData(context, Locale.getDefault().language)
        return setLocale(context, lang)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(context, Locale.getDefault().language)
    }
    
    // Note: We will access AppPreferences via Dependency Injection in ViewModel,
    // but for ContextWrapper usage in BaseContext of Activity/Application, we might need direct SharedPreferences access 
    // or pass the language explicitly.
    // For simplicity, let's assume this helper might read from SharedPreferences directly if needed,
    // or we rely on AppPreferences pushing the change.
    
    // However, AppPreferences is the source of truth. 
    // To avoid circular dependency or complexity, let's just make setLocale logic here.
    
    fun setLocale(context: Context, language: String): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else {
            updateResourcesLegacy(context, language)
        }
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String {
        // This is a simplified read. In reality we should use AppPreferences.
        // But since this is a static helper often used before Dagger graph is fully ready in attachBaseContext,
        // we might read raw prefs.
        val preferences = context.getSharedPreferences("StoryApp", Context.MODE_PRIVATE)
        return preferences.getString("APP_LANGUAGE", defaultLanguage) ?: defaultLanguage
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}
