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

package com.kaleyra.video_sdk.chat.screen.model

import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.conversation.model.mock.mockConversationState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.appbar.model.mockActions
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.flowOf

internal val mockChatUiState = ChatUiState.OneToOne(
    recipientDetails = ChatParticipantDetails("John Smith", ImmutableUri(), flowOf()),
    connectionState = ConnectionState.Connecting,
    actions = mockActions,
    conversationState = mockConversationState,
    isInCall = true
)