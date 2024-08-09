/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.video_common_ui.CompanyUI.Theme
import com.kaleyra.video_common_ui.KaleyraFontFamily
import com.kaleyra.video_sdk.extensions.darkColorSchemeFrom
import com.kaleyra.video_sdk.extensions.lightColorSchemeFrom

internal val defaultTypography = androidx.compose.material3.Typography()
internal val typography = androidx.compose.material3.Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = KaleyraFontFamily.default),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = KaleyraFontFamily.default),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = KaleyraFontFamily.default),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = KaleyraFontFamily.default),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = KaleyraFontFamily.default),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = KaleyraFontFamily.default),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = KaleyraFontFamily.default),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = KaleyraFontFamily.default),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = KaleyraFontFamily.default),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = KaleyraFontFamily.default),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = KaleyraFontFamily.default),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = KaleyraFontFamily.default),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = KaleyraFontFamily.default),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = KaleyraFontFamily.default),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = KaleyraFontFamily.default)
)

/**
 * Composable function to generate the Collaboration M3 Theme
 * @param theme Theme input theme
 * @param lightStatusBarIcons Boolean flag indicating if the system bars should be transparent, true if transparent, false otherwise
 * @param content [@androidx.compose.runtime.Composable] Function1<[@kotlin.ParameterName] Boolean, Unit> composable callback with isDarkTheme as input flag
 */
@Composable
fun CollaborationTheme(
    theme: Theme,
    lightStatusBarIcons: Boolean = false,
    content: @Composable (isDarkTheme: Boolean) -> Unit
) {
    val systemUiController = rememberSystemUiController()

    val lightColors = theme.day.colors
    val darkColors = theme.night.colors
    val isSystemDarkTheme = isSystemInDarkTheme()
    val isDarkTheme by remember(theme) {
        derivedStateOf {
            when (theme.defaultStyle) {
                Theme.DefaultStyle.Day -> false
                Theme.DefaultStyle.Night -> true
                else -> isSystemDarkTheme
            }
        }
    }


    val colors = when {
        isDarkTheme -> when(darkColors) {
            is Theme.Colors.Seed -> darkColorSchemeFrom(darkColors.color)
            else -> darkColorSchemeFrom(kaleyra_m3_seed.toArgb())

        }
        else ->  when(lightColors) {
            is Theme.Colors.Seed -> lightColorSchemeFrom(lightColors.color)
            else -> lightColorSchemeFrom(kaleyra_m3_seed.toArgb())

        }
    }
    val kaleyraColors = when {
        isDarkTheme -> darkKaleyraColors()
        else -> lightKaleyraColors()
    }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !lightStatusBarIcons && !isDarkTheme
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = !isDarkTheme,
            navigationBarContrastEnforced = false
        )
    }

    CompositionLocalProvider(value = LocalKaleyraColors provides kaleyraColors) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            content = { content(isDarkTheme) }
        )
    }
}

@Immutable
internal data class KaleyraColors(
    val warning: Color = Color(0xFFFFD02B),
    val onWarning: Color = Color.White,
    val hangUp: Color = Color(0xFFE11900),
    val onHangUp: Color = Color.White,
    val answer: Color = Color(0xFF1A7924),
    val onAnswer: Color = Color.White
)

internal val LocalKaleyraColors = compositionLocalOf { KaleyraColors() }

internal fun lightKaleyraColors(
    warning: Color = Color(0xFFFFD02B),
    onWarning: Color = Color.White,
    hangUp: Color = Color(0xFFE11900),
    onHangUp: Color = Color.White,
    answer: Color = Color(0xFF1A7924),
    onAnswer: Color = Color.White
) = KaleyraColors(
    warning = warning,
    onWarning = onWarning,
    hangUp = hangUp,
    onHangUp = onHangUp,
    answer = answer,
    onAnswer = onAnswer
)

internal fun darkKaleyraColors(
    warning: Color = Color(0xFFFFD02B),
    onWarning: Color = Color.White,
    hangUp: Color = Color(0xFFAE1300),
    onHangUp: Color = Color.White,
    answer: Color = Color(0xFF1A7924),
    onAnswer: Color = Color.White
) = KaleyraColors(
    warning = warning,
    onWarning = onWarning,
    hangUp = hangUp,
    onHangUp = onHangUp,
    answer = answer,
    onAnswer = onAnswer
)

// TODO decide if this object will be used for a custom theme, or only some colors, so it's overkill
internal object KaleyraTheme {

    val colors: KaleyraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKaleyraColors.current
}

/**
 * Composable function to build the Kaleyra M3 Theme
 * @param content [@androidx.compose.runtime.Composable] Function0<Unit> composable callback called when the content should be rendered
 */
@Composable
fun KaleyraTheme(content: @Composable (isDarkTheme: Boolean) -> Unit) {
    val theme = Theme(
        day = Theme.Style(colors = Theme.Colors.Seed(color = kaleyra_m3_seed.toArgb())),
        night = Theme.Style(colors = Theme.Colors.Seed(color = kaleyra_m3_seed.toArgb()))
    )
    CollaborationTheme(theme = theme, content = content)
}

private val TermsDarkColorScheme = darkColorScheme(
    primary = Color.White,
    surface = Color(0xFF0E0E0E),
    onPrimary = Color.Black,
    onSurface = Color.White
)

private val TermsLightColorScheme = lightColorScheme(
    primary = Color.Black,
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

/**
 * Composable function to build the Kaleyra Theme
 * @param isDarkTheme Boolean flag indicating if dark theme should be used, true if dark theme should be used, false otherwise
 * @param content [@androidx.compose.runtime.Composable] Function0<Unit> composable callback called when the content should be rendered
 */
@Composable
fun TermsAndConditionsTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isDarkTheme -> TermsDarkColorScheme
        else -> TermsLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
