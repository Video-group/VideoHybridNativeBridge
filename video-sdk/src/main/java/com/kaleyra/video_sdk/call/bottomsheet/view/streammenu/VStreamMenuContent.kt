package com.kaleyra.video_sdk.call.bottomsheet.view.streammenu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactions.view.CancelAction
import com.kaleyra.video_sdk.call.callactions.view.FullscreenAction
import com.kaleyra.video_sdk.call.callactions.view.PinAction
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun VStreamMenuContent(
    selectedStreamId: String,
    onDismiss: () -> Unit,
    onFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StreamViewModel = viewModel<StreamViewModel>(
        factory = StreamViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFullscreenMenu by remember { mutableStateOf(false) }

    val onFullscreenClick: (Boolean) -> Unit = { isFullscreen ->
        if (isFullscreen) {
            viewModel.fullscreen(null)
            onDismiss()
        } else viewModel.fullscreen(selectedStreamId)
        showFullscreenMenu = true
        onFullscreen()
    }
    val isPinLimitReached by remember(viewModel) {
        derivedStateOf {
            uiState.pinnedStreams.count() >= viewModel.maxPinnedStreams
        }
    }

    BackHandler(enabled = showFullscreenMenu) {
        onFullscreenClick(true)
    }
    
    VStreamMenuContent(
        isFullscreen = uiState.fullscreenStream?.id == selectedStreamId,
        isPinned = uiState.pinnedStreams.value.fastAny { stream -> stream.id == selectedStreamId },
        isPinLimitReached = isPinLimitReached,
        onCancelClick = onDismiss,
        onFullscreenClick = { isFullscreen -> onFullscreenClick(isFullscreen) },
        onPinClick = { isPinned ->
            if (isPinned) viewModel.unpin(selectedStreamId)
            else viewModel.pin(selectedStreamId)
            onDismiss()
        },
        modifier = modifier
    )
}

@Composable
internal fun VStreamMenuContent(
    isFullscreen: Boolean,
    isPinned: Boolean,
    isPinLimitReached: Boolean,
    onCancelClick: () -> Unit,
    onFullscreenClick: (Boolean) -> Unit,
    onPinClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(14.dp)) {
        if (!isFullscreen) {
            CancelAction(
                label = false,
                onClick = onCancelClick
            )
            Spacer(modifier = Modifier.height(SheetItemsSpacing))
            PinAction(
                label = false,
                pin = isPinned,
                enabled = isPinned || !isPinLimitReached,
                onClick = { onPinClick(isPinned) }
            )
            Spacer(modifier = Modifier.height(SheetItemsSpacing))
        }
        FullscreenAction(
            label = false,
            fullscreen = isFullscreen,
            onClick = { onFullscreenClick(isFullscreen) }
        )
    }
}

@MultiConfigPreview
@Composable
internal fun VStreamMenuContentPreview() {
    KaleyraTheme {
        Surface {
            VStreamMenuContent(false, false, false,  {}, {}, {})
        }
    }
}

@MultiConfigPreview
@Composable
internal fun VStreamFullscreenMenuContentPreview() {
    KaleyraTheme {
        Surface {
            VStreamMenuContent(true, false, false, {}, {}, {})
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun VStreamPinLimitMenuContentPreview() {
    KaleyraTheme {
        Surface {
            VStreamMenuContent(false, false, true, {}, {}, {})
        }
    }
}