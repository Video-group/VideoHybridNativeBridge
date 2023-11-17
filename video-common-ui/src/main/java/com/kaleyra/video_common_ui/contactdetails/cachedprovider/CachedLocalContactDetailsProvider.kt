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

package com.kaleyra.video_common_ui.contactdetails.cachedprovider

import com.kaleyra.video_common_ui.contactdetails.provider.LocalContactDetailsProvider
import com.kaleyra.video_common_ui.model.UserDetailsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class CachedLocalContactDetailsProvider(val userDetailsProvider: UserDetailsProvider, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    CachedContactDetailsProvider(LocalContactDetailsProvider(userDetailsProvider, ioDispatcher))