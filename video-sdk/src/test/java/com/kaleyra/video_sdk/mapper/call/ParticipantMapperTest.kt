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

package com.kaleyra.video_sdk.mapper.call

import android.net.Uri
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isMeParticipantInitialized
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toMyParticipantState
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayImages
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayNames
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ParticipantMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>(relaxed = true)

    private val callParticipantsMock = mockk<CallParticipants>(relaxed = true)

    private val participantMock1 = mockk<CallParticipant>(relaxed = true)

    private val participantMock2 = mockk<CallParticipant>(relaxed = true)

    private val participantMeMock = mockk<CallParticipant.Me>(relaxed = true)

    private val uriMock1 = mockk<Uri>(relaxed = true)

    private val uriMock2 = mockk<Uri>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        with(participantMock1) {
            every { userId } returns "userId1"
            every { combinedDisplayName } returns MutableStateFlow("displayName1")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock1)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { combinedDisplayName } returns MutableStateFlow("displayName2")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock2)
        }
    }

    @Test
    fun meParticipantNull_isMeParticipantInitialized_false() = runTest {
        every { callParticipantsMock.me } returns null
        val call = flowOf(callMock)
        val result = call.isMeParticipantInitialized()
        Assert.assertEquals(false, result.first())
    }

    @Test
    fun meParticipantNotNull_isMeParticipantInitialized_true() = runTest {
        every { callParticipantsMock.me } returns mockk()
        val call = flowOf(callMock)
        val result = call.isMeParticipantInitialized()
        Assert.assertEquals(true, result.first())
    }

    @Test
    fun emptyOtherParticipants_toOtherDisplayNames_emptyList() = runTest {
        every { callParticipantsMock.others } returns listOf()
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf<String>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun filledOtherParticipants_toOtherDisplayNames_displayNamesList() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1", "displayName2")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun addOtherParticipant_toOtherDisplayNames_displayNameAdded() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants
        every { callParticipantsMock.others } returns listOf(participantMock1)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1")
        Assert.assertEquals(expected, actual)

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { others } returns listOf(participantMock1, participantMock2)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf("displayName1", "displayName2")
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun removeOtherParticipant_toOtherDisplayNames_displayNameRemoved() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1", "displayName2")
        Assert.assertEquals(expected, actual)

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { others } returns listOf(participantMock1)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf("displayName1")
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun displayNameUpdate_toOtherDisplayNames_displayNameUpdated() = runTest {
        val displayNameFlow = MutableStateFlow("displayName2")
        val participantMock3 = mockk<CallParticipant> {
            every { userId } returns "userId3"
            every { combinedDisplayName } returns displayNameFlow
        }
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock3)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1", "displayName2")
        Assert.assertEquals(expected, actual)

        displayNameFlow.value = "displayNameModified"
        val newActual = result.first()
        val newExpected = listOf("displayName1", "displayNameModified")
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun singleOtherParticipants_isGroupCall_false() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1)
        val call = MutableStateFlow(callMock)
        val result = call.isGroupCall(MutableStateFlow("companyId"))
        Assert.assertEquals(false, result.first())
    }

    @Test
    fun multipleOtherParticipants_isGroupCall_true() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.isGroupCall(MutableStateFlow("companyId"))
        Assert.assertEquals(true, result.first())
    }

    @Test
    fun `when a participant userId is the companyId, they are not counted as group member`() = runTest {
        every { participantMock1.userId } returns "companyId"
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.isGroupCall(MutableStateFlow("companyId"))
        Assert.assertEquals(false, result.first())
    }

    @Test
    fun emptyOtherParticipants_toOtherDisplayImages_emptyList() = runTest {
        every { callParticipantsMock.others } returns listOf()
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayImages()
        val actual = result.first()
        val expected = listOf<Uri>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun filledOtherParticipants_toOtherDisplayImages_displayImagesList() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayImages()
        val actual = result.first()
        val expected = listOf(uriMock1, uriMock2)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun addOtherParticipant_toOtherDisplayImages_displayImageAdded() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants
        every { callParticipantsMock.others } returns listOf(participantMock1)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayImages()
        val actual = result.first()
        val expected = listOf(uriMock1)
        Assert.assertEquals(expected, actual)

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { others } returns listOf(participantMock1, participantMock2)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf(uriMock1, uriMock2)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun removeOtherParticipant_toOtherDisplayImages_displayImageRemoved() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayImages()
        val actual = result.first()
        val expected = listOf(uriMock1, uriMock2)
        Assert.assertEquals(expected, actual)

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { others } returns listOf(participantMock1)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf(uriMock1)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun displayNameUpdate_toOtherDisplayImages_displayNameUpdated() = runTest {
        val uriMock3 = mockk<Uri>()
        val displayImageFlow = MutableStateFlow(uriMock2)
        val participantMock3 = mockk<CallParticipant> {
            every { userId } returns "userId3"
            every { combinedDisplayImage } returns displayImageFlow
        }
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock3)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayImages()
        val actual = result.first()
        val expected = listOf(uriMock1, uriMock2)
        Assert.assertEquals(expected, actual)

        displayImageFlow.value = uriMock3
        val newActual = result.first()
        val newExpected = listOf(uriMock1, uriMock3)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun testToMyState() = runTest {
        every { callParticipantsMock.me } returns participantMeMock
        every { participantMeMock.state } returns MutableStateFlow(CallParticipant.State.InCall)
        val actual = flowOf(callMock).toMyParticipantState().first()
        assertEquals(CallParticipant.State.InCall, actual)
    }

}