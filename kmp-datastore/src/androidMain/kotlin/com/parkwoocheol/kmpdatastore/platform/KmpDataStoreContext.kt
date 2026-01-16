package com.parkwoocheol.kmpdatastore.platform

import android.content.Context

/**
 * Context holder for Android platform.
 *
 * Must be initialized before creating any DataStore instances.
 *
 * Example (in Application class):
 * ```
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         KmpDataStoreContext.init(this)
 *     }
 * }
 * ```
 */
object KmpDataStoreContext {
    private var appContext: Context? = null

    /**
     * Initializes the Android context.
     * Should be called once in the Application onCreate() method.
     *
     * @param context The application context
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Gets the application context.
     *
     * @return The application context
     * @throws IllegalStateException if context has not been initialized
     */
    internal fun get(): Context {
        return appContext
            ?: throw IllegalStateException(
                "KmpDataStoreContext not initialized. " +
                    "Call KmpDataStoreContext.init(context) in your Application.onCreate()",
            )
    }
}
