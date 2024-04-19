package com.kaleyra.video_sdk.call.callactionnew

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

object CallActionDefaults {

    val buttonShape = RoundedCornerShape(18.dp)

    val minButtonSize = 48.dp

    val buttonContentPadding = PaddingValues(12.dp)

    val badgeShape = CircleShape

    val badgeSize = 20.dp

    val badgeOffset = 4.dp

    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface.copy(.1f)

    val contentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface

    val checkedContainerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isSystemInDarkTheme()) 1f else .66f)

    val checkedContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surface

    val badgeContainerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primary

    @Composable
    fun iconButtonColors(
        containerColor: Color = CallActionDefaults.containerColor,
        contentColor : Color = CallActionDefaults.contentColor
    ): IconButtonColors =
        IconButtonDefaults.filledIconButtonColors(containerColor, contentColor)

    @Composable
    fun iconToggleButtonColors(
        containerColor: Color =  CallActionDefaults.containerColor,
        contentColor: Color =  CallActionDefaults.contentColor,
        checkedContainerColor: Color = CallActionDefaults.checkedContainerColor,
        checkedContentColor: Color = CallActionDefaults.checkedContentColor,
    ): IconToggleButtonColors = IconButtonDefaults.filledIconToggleButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        checkedContainerColor = checkedContainerColor,
        checkedContentColor = checkedContentColor
    )

    @Composable
    fun badgeColors(containerColor: Color = badgeContainerColor) =
        CardDefaults.cardColors(containerColor)

}

@Composable
fun CallToggleAction(
    icon: Painter,
    checked: Boolean,
    contentDescription: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String? = null,
    buttonContentPadding: PaddingValues = CallActionDefaults.buttonContentPadding,
    label: String? = null,
    badge: (@Composable BoxScope.() -> Unit)? = null
) {
    var isButtonTextDisplayed by remember { mutableStateOf(false) }
    CallActionLayout(
        modifier = modifier,
        label = label.takeIf { !isButtonTextDisplayed },
        badge = badge,
        iconButton = {
            FilledIconToggleButton(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = CallActionDefaults.minButtonSize,
                        minHeight = CallActionDefaults.minButtonSize
                    )
                    .fillMaxWidth(),
                shape = CallActionDefaults.buttonShape,
                colors = CallActionDefaults.iconToggleButtonColors()
            ) {
                ButtonLayout(
                    icon = icon,
                    text = buttonText,
                    contentDescription = contentDescription,
                    contentPadding = buttonContentPadding,
                    onButtonTextDisplay = { isButtonTextDisplayed = it }
                )
            }
        }
    )
}

@Composable
fun CallAction(
    icon: Painter,
    contentDescription: String,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    buttonText: String? = null,
    buttonColor: Color = MaterialTheme.colorScheme.onSurface.copy(.1f),
    buttonContentColor: Color = MaterialTheme.colorScheme.onSurface,
    buttonContentPadding: PaddingValues = CallActionDefaults.buttonContentPadding,
    label: String? = null,
    badge: (@Composable BoxScope.() -> Unit)? = null
) {
    var isButtonTextDisplayed by remember { mutableStateOf(false) }
    CallActionLayout(
        modifier = modifier,
        label = label.takeIf { !isButtonTextDisplayed },
        badge = badge,
        iconButton = {
            FilledIconButton(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = CallActionDefaults.minButtonSize,
                        minHeight = CallActionDefaults.minButtonSize
                    )
                    .fillMaxWidth(),
                shape = CallActionDefaults.buttonShape,
                colors = CallActionDefaults.iconButtonColors(containerColor = buttonColor, contentColor = buttonContentColor),
                onClick = onClick
            ) {
                ButtonLayout(
                    icon = icon,
                    text = buttonText,
                    contentDescription = contentDescription,
                    contentPadding = buttonContentPadding,
                    onButtonTextDisplay = { isButtonTextDisplayed = it }
                )
            }
        }
    )
}

@Composable
private fun CallActionLayout(
    modifier: Modifier,
    iconButton: @Composable () -> Unit,
    badge: (@Composable BoxScope.() -> Unit)?,
    label: String? = null
) {
    Box(modifier.width(IntrinsicSize.Max)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            iconButton()
            if (label != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    modifier = Modifier
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints.copy(maxWidth = Constraints.Infinity))
                            layout(constraints.minWidth, placeable.height) {
                                placeable.placeRelative(-placeable.width / 2, 0)
                            }
                        },
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        if (badge != null) {
            Badge(
                modifier = modifier
                    .align(Alignment.TopEnd)
                    .offset {
                        with(density) {
                            val offset = CallActionDefaults.badgeOffset
                                .toPx()
                                .roundToInt()
                            IntOffset(offset, -offset)
                        }
                    },
                content = badge
            )
        }
    }
}

@Composable
private fun ButtonLayout(
    icon: Painter,
    text: String?,
    contentDescription: String,
    contentPadding: PaddingValues,
    onButtonTextDisplay: (isDisplayed: Boolean) -> Unit
) {
    var shouldDisplayButtonText by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.padding(contentPadding),
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription
        )
        if (text != null && shouldDisplayButtonText) {
            Spacer(Modifier.width(12.dp))
            Text(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(maxWidth = Constraints.Infinity))
                        shouldDisplayButtonText = placeable.width <= constraints.maxWidth
                        onButtonTextDisplay(shouldDisplayButtonText)
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(0, 0)
                        }
                    },
                text = text
            )
        }
    }
}

@Composable
private fun Badge(
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)
) {
    Card(
        modifier = modifier,
        colors = CallActionDefaults.badgeColors(),
        shape = CallActionDefaults.badgeShape
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(CallActionDefaults.badgeSize)
                .align(Alignment.CenterHorizontally),
            content = content
        )
    }
}
