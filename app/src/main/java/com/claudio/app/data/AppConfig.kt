package com.claudio.app.data

import com.claudio.app.BuildConfig

/**
 * Build-time defaults for Claudio.
 *
 * Release builds receive the built-in free key through the FREE_API_KEY
 * GitHub Actions secret, so the app works out of the box. BYOK (bring your own
 * key) is optional and always takes priority when set.
 */
object AppConfig {
    val FREE_API_KEY: String = BuildConfig.FREE_API_KEY

    const val DEFAULT_NAME = "saint"
    const val USER_AGENT = "https://github.com/saintedKyle/claudio"
}
