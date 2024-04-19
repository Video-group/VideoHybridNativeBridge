package com.kaleyra.video_sdk.call.callactionnew

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun VirtualBackgroundAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: Boolean = false
) {
    val text = stringResource(id = R.string.kaleyra_call_sheet_virtual_background)
    CallAction(
        modifier = modifier,
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_virtual_background),
        enabled = enabled,
        contentDescription = text,
        buttonText = text,
        label = if (label) text else null,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun VirtualBackgroundActionPreview() {
    KaleyraM3Theme {
        Surface {
            VirtualBackgroundAction({})
        }
    }
}