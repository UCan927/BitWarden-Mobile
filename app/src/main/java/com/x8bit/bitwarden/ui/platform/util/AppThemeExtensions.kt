package com.x8bit.bitwarden.ui.platform.util

import com.x8bit.bitwarden.R
import com.x8bit.bitwarden.ui.platform.base.util.Text
import com.x8bit.bitwarden.ui.platform.base.util.asText
import com.x8bit.bitwarden.ui.platform.feature.settings.appearance.model.AppTheme

/**
 * Returns a human-readable display label for the given [AppTheme].
 */
val AppTheme.displayLabel: Text
    get() = when (this) {
        AppTheme.DEFAULT -> R.string.default_system.asText()
        AppTheme.DARK -> R.string.dark.asText()
        AppTheme.LIGHT -> R.string.light.asText()
    }
