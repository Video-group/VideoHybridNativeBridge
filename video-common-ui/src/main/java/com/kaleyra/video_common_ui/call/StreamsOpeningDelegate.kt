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

package com.kaleyra.video_common_ui.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * StreamsOpeningDelegate delegate for the opening stream operation
 */
 class StreamsOpeningDelegate(private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {

    /**
     * Open Participant streams
     * @param call Call the call object
     */
    fun openParticipantsStreams(call: Call) {
        call.participants
            .map { it.list }
            .flatMapLatest { participantsList ->
                participantsList.map { it.streams }.merge()
            }
            .onEach { it.forEach { stream -> stream.open() } }
            .launchIn(coroutineScope)
    }
}