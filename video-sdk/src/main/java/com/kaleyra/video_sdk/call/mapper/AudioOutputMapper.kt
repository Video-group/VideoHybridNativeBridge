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

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.connectionservice.CallAudioOutput
import com.kaleyra.video_common_ui.connectionservice.CallAudioOutputDelegate
import com.kaleyra.video_common_ui.connectionservice.CallAudioState
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.audioOutputDevicesList
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.model.BluetoothDeviceState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object AudioOutputMapper {

    fun Flow<CallAudioState>.toAudioOutputUiState(): Flow<AudioOutputUiState> {
        return this.map { callAudioState ->
            AudioOutputUiState(
                playingDeviceId = callAudioState.currentOutput?.mapToAudioDeviceUi()?.id,
                audioDeviceList = ImmutableList(callAudioState.availableOutputs.map { it.mapToAudioDeviceUi() })
            )
        }
    }

    fun Flow<CallAudioState>.mapToCurrentAudioDeviceUi(): Flow<AudioDeviceUi?> {
        return this.map { it.currentOutput?.mapToAudioDeviceUi() }
    }

    fun CallAudioOutput.mapToAudioDeviceUi(): AudioDeviceUi {
        return when (this) {
            CallAudioOutput.Speaker -> AudioDeviceUi.LoudSpeaker
            CallAudioOutput.Earpiece -> AudioDeviceUi.EarPiece
            CallAudioOutput.WiredHeadset -> AudioDeviceUi.WiredHeadset
            is CallAudioOutput.Bluetooth -> AudioDeviceUi.Bluetooth(
                id = id,
                name = name,
                connectionState = null,
                batteryLevel = null
            )

            CallAudioOutput.Muted -> AudioDeviceUi.Muted
        }
    }

    fun AudioDeviceUi.mapToCallAudioOutput(callAudioOutputDelegate: CallAudioOutputDelegate): CallAudioOutput? {
        return when (this) {
            is AudioDeviceUi.Bluetooth -> {
                val devices = callAudioOutputDelegate.callOutputState.value.availableOutputs
                devices.firstOrNull { it is CallAudioOutput.Bluetooth && it.id == this.id }
            }
            AudioDeviceUi.EarPiece -> CallAudioOutput.Earpiece
            AudioDeviceUi.LoudSpeaker -> CallAudioOutput.Speaker
            AudioDeviceUi.Muted -> CallAudioOutput.Muted
            AudioDeviceUi.WiredHeadset -> CallAudioOutput.WiredHeadset
        }
    }

    fun AudioDeviceUi.mapToAudioOutputDevice(call: Call): AudioOutputDevice? {
        return when (this) {
            is AudioDeviceUi.Bluetooth -> {
                val devices = call.audioOutputDevicesList.replayCache.firstOrNull() ?: return null
                devices.firstOrNull { it is AudioOutputDevice.Bluetooth && it.identifier == this.id }
            }
            AudioDeviceUi.EarPiece -> AudioOutputDevice.Earpiece()
            AudioDeviceUi.LoudSpeaker -> AudioOutputDevice.Loudspeaker()
            AudioDeviceUi.Muted -> AudioOutputDevice.None()
            AudioDeviceUi.WiredHeadset -> AudioOutputDevice.WiredHeadset()
        }
    }

    fun Flow<CallUI>.toCurrentAudioDeviceUi(): Flow<AudioDeviceUi?> {
        return this.flatMapLatest { it.currentAudioOutputDevice }
                .map { it?.mapToAudioDeviceUi() }
                .distinctUntilChanged()
    }

    fun Flow<CallUI>.toAvailableAudioDevicesUi(): Flow<List<AudioDeviceUi>> =
        this.flatMapLatest { it.audioOutputDevicesList }
            .map { list -> list.map { it.mapToAudioDeviceUi() } }
            .distinctUntilChanged()

    fun AudioOutputDevice.mapToAudioDeviceUi(): AudioDeviceUi =
        when (this) {
            is AudioOutputDevice.None -> AudioDeviceUi.Muted
            is AudioOutputDevice.Earpiece -> AudioDeviceUi.EarPiece
            is AudioOutputDevice.Loudspeaker -> AudioDeviceUi.LoudSpeaker
            is AudioOutputDevice.WiredHeadset -> AudioDeviceUi.WiredHeadset
            is AudioOutputDevice.Bluetooth -> AudioDeviceUi.Bluetooth(
                id = identifier,
                name = name,
                connectionState = bluetoothConnectionStatus.mapToBluetoothDeviceState(),
                batteryLevel = batteryLevel
            )
        }

    fun AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.mapToBluetoothDeviceState(): BluetoothDeviceState =
        when (this) {
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.ACTIVE -> BluetoothDeviceState.Active
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.DISCONNECTED -> BluetoothDeviceState.Disconnected
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.AVAILABLE -> BluetoothDeviceState.Available
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.DEACTIVATING -> BluetoothDeviceState.Deactivating
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTING -> BluetoothDeviceState.Connecting
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTED -> BluetoothDeviceState.Connected
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.ACTIVATING -> BluetoothDeviceState.Activating
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTING_AUDIO -> BluetoothDeviceState.ConnectingAudio
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.PLAYING_AUDIO -> BluetoothDeviceState.PlayingAudio
            else -> BluetoothDeviceState.Failed

        }
}