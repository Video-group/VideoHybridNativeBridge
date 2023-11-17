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

package com.kaleyra.video_glasses_sdk.call.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.kaleyra.video.conference.Call
import com.kaleyra.video_glasses_sdk.utils.extensions.ContextExtensions.getAttributeResourceId
import com.kaleyra.video_glasses_sdk.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.video_glasses_sdk.utils.safeNavigate
import com.kaleyra.video_glasses_sdk.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile

/**
 * RingingFragment
 */
internal class RingingFragment : PreCallFragment() {

    override var themeResId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        themeResId = requireActivity().theme.getAttributeResourceId(R.attr.kaleyra_ringingStyle)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun bindUI() {
        super.bindUI()

        repeatOnStarted {
            viewModel.callState
                .takeWhile { it !is Call.State.Connecting }
                .onCompletion {
                    binding.kaleyraSubtitle.text = resources.getString(R.string.kaleyra_glass_connecting)
                    binding.kaleyraBottomNavigation.visibility = View.INVISIBLE
                }
                .launchIn(this)
        }
    }

    override fun onConnected() { findNavController().safeNavigate(RingingFragmentDirections.actionRingingFragmentToEmptyFragment()) }

    override fun setSubtitle(isGroupCall: Boolean, isLink: Boolean) {
        binding.kaleyraSubtitle.text = resources.getString(
            when {
                isLink -> R.string.kaleyra_glass_connecting
                isGroupCall -> R.string.kaleyra_glass_ringing_group
                else -> R.string.kaleyra_glass_ringing
            }
    ) }

    override fun onTap() = true.also { viewModel.onAnswer() }

    override fun onSwipeDown() = true.also { viewModel.onHangup() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also { if(it) binding.kaleyraParticipantsScrollView.smoothScrollByWithAutoScroll(resources.displayMetrics.densityDpi / 2, 0) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also { if(it) binding.kaleyraParticipantsScrollView.smoothScrollByWithAutoScroll(-resources.displayMetrics.densityDpi / 2, 0) }
}