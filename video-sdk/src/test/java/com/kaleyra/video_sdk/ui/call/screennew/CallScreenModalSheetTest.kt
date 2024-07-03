package com.kaleyra.video_sdk.ui.call.screennew

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.fileshare.model.FileShareUiState
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.screennew.CallScreenModalSheet
import com.kaleyra.video_sdk.call.screennew.CallScreenModalSheetType
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.StackedSnackbarUiState
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalMaterial3Api::class)
class CallScreenModalSheetTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        mockkObject(AudioOutputViewModel)
        mockkObject(ScreenShareViewModel)
        mockkObject(FileShareViewModel)
        mockkObject(WhiteboardViewModel)
        mockkObject(VirtualBackgroundViewModel)
        mockkObject(UserMessagesViewModel)

        every { AudioOutputViewModel.provideFactory(any()) } returns mockk {
            every { create<AudioOutputViewModel>(any(), any()) } returns mockk<AudioOutputViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(AudioOutputUiState())
            }
        }
        every { ScreenShareViewModel.provideFactory(any()) } returns mockk {
            every { create<ScreenShareViewModel>(any(), any()) } returns mockk<ScreenShareViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(ScreenShareUiState())
            }
        }
        every { FileShareViewModel.provideFactory(any(), any()) } returns mockk {
            every { create<FileShareViewModel>(any(), any()) } returns mockk<FileShareViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(FileShareUiState())
            }
        }
        every { WhiteboardViewModel.provideFactory(any(), any()) } returns mockk {
            every { create<WhiteboardViewModel>(any(), any()) } returns mockk<WhiteboardViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(WhiteboardUiState())
            }
        }
        every { VirtualBackgroundViewModel.provideFactory(any()) } returns mockk {
            every { create<VirtualBackgroundViewModel>(any(), any()) } returns mockk<VirtualBackgroundViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(VirtualBackgroundUiState())
            }
        }
        every { UserMessagesViewModel.provideFactory(any(), any()) } returns mockk {
            every { create<UserMessagesViewModel>(any(), any()) } returns mockk<UserMessagesViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(StackedSnackbarUiState())
                every { userMessage } returns flowOf(ImmutableList(listOf(PinScreenshareMessage("username"))))
            }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun audioSheetType_audioComponentIsDisplayed() {
        composeTestRule.setContent {
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.Audio,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_audio_route_title)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
    }

    @Test
    fun screenShareSheetType_screenShareComponentIsDisplayed() {
        composeTestRule.setContent {
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.ScreenShare,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_screenshare_picker_title)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
    }

    @Test
    fun fileShareSheetType_fileShareComponentIsDisplayed() {
        composeTestRule.setContent {
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.FileShare,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
    }

    @Test
    fun whiteboardSheetType_whiteboardComponentIsDisplayed() {
        composeTestRule.setContent {
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.Whiteboard,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
    }

    @Test
    fun virtualBackgroundSheetType_virtualBackgroundComponentIsDisplayed() {
        composeTestRule.setContent {
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.VirtualBackground,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
    }

    @Test
    fun audioComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = spyk(rememberModalBottomSheetState())
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.Audio,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_audio_route_title)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        coVerify(exactly = 1) { sheetState!!.hide() }
    }

    @Test
    fun screenShareComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = spyk(rememberModalBottomSheetState())
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.ScreenShare,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_screenshare_picker_title)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        coVerify(exactly = 1) { sheetState!!.hide() }
    }

    @Test
    fun fileShareComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = spyk(rememberModalBottomSheetState())
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.FileShare,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        coVerify(exactly = 1) { sheetState!!.hide() }
    }

    @Test
    fun whiteboardComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = spyk(rememberModalBottomSheetState())
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.Whiteboard,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        coVerify(exactly = 1) { sheetState!!.hide() }
    }

    @Test
    fun virtualBackgroundComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = spyk(rememberModalBottomSheetState())
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.VirtualBackground,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        coVerify(exactly = 1) { sheetState!!.hide() }
    }

    @Test
    fun lifecycleStarted_dismissIsNotPerformed() {
        val lifecycleOwner = mockk<LifecycleOwner>(relaxed = true) {
            every { lifecycle.currentState } returns Lifecycle.State.STARTED
        }
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = spyk(rememberModalBottomSheetState())
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.VirtualBackground,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onUserMessageActionClick = {},
                lifecycleOwner = lifecycleOwner
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(false, dismissed)
        coVerify(exactly = 0) { sheetState!!.hide() }
    }

    @Test
    fun whiteboardPinStreamMessage_onUserMessageActionClickInvoked() {
        var clicked = false
        composeTestRule.setContent {
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.Whiteboard,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onUserMessageActionClick = { clicked = true }
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        val pinText = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(pinText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, clicked)
    }

    @Test
    fun fileSharePinStreamMessage_onUserMessageActionClickInvoked() {
        var clicked = false
        composeTestRule.setContent {
            CallScreenModalSheet(
                modalSheetType = CallScreenModalSheetType.FileShare,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onUserMessageActionClick = { clicked = true }
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        val pinText = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(pinText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, clicked)
    }

}
