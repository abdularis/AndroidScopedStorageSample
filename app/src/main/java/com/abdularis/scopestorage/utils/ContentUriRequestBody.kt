package com.abdularis.scopestorage.utils

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.FileInputStream

/**
 * contentUri scheme should be content:// or file://
 */
class ContentUriRequestBody(
    private val contentResolver: ContentResolver,
    private val contentUri: Uri
) : RequestBody() {

    override fun contentLength(): Long {
        return contentResolver.openFileDescriptor(contentUri, MODE_READ)?.use { it.statSize } ?: 0
    }

    override fun contentType(): MediaType? {
        return contentResolver.getType(contentUri)?.toMediaTypeOrNull()
    }

    override fun writeTo(sink: BufferedSink) {
        contentResolver.openInputStream(contentUri)?.source()?.use { sink.writeAll(it) }
    }

    companion object {
        private const val MODE_READ = "r"
    }
}
