package com.kaleyra.video_common_ui.connectionservice

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.net.Uri
import android.os.Build
import android.telecom.Connection
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.callservice.CallForegroundServiceWorker
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.utils.CallExtensions
import com.kaleyra.video_common_ui.utils.CallExtensions.shouldShowAsActivity
import com.kaleyra.video_common_ui.utils.CallExtensions.showOnAppResumed
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PhoneConnectionServiceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private var service: PhoneConnectionService? = null

    private var notificationBuilder: Notification.Builder? = null

    private var notificationManager = ApplicationProvider.getApplicationContext<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val callMock = mockk<CallUI>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val otherParticipantMock = mockk<CallParticipant>(relaxed = true)

    private val connection = mockk<CallConnection>(relaxed = true)

    @Before
    fun setup() {
        service = spyk(Robolectric.setupService(PhoneConnectionService::class.java))
        service!!.setPrivateField("coroutineScope", MainScope())
        notificationBuilder = Notification.Builder(service)
            .setSmallIcon(1)
            .setContentTitle("Test")
            .setContentText("content text")
        mockkConstructor(CallForegroundServiceWorker::class)
        mockkObject(ContactsController)
        mockkObject(CallExtensions)
        mockkObject(ContactDetailsManager)
        mockkObject(CallConnection)
        every { anyConstructed<CallForegroundServiceWorker>().bind(any(), captureLambda()) } answers {
            lambda<(CallUI) -> Unit>().invoke(callMock)
        }
        every { anyConstructed<CallForegroundServiceWorker>().dispose() } returns Unit
        every { ContactsController.createOrUpdateConnectionServiceContact(any(), any(), any()) } returns Unit
        every { ContactsController.deleteConnectionServiceContact(any(), any()) } returns Unit
        with(callMock) {
            every { shouldShowAsActivity() } returns false
            every { showOnAppResumed(any()) } returns Unit
            every { participants } returns MutableStateFlow(participantsMock)
        }
        every { CallConnection.create(any()) } returns connection
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testOnStartCommand() {
        val startType = service!!.onStartCommand(null, 0, 0)
        assertEquals(Service.START_STICKY, startType)
    }

    @Test
    fun testOnDestroy() {
        val uri = Uri.parse("")
        every { connection.address } returns uri
        service!!.onCreateIncomingConnection(mockk(), mockk())
        service!!.onDestroy()
        verify(exactly = 1) { anyConstructed<CallForegroundServiceWorker>().dispose() }
        verify(exactly = 1) { ContactsController.deleteConnectionServiceContact(service!!, uri) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun testOnNewNotification() {
        val notification = notificationBuilder!!.build()
        val callMock = mockk<Call>(relaxed = true)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())

        service!!.onNewNotification(callMock, notification, 10)
        assertEquals(notification, Shadows.shadowOf(service).lastForegroundNotification)
        assertEquals(10, Shadows.shadowOf(service).lastForegroundNotificationId)
        assertEquals(
            notification,
            Shadows.shadowOf(notificationManager).getNotification(10)
        )
        assertNotEquals(0, notification.flags and Notification.FLAG_FOREGROUND_SERVICE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun testOnNewNotificationWithForegroundServiceType() {
        val notification = notificationBuilder!!.build()
        val callMock = mockk<Call>(relaxed = true)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())

        service!!.onNewNotification(callMock, notification, 10)
        assertEquals(notification, Shadows.shadowOf(service).lastForegroundNotification)
        assertEquals(10, Shadows.shadowOf(service).lastForegroundNotificationId)
        assertEquals(
            notification,
            Shadows.shadowOf(notificationManager).getNotification(10)
        )
        assertNotEquals(0, notification.flags and Notification.FLAG_FOREGROUND_SERVICE)
        assertEquals(
            service!!.getForegroundServiceType(false),
            service!!.foregroundServiceType
        )
    }

    @Test
    fun testOnClearNotification() {
        service!!.startForeground(10, notificationBuilder!!.build())
        service!!.onClearNotification(10)
        assertEquals(true, Shadows.shadowOf(service).isForegroundStopped)
        assertEquals(true, Shadows.shadowOf(service).notificationShouldRemoved)
    }

    @Test
    fun testOnDestroyRemovesNotification() {
        val n: Notification = notificationBuilder!!.build()
        service!!.startForeground(10, n)
        service!!.onDestroy()
        assertEquals(null, Shadows.shadowOf(notificationManager).getNotification(10))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun testOnConnectionServiceFocusLost() {
        service!!.onConnectionServiceFocusLost()
        verify(exactly = 1) { service!!.connectionServiceFocusReleased() }
    }

    @Test
    fun testOnCreateIncomingConnectionFailed() {
        service!!.startForeground(10, notificationBuilder!!.build())
        service!!.onCreateIncomingConnectionFailed(null, null)
        assertEquals(true, Shadows.shadowOf(service).isForegroundStopped)
        assertEquals(true, Shadows.shadowOf(service).notificationShouldRemoved)
    }

    @Test
    fun testOnCreateOutgoingConnectionFailed() {
        service!!.startForeground(10, notificationBuilder!!.build())
        service!!.onCreateOutgoingConnectionFailed(null, null)
        assertEquals(true, Shadows.shadowOf(service).isForegroundStopped)
        assertEquals(true, Shadows.shadowOf(service).notificationShouldRemoved)
    }

    @Test
    fun testOnCreateOutgoingConnection() {
        val createdConnection = service!!.onCreateOutgoingConnection(mockk(), mockk())
        assertEquals(connection, createdConnection)
        verify { connection.setDialing() }
        verify { connection.addConnectionStateListener(service!!) }
        verify(exactly = 1) { anyConstructed<CallForegroundServiceWorker>().bind(service!!, any()) }
    }

    @Test
    fun testOnCreateIncomingConnection() {
        val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk())
        assertEquals(connection, createdConnection)
        verify { connection.addIncomingCallListener(service!!) }
        verify { connection.addConnectionStateListener(service!!) }
    }

    @Test
    fun testIncomingConnectionAnswer() {
        val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk())
        PhoneConnectionService.answer()
        verify(exactly = 1) { createdConnection.onAnswer() }
    }

    @Test
    fun testIncomingConnectionReject() {
        val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk())
        PhoneConnectionService.reject()
        verify(exactly = 1) { createdConnection.onReject() }
    }

    @Test
    fun testIncomingConnectionEnd() {
        val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk())
        PhoneConnectionService.end()
        verify(exactly = 1) { createdConnection.onDisconnect() }
    }

    @Test
    fun testOutgoingConnectionEnd() {
        val createdConnection = service!!.onCreateOutgoingConnection(mockk(), mockk())
        PhoneConnectionService.end()
        verify(exactly = 1) { createdConnection.onDisconnect() }
    }

    @Test
    fun callActivityShouldBeShown_onCreateOutgoingConnection_activityShown() {
        every { callMock.shouldShowAsActivity() } returns true
        service!!.onCreateOutgoingConnection(mockk(), mockk())
        verify(exactly = 1) { callMock.showOnAppResumed(any()) }
    }

    @Test
    fun callActivityShouldNotBeShown_onCreateOutgoingConnection_activityNotShown() {
        every { callMock.shouldShowAsActivity() } returns false
        service!!.onCreateOutgoingConnection(mockk(), mockk())
        verify(exactly = 0) { callMock.showOnAppResumed(any()) }
    }

    @Test
    fun connectionStateDisconnected_onConnectionStateChange_serviceIsStopped() {
        val connection = mockk<CallConnection>(relaxed = true)
        every { connection.state } returns Connection.STATE_DISCONNECTED
        service!!.onConnectionStateChange(connection)
        verify { connection.removeIncomingCallListener(service!!) }
        verify { connection.removeConnectionStateListener(service!!) }
        assertEquals(true, Shadows.shadowOf(service).isForegroundStopped)
        assertEquals(true, Shadows.shadowOf(service).isStoppedBySelf)
    }

    @Test
    fun genericConnectionState_onConnectionStateChange_serviceIsNotStopped() {
        val connection = mockk<CallConnection>(relaxed = true)
        every { connection.state } returns mockk(relaxed = true)
        service!!.onConnectionStateChange(connection)
        verify(exactly = 0) { connection.removeIncomingCallListener(service!!) }
        verify(exactly = 0) { connection.removeConnectionStateListener(service!!) }
        assertEquals(false, Shadows.shadowOf(service).isForegroundStopped)
        assertEquals(false, Shadows.shadowOf(service).isStoppedBySelf)
    }

    @Test
    fun testOnShowIncomingCallUi() {
        val connection = mockk<CallConnection>(relaxed = true)
        service!!.onShowIncomingCallUi(connection)
        verify { connection.setRinging() }
        verify(exactly = 1) { anyConstructed<CallForegroundServiceWorker>().bind(service!!, any()) }
        verify(exactly = 1) { ContactsController.createOrUpdateConnectionServiceContact(service!!, connection.address, any()) }
    }

    @Test
    fun callActivityShouldBeShown_onShowIncomingCallUi_activityShown() {
        every { callMock.shouldShowAsActivity() } returns true
        service!!.onShowIncomingCallUi(mockk(relaxed = true))
        verify(exactly = 1) { callMock.showOnAppResumed(any()) }
    }

    @Test
    fun callActivityShouldNotBeShown_onShowIncomingCallUi_activityNotShown() {
        every { callMock.shouldShowAsActivity() } returns false
        service!!.onShowIncomingCallUi(mockk(relaxed = true))
        verify(exactly = 0) { callMock.showOnAppResumed(any()) }
    }

    @Test
    fun groupCall_onShowIncomingCallUi_contactSetAsIncomingGroupCallText() {
        val connection = mockk<CallConnection>(relaxed = true)
        val otherParticipantMock2 = mockk<CallParticipant>(relaxed = true)
        every { participantsMock.others } returns listOf(otherParticipantMock, otherParticipantMock2)
        service!!.onShowIncomingCallUi(connection)
        val text = service!!.resources.getString(R.string.kaleyra_notification_incoming_group_call)
        verify(exactly = 1) {
            ContactsController.createOrUpdateConnectionServiceContact(service!!, connection.address, text)
        }
    }

    @Test
    fun oneToOne_onShowIncomingCallUi_contactSetAsOtherUsername() {
        val connection = mockk<CallConnection>(relaxed = true)
        every { participantsMock.others } returns listOf(otherParticipantMock)
        every { otherParticipantMock.combinedDisplayName } returns MutableStateFlow("otherDisplayName")
        service!!.onShowIncomingCallUi(connection)
        verify(exactly = 1) {
            ContactsController.createOrUpdateConnectionServiceContact(service!!, connection.address, "otherDisplayName")
        }
    }

    inline fun <reified T> T.setPrivateField(field: String, value: Any): T  = apply {
        T::class.java.declaredFields
            .find { it.name == field}
            ?.also { it.isAccessible = true }
            ?.set(this, value)
    }
}