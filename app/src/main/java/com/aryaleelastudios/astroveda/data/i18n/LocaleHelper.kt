package com.aryaleelastudios.astroveda.data.i18n

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

class LocalizedContextWrapper(
    base: Context,
    private val localizedResources: Resources
) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedResources
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

object LocaleHelper {
    fun localizedContext(baseContext: Context, languageCode: String): Context {
        val resolvedCode = LanguageRegistry.resolveOrDefault(languageCode).code
        val locale = Locale(resolvedCode)
        val config = Configuration(baseContext.resources.configuration).apply {
            setLocale(locale)
        }
        return baseContext.createConfigurationContext(config)
    }

    fun wrapWithLocale(baseContext: Context, languageCode: String): Context {
        val configContext = localizedContext(baseContext, languageCode)
        return LocalizedContextWrapper(baseContext, configContext.resources)
    }
}
