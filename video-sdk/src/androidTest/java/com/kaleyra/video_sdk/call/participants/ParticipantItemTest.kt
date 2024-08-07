package com.kaleyra.video_sdk.call.participants

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.view.ParticipantItem
import com.kaleyra.video_sdk.call.streamnew.model.core.AudioUi
import com.kaleyra.video_sdk.call.streamnew.model.core.VideoUi
import com.kaleyra.video_sdk.call.streamnew.model.core.streamUiMock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ParticipantItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var stream by mutableStateOf(streamUiMock)

    private var pinned by mutableStateOf(false)

    private var isAdminStream by mutableStateOf(false)

    private var amIAdmin by mutableStateOf(false)

    private var streamId: String? = null

    private var isStreamMuted = false

    private var isStreamMicDisabled = false

    private var isStreamPinned = false

    private var isMoreClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ParticipantItem(
                stream = stream,
                pinned = pinned,
                isAdminStream = isAdminStream,
                amIAdmin = amIAdmin,
                onMuteStreamClick = { streamId, value ->
                    this@ParticipantItemTest.streamId = streamId
                    isStreamMuted = value
                },
                onDisableMicClick = { streamId, value ->
                    this@ParticipantItemTest.streamId = streamId
                    isStreamMicDisabled = value
                },
                onPinStreamClick = { streamId, value ->
                    this@ParticipantItemTest.streamId = streamId
                    isStreamPinned = value
                },
                onMoreClick = {
                    isMoreClicked = true
                }
            )
        }
    }

    @After
    fun tearDown() {
        stream = streamUiMock
        pinned = false
        isAdminStream = false
        amIAdmin = false
        isMoreClicked = false
    }

    @Test
    fun avatarFailsToLoad_letterIsDisplayed() {
        stream = streamUiMock.copy(username = "username", avatar = null)
        composeTestRule.onNodeWithText("U").assertIsDisplayed()
    }

    @Test
    fun testYouIsDisplayed() {
        stream = streamUiMock.copy(isMine = true)
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_you, stream.username)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testUsernameIsDisplayed() {
        composeTestRule.onNodeWithText(stream.username).assertIsDisplayed()
    }

    @Test
    fun testAdminTextIsDisplayed() {
        isAdminStream = true
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_admin)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testScreenShareTextIsDisplayed() {
        stream = streamUiMock.copy(video = VideoUi(id = "id", isScreenShare = true))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_screenshare)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testScreenShareTextIsDisplayedWhenUserAdmin() {
        isAdminStream = true
        stream = streamUiMock.copy(video = VideoUi(id = "id", isScreenShare = true))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_screenshare)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testParticipantTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_participant)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun streamIsMineAndAudioIsDisabled_enableMicButtonIsDisplayed() {
        stream = streamUiMock.copy(isMine = true, audio = AudioUi(id = "id", isEnabled = false))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_enable_microphone)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun streamIsMineAndAudioIsEnabled_disableMicButtonIsDisplayed() {
        stream = streamUiMock.copy(isMine = true, audio = AudioUi(id = "id", isEnabled = true))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_disable_microphone)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmAdminAndAudioIsDisabled_enableMicButtonIsDisplayed() {
        stream = streamUiMock.copy(isMine = true, audio = AudioUi(id = "id", isEnabled = false))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_enable_microphone)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmAdminAndAudioIsEnabled_disableMicButtonIsDisplayed() {
        stream = streamUiMock.copy(isMine = true, audio = AudioUi(id = "id", isEnabled = true))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_disable_microphone)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmNotAdminAndStreamIsNotMineAndAudioIsNotMuted_muteAudioForMeButtonIsDisplayed() {
        amIAdmin = false
        stream = streamUiMock.copy(isMine = false, audio = AudioUi("id", isMutedForYou = false))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmNotAdminAndStreamIsNotMineAndAudioIsMuted_unmuteAudioForMeButtonIsDisplayed() {
        amIAdmin = false
        stream = streamUiMock.copy(isMine = false, audio = AudioUi("id", isMutedForYou = true))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun streamIsMineAndIsPinned_unpinButtonIsDisplayed() {
        stream = streamUiMock.copy(isMine = true)
        pinned = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun streamIsMineAndIsNotPinned_pinButtonIsDisplayed() {
        stream = streamUiMock.copy(isMine = true)
        pinned = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmNotAdminAndIsPinned_unpinButtonIsDisplayed() {
        amIAdmin = false
        pinned = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmNotAdminAndIsNotPinned_pinButtonIsDisplayed() {
        amIAdmin = false
        pinned = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmAdminAndStreamIsNotMine_showMoreActionsButtonIsDisplayed() {
        amIAdmin = true
        stream = streamUiMock.copy(isMine = false)
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_show_more_actions)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun screenShareStream_micButtonIsNotDisplayed() {
        stream = streamUiMock.copy(video = VideoUi(id = "id", isEnabled = true, isScreenShare = true))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_disable_microphone)
        composeTestRule.onNodeWithContentDescription(description).assertDoesNotExist()
    }

    @Test
    fun localScreenShareStream_unpinButtonIsNotDisplayed() {
        stream = streamUiMock.copy(isMine = true, video = VideoUi(id = "id", isEnabled = true, isScreenShare = true))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream)
        composeTestRule.onNodeWithContentDescription(description).assertDoesNotExist()
    }

    @Test
    fun testOnMuteStreamClick() {
        amIAdmin = false
        stream = streamUiMock.copy(id = "customStreamId", isMine = false, audio = AudioUi("id", isMutedForYou = false))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals("customStreamId", streamId)
        assertEquals(true, isStreamMuted)
    }

    @Test
    fun testOnUnMuteStreamClick() {
        amIAdmin = false
        stream = streamUiMock.copy(id = "customStreamId", isMine = false, audio = AudioUi("id", isMutedForYou = true))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals("customStreamId", streamId)
        assertEquals(false, isStreamMuted)
    }

    @Test
    fun testOnDisableMicClick() {
        stream = streamUiMock.copy(id = "customStreamId", isMine = true, audio = AudioUi(id = "id", isEnabled = true))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_disable_microphone)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals("customStreamId", streamId)
        assertEquals(true, isStreamMicDisabled)
    }

    @Test
    fun testOnEnableMicClick() {
        stream = streamUiMock.copy(id = "customStreamId", isMine = true, audio = AudioUi(id = "id", isEnabled = false))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_enable_microphone)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals("customStreamId", streamId)
        assertEquals(false, isStreamMicDisabled)
    }

    @Test
    fun testOnUnpinStreamClick() {
        stream = streamUiMock.copy(id = "customStreamId", isMine = true)
        pinned = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals("customStreamId", streamId)
        assertEquals(false, isStreamPinned)
    }

    @Test
    fun testOnPinStreamClick() {
        stream = streamUiMock.copy(id = "customStreamId", isMine = true)
        pinned = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals("customStreamId", streamId)
        assertEquals(true, isStreamPinned)
    }

    @Test
    fun testOnMoreClick() {
        amIAdmin = true
        stream = streamUiMock.copy(isMine = false)
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_show_more_actions)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, isMoreClicked)
    }
}