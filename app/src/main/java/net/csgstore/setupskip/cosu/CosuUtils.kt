package net.csgstore.setupskip.cosu

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.app.DownloadManager
import android.net.Uri
import android.os.Handler
import android.util.Log

/**
 * Utility class for various operations necessary during COSU set up.
 */
/* package */
internal object CosuUtils {
    const val TAG = "CosuSetup"
    const val DEBUG = false
    const val MSG_DOWNLOAD_COMPLETE = 1
    const val MSG_DOWNLOAD_TIMEOUT = 2
    const val MSG_INSTALL_COMPLETE = 3
    private const val DOWNLOAD_TIMEOUT_MILLIS = 120000
    @JvmStatic
    fun startDownload(dm: DownloadManager, handler: Handler, location: String?): Long {
        val request = DownloadManager.Request(Uri.parse(location))
        val id = dm.enqueue(request)
        handler.sendMessageDelayed(
            handler.obtainMessage(MSG_DOWNLOAD_TIMEOUT, id),
            DOWNLOAD_TIMEOUT_MILLIS.toLong()
        )
        if (DEBUG)
            Log.d(TAG, "Starting download: DownloadId=$id")
        return id
    }
}