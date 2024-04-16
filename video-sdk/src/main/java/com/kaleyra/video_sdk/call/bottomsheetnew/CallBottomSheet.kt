package com.kaleyra.video_sdk.call.bottomsheetnew

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal object CallScreenScaffoldDefault {

    val contentPadding = PaddingValues(16.dp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CallScreenScaffold(
    modifier: Modifier = Modifier,
    topAppBar: @Composable () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
    sheetDragContent: @Composable ColumnScope.() -> Unit,
    sheetState: CallSheetState = rememberCallSheetState(),
    sheetScrimColor: Color = CallBottomSheetDefaults.ScrimColor,
    sheetDragHandle: @Composable (() -> Unit)? = { CallBottomSheetDefaults.DragHandle() },
    cornerShape: RoundedCornerShape = CallBottomSheetDefaults.Shape,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentPadding: PaddingValues = CallScreenScaffoldDefault.contentPadding,
    content: @Composable (PaddingValues) -> Unit
) {
    val dragOrientation = Orientation.Vertical

    val scope = rememberCoroutineScope()
    val animateToDismiss: () -> Unit = remember {
        { scope.launch { sheetState.collapse() } }
    }
    val settleToDismiss: (velocity: Float) -> Unit = remember {
        { scope.launch { sheetState.settle(it) } }
    }

    val density = LocalDensity.current
    var sheetDragContentHeight by remember { mutableStateOf(0.dp) }
    var bottomSheetPadding by remember { mutableStateOf(0.dp) }
    var topAppBarPadding by remember { mutableStateOf(0.dp) }
    val paddingValues by remember {
        derivedStateOf { PaddingValues(top = topAppBarPadding, bottom = bottomSheetPadding) }
    }

    Surface(
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(Modifier.fillMaxSize()) {
            content(paddingValues)
            Box(
                modifier = Modifier
                    .onSizeChanged {
                        topAppBarPadding = with(density) { it.height.toDp() }
                    },
                content = { topAppBar() }
            )
            Scrim(
                color = sheetScrimColor,
                onDismissRequest = animateToDismiss,
                visible = sheetState.targetValue == CallSheetValue.Expanded
            )
            CallBottomSheetLayout(
                modifier = modifier
                    .onSizeChanged {
                        val height = with(density) { it.height.toDp() }
                        bottomSheetPadding = height - sheetDragContentHeight
                    }
                    .padding(contentPadding)
                    .align(Alignment.BottomCenter)
                    .clip(cornerShape),
                sheetContent = {
                    Surface {
                        Column(content = sheetContent)
                    }
                },
                sheetDragContent = sheetDragHandle?.let { dragHandle ->
                    {
                        Surface(
                            modifier = Modifier
                                .offset {
                                    val offset = if (!sheetState.offset.isNaN()) sheetState
                                        .requireOffset()
                                        .roundToInt() else 0
                                    IntOffset(x = 0, y = offset)
                                }
                                .nestedScroll(
                                    CallBottomSheetNestedScrollConnection(
                                        sheetState = sheetState,
                                        orientation = dragOrientation,
                                        onFling = settleToDismiss
                                    )
                                )
                                .anchoredDraggable(
                                    state = sheetState.anchoredDraggableState,
                                    orientation = dragOrientation
                                ),
                            shape = cornerShape.copy(
                                bottomStart = CornerSize(0.dp),
                                bottomEnd = CornerSize(0.dp)
                            ),
                            content = {
                                Column {
                                    Box(
                                        Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .dragHandleSemantics(
                                                sheetState = sheetState,
                                                coroutineScope = scope,
                                                onDismiss = animateToDismiss
                                            )
                                    ) {
                                        dragHandle()
                                    }
                                    Column(
                                        modifier = Modifier.onSizeChanged {
                                            val newAnchors = DraggableAnchors {
                                                CallSheetValue.Expanded at 0f
                                                CallSheetValue.Collapsed at it.height.toFloat()
                                            }
                                            sheetDragContentHeight =
                                                with(density) { it.height.toDp() }
                                            sheetState.anchoredDraggableState.updateAnchors(
                                                newAnchors
                                            )
                                        },
                                        content = sheetDragContent
                                    )
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
internal fun CallBottomSheetLayout(
    modifier: Modifier = Modifier,
    sheetDragContent: @Composable (() -> Unit)?,
    sheetContent: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = {
            sheetContent()
            sheetDragContent?.invoke()
        }
    ) { measurables, constraints ->
        val bodyMeasurable = measurables[0]
        val sheetMeasurable = if (sheetDragContent != null) measurables[1] else null
        val body = bodyMeasurable.measure(constraints)
        val bottomSheet = sheetMeasurable?.measure(constraints.copy(minWidth = body.width, maxWidth = body.width))

        val sheetHeight = bottomSheet?.height ?: 0
        val width = body.width
        val height = body.height + sheetHeight
        layout(width, height) {
            bottomSheet?.placeRelative(0, 0)
            body.placeRelative(0, sheetHeight)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalCallBottomSheet(
    sheetContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: CallSheetState = rememberCallSheetState(),
    sheetScrimColor: Color = CallBottomSheetDefaults.ScrimColor,
    sheetDragHandle: @Composable (() -> Unit)? = { CallBottomSheetDefaults.VerticalDragHandle() },
    cornerShape: RoundedCornerShape = CallBottomSheetDefaults.Shape,
    content: @Composable RowScope.() -> Unit
) {
    val dragOrientation = Orientation.Horizontal
    val scope = rememberCoroutineScope()
    val animateToDismiss: () -> Unit = remember(scope) {
        { scope.launch { sheetState.collapse() } }
    }
    val settleToDismiss: (velocity: Float) -> Unit = remember(scope) {
        { scope.launch { sheetState.settle(it) } }
    }

    Box(Modifier.fillMaxSize()) {
        Scrim(
            color = sheetScrimColor,
            onDismissRequest = animateToDismiss,
            visible = sheetState.targetValue == CallSheetValue.Expanded
        )
        Row(modifier.clip(cornerShape)) {
            Surface(
                modifier = Modifier
                    .offset {
                        val offset = if (!sheetState.offset.isNaN()) sheetState
                            .requireOffset()
                            .roundToInt() else 0
                        IntOffset(x = offset, y = 0)
                    }
                    .nestedScroll(
                        CallBottomSheetNestedScrollConnection(
                            sheetState = sheetState,
                            orientation = dragOrientation,
                            onFling = settleToDismiss
                        )
                    )
                    .anchoredDraggable(
                        state = sheetState.anchoredDraggableState,
                        orientation = dragOrientation,
                        enabled = sheetDragHandle != null
                    ),
                shape = cornerShape.copy(topEnd = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
                content = {
                    CustomRow {
                        Box(
                            Modifier
                                .align(Alignment.CenterVertically)
                                .dragHandleSemantics(
                                    sheetState = sheetState,
                                    coroutineScope = scope,
                                    onDismiss = animateToDismiss
                                )
                        ) {
                            sheetDragHandle?.invoke()
                        }
                        Row(
                            modifier = Modifier.onSizeChanged {
                                val newAnchors = DraggableAnchors {
                                    CallSheetValue.Expanded at 0f
                                    CallSheetValue.Collapsed at it.width.toFloat()
                                }
                                sheetState.anchoredDraggableState.updateAnchors(newAnchors)
                            },
                            content = sheetContent
                        )
                    }
                }
            )
            Surface {
                Row(content = content)
            }
        }
    }
}

@Composable
fun CustomRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        val body = measurables[1].measure(constraints)
        val bottomSheet = measurables[0].measure(constraints.copy(maxHeight = body.height))

        val width = bottomSheet.width + body.width
        val height = body.height
        layout(width, height) {
            bottomSheet.placeRelative(0, 0)
            body.placeRelative(bottomSheet.width, 0)
        }
    }
}

@Composable
private fun Scrim(
    color: Color,
    onDismissRequest: () -> Unit,
    visible: Boolean
) {
    if (color.isSpecified) {
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = TweenSpec(),
            label = "alpha"
        )
        val dismissSheet = if (visible) {
            Modifier
                .pointerInput(onDismissRequest) {
                    detectTapGestures {
                        onDismissRequest()
                    }
                }
                .clearAndSetSemantics {}
        } else {
            Modifier
        }
        Canvas(
            Modifier
                .fillMaxSize()
                .then(dismissSheet)
        ) {
            drawRect(color = color, alpha = alpha)
        }
    }
}

private fun Modifier.dragHandleSemantics(
    sheetState: CallSheetState,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit
): Modifier =
    semantics(mergeDescendants = true) {
        with(sheetState) {
            dismiss {
                onDismiss()
                true
            }
            if (currentValue == CallSheetValue.Collapsed) {
                expand {
                    coroutineScope.launch { expand() }
                    true
                }
            } else {
                collapse {
                    coroutineScope.launch { collapse() }
                    true
                }
            }
        }
    }
