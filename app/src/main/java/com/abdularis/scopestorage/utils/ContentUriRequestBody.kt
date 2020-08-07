package com.abdularis.scopestorage.utils

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
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

    private val parcelFileDescriptor: ParcelFileDescriptor? by lazy {
        contentResolver.openFileDescriptor(contentUri, MODE_READ)
    }

    override fun contentLength(): Long {
        return parcelFileDescriptor?.statSize ?: 0
    }

    override fun contentType(): MediaType? {
        val contentType = contentResolver.getType(contentUri)
        return contentType?.toMediaTypeOrNull()
    }

    override fun writeTo(sink: BufferedSink) {
        parcelFileDescriptor?.fileDescriptor?.let { fd ->
            FileInputStream(fd).source().use { fis ->
                sink.writeAll(fis)
            }
        }
    }

    companion object {
        private const val MODE_READ = "r"
    }
}
