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

package com.kaleyra.video_sdk.call

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.notification.CallNotificationActionReceiver
import com.kaleyra.video_common_ui.notification.CallNotificationExtra
import com.kaleyra.video_common_ui.notification.CallNotificationExtra.IS_CALL_SERVICE_RUNNING_EXTRA
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationActionReceiver
import com.kaleyra.video_common_ui.proximity.ProximityCallActivity
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.moveToFront
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.turnScreenOff
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.getScreenAspectRatio
import com.kaleyra.video_sdk.call.screen.CallScreen
import com.kaleyra.video_sdk.extensions.RationalExtensions.coerceRationalForPip
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * PhoneCallActivity implementation of activity class used to display calls
 * @property onBackPressedCallback OnBackPressedCallback back press callback
 * @property disableProximity Boolean true to disable proximity listener to trigger camera and display on/off, false otherwise
 * @property isPipSupported Boolean true if picture-in-picture mode is supported, false otherwise
 */
internal class PhoneCallActivity : FragmentActivity(), ProximityCallActivity {

    private companion object {
        var pictureInPictureAspectRatio: Rational = Rational(9, 16)

        val isInPipMode: MutableStateFlow<Boolean> = MutableStateFlow(false)

        val shouldShowFileShare: MutableStateFlow<Boolean> = MutableStateFlow(false)

        var isActivityFinishing: Boolean = false

        var isInForeground: Boolean = false

        var isFileShareDisplayed: Boolean = false

        var isWhiteboardDisplayed: Boolean = false

        var isUsbCameraConnecting: Boolean = false
    }
    private var isAskingInputPermissions: Boolean = false

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = enterPipModeIfSupported()
    }

    override val disableProximity: Boolean
        get() = !isInForeground || isInPipMode.value || isWhiteboardDisplayed || isFileShareDisplayed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentAction(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            updatePipParams()?.let { setPictureInPictureParams(it) }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setContent {
            CallScreen(
                shouldShowFileShareComponent = shouldShowFileShare.collectAsStateWithLifecycle().value,
                isInPipMode = isInPipMode.collectAsStateWithLifecycle().value,
                enterPip = ::enterPipModeIfSupported,
                onFileShareVisibility = ::onFileShareVisibility,
                onWhiteboardVisibility = { isWhiteboardDisplayed = it },
                onDisplayMode = ::onDisplayMode,
                onPipAspectRatio = ::onAspectRatio,
                onUsbCameraConnected = ::onUsbConnecting,
                onActivityFinishing = { isActivityFinishing = true },
                onAskInputPermissions = { isAskingInputPermissions = it },
                onConnectionServicePermissionsResult = ::onConnectionServicePermissions
            )
        }
        turnScreenOn()
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        isInPipMode.value = isInPictureInPictureMode
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    override fun onDestroy() {
        super.onDestroy()
        turnScreenOff()
    }

    override fun disableWindowTouch() {
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun enableWindowTouch() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode.value = isInPictureInPictureMode
    }

    private val isPipSupported by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) packageManager.hasSystemFeature(
            PackageManager.FEATURE_PICTURE_IN_PICTURE
        )
        else false
    }

    private fun enterPipModeIfSupported() {
        if (!isPipSupported || isActivityFinishing) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            updatePipParams()?.let { params ->
                enterPictureInPictureMode(params)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePipParams() =
        PictureInPictureParams.Builder()
            .setAspectRatio(pictureInPictureAspectRatio)
            .build()

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isAskingInputPermissions || isUsbCameraConnecting) return
        enterPipModeIfSupported()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentAction(intent)
        restartActivityIfCurrentCallIsEnded(intent)
    }

    private fun handleIntentAction(intent: Intent): Boolean {
        return when (intent.extras?.getString(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA)) {
            CallNotificationActionReceiver.ACTION_ANSWER, CallNotificationActionReceiver.ACTION_HANGUP -> {
                val isCallServiceRunning = intent.extras?.getBoolean(IS_CALL_SERVICE_RUNNING_EXTRA, true) ?: true
                if (isCallServiceRunning) {
                    forwardIntentToReceiver(intent, CallNotificationActionReceiver::class.java)
                }
                true
            }

            FileShareNotificationActionReceiver.ACTION_DOWNLOAD -> {
                forwardIntentToReceiver(intent, FileShareNotificationActionReceiver::class.java)
                shouldShowFileShare.value = true
                true
            }

            else -> false
        }
    }

    private fun <T: BroadcastReceiver> forwardIntentToReceiver(intent: Intent, receiver: Class<T>) {
        sendBroadcast(Intent(this, receiver).apply {
            putExtras(intent)
        })
    }

    private fun onUsbConnecting(isUsbConnecting: Boolean) {
        isUsbCameraConnecting = isUsbConnecting
    }

    private fun onFileShareVisibility(isFileShareVisible: Boolean) {
        isFileShareDisplayed = isFileShareVisible
        if (isFileShareVisible) shouldShowFileShare.value = false
    }

    private fun onAspectRatio(aspectRatio: Rational) {
        pictureInPictureAspectRatio = if (aspectRatio == Rational.NaN) getScreenAspectRatio() else aspectRatio.coerceRationalForPip()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        updatePipParams()?.let { setPictureInPictureParams(it) }
    }

    private fun onDisplayMode(displayMode: CallUI.DisplayMode) {
            when (displayMode) {
                is CallUI.DisplayMode.PictureInPicture -> {
                    when {
                        isInPipMode.value -> return
                        isInForeground -> enterPipModeIfSupported()
                        else -> tryEnterPipFromBackground()
                    }
                }

                is CallUI.DisplayMode.Foreground -> {
                    if (isInForeground) return
                    moveToFront()
                }

                is CallUI.DisplayMode.Background -> moveTaskToBack(true)

                else -> Unit
            }
    }

    private fun tryEnterPipFromBackground() {
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) {
                if (activity != this@PhoneCallActivity) return
                enterPipModeIfSupported()
                application.unregisterActivityLifecycleCallbacks(this)
            }
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit

        })
        moveToFront()
    }

    private fun onConnectionServicePermissions() {
        val extra = intent.extras?.getString(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA)
        if (extra != CallNotificationActionReceiver.ACTION_ANSWER && extra != CallNotificationActionReceiver.ACTION_HANGUP) return
        forwardIntentToReceiver(intent, CallNotificationActionReceiver::class.java)
    }

    private fun restartActivityIfCurrentCallIsEnded(intent: Intent) {
        if (isActivityFinishing && Intent.FLAG_ACTIVITY_NEW_TASK.let { intent.flags.and(it) == it }) {
            finishAndRemoveTask()
            startActivity(intent)
        }
    }

}
