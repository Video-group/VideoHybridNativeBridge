package com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.SheetItemsLayout
import com.kaleyra.video_sdk.call.bottomsheetnew.SheetActionsSpacing
import com.kaleyra.video_sdk.call.bottomsheetnew.VerticalSheetItemsLayout
import com.kaleyra.video_sdk.call.callactionnew.AnswerAction
import com.kaleyra.video_sdk.call.callactionnew.MoreAction
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.row.ReversibleRow
import kotlinx.coroutines.launch

@Composable
internal fun SheetActions(
    modifier: Modifier = Modifier,
    actions: ImmutableList<@Composable (label: Boolean, modifier: Modifier) -> Unit>,
    sheetState: CallSheetState,
    maxActions: Int = Int.MAX_VALUE,
    showAnswerAction: Boolean,
    onAnswerActionClick: () -> Unit,
    onActionsPlaced: (actionsPlaced: Int) -> Unit
) {
    var showMoreAction by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val onMoreClick: () -> Unit = remember {
        {
            scope.launch {
                if (sheetState.currentValue == CallSheetValue.Expanded) {
                    sheetState.collapse()
                } else {
                    sheetState.expand()
                }
            }
        }
    }

    ReversibleRow(modifier, reverseLayout = true) {
        when {
            showAnswerAction -> {
                AnswerAction(onClick = onAnswerActionClick)
                Spacer(Modifier.width(SheetActionsSpacing))
            }
            showMoreAction -> {
                MoreAction(onClick = onMoreClick)
                Spacer(Modifier.width(SheetActionsSpacing))
            }
        }

        SheetItemsLayout(
            onItemsPlaced = { itemsPlaced ->
                showMoreAction = actions.count() > itemsPlaced
                onActionsPlaced(itemsPlaced)
            },
            horizontalItemSpacing = SheetActionsSpacing,
            maxItems = maxActions - if (showAnswerAction || showMoreAction) 1 else 0,
            content = { actions.value.forEach { action -> action(false, Modifier) } }
        )
    }
}

// TODO update this composable like SheetActions above
@Composable
internal fun VerticalSheetActions(
    actions: ImmutableList<@Composable (label: Boolean, modifier: Modifier) -> Unit>,
    showMoreItem: Boolean,
    onMoreItemClick: (() -> Unit),
    onItemsPlaced: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (showMoreItem) {
            MoreAction(onClick = onMoreItemClick)
            Spacer(Modifier.height(SheetActionsSpacing))
        }
        VerticalSheetItemsLayout(
            onItemsPlaced = onItemsPlaced,
            verticalItemSpacing = SheetActionsSpacing,
            content = { actions.value.forEach { action -> action(false, Modifier) } }
        )
    }
}