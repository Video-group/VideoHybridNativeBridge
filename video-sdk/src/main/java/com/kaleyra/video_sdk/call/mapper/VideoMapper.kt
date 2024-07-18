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

package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.InputMapper.toMyCameraStream
import com.kaleyra.video_sdk.call.mapper.VideoMapper.mapToVideoUi
import com.kaleyra.video_sdk.call.streamnew.model.core.ImmutableView
import com.kaleyra.video_sdk.call.pointer.model.PointerUi
import com.kaleyra.video_sdk.call.streamnew.model.core.VideoUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal object VideoMapper {
    fun StateFlow<Input.Video?>.mapToVideoUi(): Flow<VideoUi?> =
        flow {
            val initialValue = value?.let { video ->
                VideoUi(
                    video.id,
                    video.view.value?.let { ImmutableView(it) },
                    video.enabled.value,
                    video.isScreenShare(),
                    ImmutableList(emptyList())
                )
            }
            emit(initialValue)

            val flow = this@mapToVideoUi.filterIsInstance<Input.Video>()
            combine(
                flow.map { it.id },
                flow.flatMapLatest { it.view }.map { it?.let { ImmutableView(it) } },
                flow.flatMapLatest { it.enabled },
                flow.map { it.isScreenShare() },
                flow.mapToPointersUi()
            ) { id, view, enabled, isScreenShare, pointers ->
                val pointerList = ImmutableList(if (view != null && enabled) pointers else emptyList())
                VideoUi(id, view, enabled, isScreenShare, pointerList)
            }.collect {
                emit(it)
            }
        }.distinctUntilChanged()

    fun Flow<Input.Video>.mapToPointersUi(): Flow<List<PointerUi>> {
        val list = mutableMapOf<String, PointerUi>()
        return flow {
            emit(emptyList())
            combine(
                this@mapToPointersUi.flatMapLatest { it.events }.filterIsInstance<Input.Video.Event.Pointer>(),
                this@mapToPointersUi.shouldMirrorPointer()
            ) { event, mirror ->
                if (event.action is Input.Video.Event.Pointer.Action.Idle) list.remove(event.producer.userId)
                else list[event.producer.userId] = event.mapToPointerUi(mirror)
                emit(list.values.toList())
            }.collect()
        }.distinctUntilChanged()
    }

    fun Call.toMyCameraVideoUi(): Flow<VideoUi?> =
        this
            .toMyCameraStream()
            .flatMapLatest { it.video.mapToVideoUi() }

    fun Flow<Input.Video>.shouldMirrorPointer(): Flow<Boolean> =
        flatMapLatest { video ->
            (video as? Input.Video.Camera.Internal)?.currentLens?.map { !it.isRear } ?: flowOf(false)
        }

    suspend fun Input.Video.Event.Pointer.mapToPointerUi(mirror: Boolean = false): PointerUi {
        return PointerUi(
            username = producer.combinedDisplayName.firstOrNull() ?: "",
            x = if (mirror) 100 - position.x else position.x,
            y = position.y
        )
    }

    private fun Input.Video.isScreenShare() = this is Input.Video.Application || this is Input.Video.Screen

}