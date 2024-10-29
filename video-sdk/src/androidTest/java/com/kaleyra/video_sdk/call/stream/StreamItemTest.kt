package com.kaleyra.video_sdk.call.stream

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.assertBottomPositionInRootIsEqualTo
import com.kaleyra.video_sdk.assertRightPositionInRootIsEqualTo
import com.kaleyra.video_sdk.assertTopPositionInRootIsEqualTo
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.model.core.streamUiMock
import com.kaleyra.video_sdk.call.stream.view.items.StreamItem
import com.kaleyra.video_sdk.call.stream.view.items.StreamItemPadding
import com.kaleyra.video_sdk.call.stream.view.items.ZoomIconTestTag
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StreamItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var stream by mutableStateOf(streamUiMock)

    private var pin by mutableStateOf(false)

    private var fullscreen by mutableStateOf(false)

    private var statusIconsAlignment by mutableStateOf(Alignment.BottomEnd)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamItem(
                stream = stream,
                fullscreen = fullscreen,
                pin = pin,
                statusIconsAlignment = statusIconsAlignment
            )
        }
    }

    @After
    fun tearDown() {
        stream = streamUiMock
        pin = false
        fullscreen = false
    }

    @Test
    fun testUsernameIsDisplayed() {
        composeTestRule.onNodeWithText(stream.username).assertIsDisplayed()
    }

    @Test
    fun viewNull_avatarIsDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = null))
        composeTestRule.onNodeWithText(stream.username[0].uppercase()).assertIsDisplayed()
    }

    @Test
    fun videoNotEnabled_avatarIsDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), isEnabled = false))
        composeTestRule.onNodeWithText(stream.username[0].uppercase()).assertIsDisplayed()
    }

    @Test
    fun videoEnabled_avatarIsNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), isEnabled = true))
        composeTestRule.onNodeWithText(stream.username[0].uppercase()).assertDoesNotExist()
    }

    @Test
    fun pinnedStream_pinIconIsDisplayed() {
        pin = true
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_pin)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun unpinnedStream_pinIconDoesNotExist() {
        pin = false
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_pin)
        composeTestRule.onNodeWithContentDescription(text).assertDoesNotExist()
    }

    @Test
    fun streamAudioDisabled_muteIconIsDisplayed() {
        stream = stream.copy(audio = AudioUi("1", isEnabled = false))
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun streamAudioNull_muteIconIsDisplayed() {
        stream = stream.copy(audio = null)
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun streamAudioEnabled_muteIconDoesNotExist() {
        stream = stream.copy(audio = AudioUi("1", isEnabled = false))
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun streamAudioDisabledAndMutedForYou_mutedForYouIconDoesNotExists() {
        stream = stream.copy(audio = AudioUi("1", isEnabled = true, isMutedForYou = false))
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithContentDescription(text).assertDoesNotExist()
    }

    @Test
    fun streamMutedForYouAndNotMine_mutedForYouIconIsDisplayed() {
        stream = stream.copy(audio = AudioUi("1", isEnabled = false, isMutedForYou = true))
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun streamMutedForYouAndMine_muteIconIsDisplayed() {
        stream = stream.copy(audio = AudioUi("1", isEnabled = false, isMutedForYou = true), isMine = true)
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun streamNotMutedForYou_mutedForYouIconDoesNotExists() {
        stream = stream.copy(audio = AudioUi("1", isEnabled = true, isMutedForYou = false))
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithContentDescription(text).assertDoesNotExist()
    }

    @Test
    fun fullscreenStream_fullscreenIconIsDisplayed() {
        fullscreen = true
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_fullscreen)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun nonFullscreenStream_fullscreenIconDoesNotExits() {
        fullscreen = false
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_fullscreen)
        composeTestRule.onNodeWithContentDescription(text).assertDoesNotExist()
    }

    @Test
    fun localStream_youAsUsernameIsDisplayed() {
        stream = stream.copy(isMine = true)
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_you)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun remoteStream_youAsUsernameDoesNotExits() {
        stream = stream.copy(isMine = false)
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_you)
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun testBottomEndStatusIconsAlignment() {
        statusIconsAlignment = Alignment.BottomEnd
        stream = stream.copy(audio = null)
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_mic_disabled)
        val fullscreenNode = composeTestRule.onNodeWithContentDescription(text).onParent()
        val parentBottom = fullscreenNode.onParent().getBoundsInRoot().bottom - StreamItemPadding
        val parentRight = fullscreenNode.onParent().getBoundsInRoot().right - StreamItemPadding
        fullscreenNode
            .assertIsDisplayed()
            .assertBottomPositionInRootIsEqualTo(parentBottom)
            .assertRightPositionInRootIsEqualTo(parentRight)
    }

    @Test
    fun testTopEndStatusIconsAlignment() {
        statusIconsAlignment = Alignment.TopEnd
        stream = stream.copy(audio = null)
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_mic_disabled)
        val fullscreenNode = composeTestRule.onNodeWithContentDescription(text).onParent()
        val parentTop = fullscreenNode.onParent().getBoundsInRoot().top + StreamItemPadding
        val parentRight = fullscreenNode.onParent().getBoundsInRoot().right - StreamItemPadding
        fullscreenNode
            .assertIsDisplayed()
            .assertTopPositionInRootIsEqualTo(parentTop)
            .assertRightPositionInRootIsEqualTo(parentRight)
    }

    @Test
    fun testTopStartStatusIconsAlignment() {
        statusIconsAlignment = Alignment.TopStart
        stream = stream.copy(audio = null)
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_mic_disabled)
        val fullscreenNode = composeTestRule.onNodeWithContentDescription(text).onParent()
        val parentTop = fullscreenNode.onParent().getBoundsInRoot().top + StreamItemPadding
        val parentLeft = fullscreenNode.onParent().getBoundsInRoot().left + StreamItemPadding
        fullscreenNode
            .assertIsDisplayed()
            .assertTopPositionInRootIsEqualTo(parentTop)
            .assertLeftPositionInRootIsEqualTo(parentLeft)
    }

    @Test
    fun testBottomStartStatusIconsAlignment() {
        statusIconsAlignment = Alignment.BottomStart
        stream = stream.copy(audio = null)
        val text = composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_stream_mic_disabled)
        val fullscreenNode = composeTestRule.onNodeWithContentDescription(text).onParent()
        val parentBottom = fullscreenNode.onParent().getBoundsInRoot().bottom - StreamItemPadding
        val parentLeft = fullscreenNode.onParent().getBoundsInRoot().left + StreamItemPadding
        fullscreenNode
            .assertIsDisplayed()
            .assertBottomPositionInRootIsEqualTo(parentBottom)
            .assertLeftPositionInRootIsEqualTo(parentLeft)
    }

    @Test
    fun videoNotPresent_zoomIconNotDisplayed() {
        stream = stream.copy(video = null)
        composeTestRule.onNodeWithTag(ZoomIconTestTag).assertIsNotDisplayed()
    }

    @Test
    fun videoDisabled_zoomIconNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), isEnabled = false))
        composeTestRule.onNodeWithTag(ZoomIconTestTag).assertIsNotDisplayed()
    }

    @Test
    fun videoPresent_zoomLevelFit_zoomIconNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), zoomLevelUi = VideoUi.ZoomLevelUi.Fit, isEnabled = true))
        composeTestRule.onNodeWithTag(ZoomIconTestTag).assertIsNotDisplayed()
    }

    @Test
    fun videoPresent_zoomLevelFill_zoomIconNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), zoomLevelUi = VideoUi.ZoomLevelUi.Fill, isEnabled = true))
        composeTestRule.onNodeWithTag(ZoomIconTestTag).assertIsNotDisplayed()
    }

    @Test
    fun videoPresent_zoomLevel2x_zoomIconNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), zoomLevelUi = VideoUi.ZoomLevelUi.`2x`, isEnabled = true))
        composeTestRule.onNodeWithTag(ZoomIconTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithText("2x").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Zoom 2x").assertIsDisplayed()
    }

    @Test
    fun videoPresent_zoomLevel3x_zoomIconNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), zoomLevelUi = VideoUi.ZoomLevelUi.`3x`, isEnabled = true))
        composeTestRule.onNodeWithTag(ZoomIconTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithText("3x").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Zoom 3x").assertIsDisplayed()
    }

    @Test
    fun videoPresent_zoomLevel4x_zoomIconNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), zoomLevelUi = VideoUi.ZoomLevelUi.`4x`, isEnabled = true))
        composeTestRule.onNodeWithTag(ZoomIconTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithText("4x").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Zoom 4x").assertIsDisplayed()
    }

    @Test
    fun videoPresent_zoomLevel5x_zoomIconNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), zoomLevelUi = VideoUi.ZoomLevelUi.`5x`, isEnabled = true))
        composeTestRule.onNodeWithTag(ZoomIconTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithText("5x").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Zoom 5x").assertIsDisplayed()
    }

    @Test
    fun videoPresent_zoomLevel6x_zoomIconNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), zoomLevelUi = VideoUi.ZoomLevelUi.`6x`, isEnabled = true))
        composeTestRule.onNodeWithTag(ZoomIconTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithText("6x").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Zoom 6x").assertIsDisplayed()
    }

    @Test
    fun videoPresent_zoomLevel7x_zoomIconNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(VideoStreamView(composeTestRule.activity)), zoomLevelUi = VideoUi.ZoomLevelUi.`7x`, isEnabled = true))
        composeTestRule.onNodeWithTag(ZoomIconTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithText("7x").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Zoom 7x").assertIsDisplayed()
    }
}
