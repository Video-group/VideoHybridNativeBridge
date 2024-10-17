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

package com.kaleyra.video_common_ui

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import com.kaleyra.video.Company
import com.kaleyra.video_common_ui.CompanyUI.Theme.Style
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CompanyThemeManagerTest {

    private val company = mockk<Company>()
    private val theme = mockk<Company.Theme>()

    private val remoteDayLogo = mockk<Uri>()
    private val remoteNightLogo = mockk<Uri>()

    private val remoteDayColors =  Company.Theme.Style.Colors.Seed(Color.Red.toArgb())
    private val remoteNightColors =  Company.Theme.Style.Colors.Seed(Color.Blue.toArgb())

    private val remoteDayStyle = Style(logo = remoteDayLogo, colors = remoteDayColors)
    private val remoteNightStyle = Style(logo = remoteNightLogo, colors = remoteNightColors)

    @Before
    fun setUp() {
        mockkObject(KaleyraVideo)
        every { company.theme } returns MutableStateFlow(theme)
        every { theme.day } returns remoteDayStyle
        every { theme.night } returns remoteNightStyle
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `remote company details`() = runTest {
        every { KaleyraVideo.theme } returns null
        val defaultTheme = CompanyUI.Theme()
        val expected = CompanyUI.Theme(
            day = defaultTheme.day.copy(logo = remoteDayLogo, colors = remoteDayColors),
            night = defaultTheme.night.copy(logo = remoteNightLogo, colors = remoteNightColors)
        )
        assertEquals(expected, company.combinedTheme.first())
    }

    @Test
    fun `set local company details`() = runTest {
        val dayLogo = mockk<Uri>()
        val nightLogo = mockk<Uri>()
        val companyUITheme = CompanyUI.Theme(
            fontFamily = FontFamily.SansSerif,
            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
            day = Style(logo = dayLogo, colors = Company.Theme.Style.Colors.Seed(color = Color.Red.toArgb())),
            night = Style(logo = nightLogo, colors = Company.Theme.Style.Colors.Seed(color = Color.Yellow.toArgb()))
        )
        every { KaleyraVideo.theme } returns companyUITheme
        assertEquals(companyUITheme, company.combinedTheme.first())
    }

    @Test
    fun `theme uses remote logo if local logo is not defined`() = runTest {
        val companyUITheme = CompanyUI.Theme(
            fontFamily = FontFamily.SansSerif,
            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
            day = Style(logo = null, colors = Company.Theme.Style.Colors.Seed(color = Color.Red.toArgb())),
            night = Style(logo = null, colors = Company.Theme.Style.Colors.Seed(color = Color.Yellow.toArgb()))
        )
        every { KaleyraVideo.theme } returns companyUITheme
        val expected = companyUITheme.copy(
            day = companyUITheme.day.copy(logo = remoteDayStyle.logo, colors = Company.Theme.Style.Colors.Seed(color = Color.Red.toArgb())),
            night = companyUITheme.night.copy(logo = remoteNightStyle.logo, colors = Company.Theme.Style.Colors.Seed(color = Color.Yellow.toArgb()))
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }

    @Test
    fun `theme uses remote colors if local colors are not defined`() = runTest {
        val dayLogo = mockk<Uri>()
        val nightLogo = mockk<Uri>()
        val companyUITheme = CompanyUI.Theme(
            fontFamily = FontFamily.SansSerif,
            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
            day = Style(logo = dayLogo, colors = null),
            night = Style(logo = nightLogo, colors = null)
        )
        every { KaleyraVideo.theme } returns companyUITheme
        val expected = companyUITheme.copy(
            day = companyUITheme.day.copy(logo = dayLogo, colors = remoteDayColors),
            night = companyUITheme.night.copy(logo = nightLogo, colors = remoteNightColors)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }
}