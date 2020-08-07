package com.abdularis.scopestorage.utils

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request

object UploadUtil {
    fun uploadFile(contentResolver: ContentResolver, fileUri: Uri) {
        val name = StorageUtil.queryFileNameFromUri(contentResolver, fileUri)
        val content = ContentUriRequestBody(contentResolver, fileUri)
        val form = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("avatar", name, content)
            .build()

        val request = Request.Builder()
            .url("http://192.168.43.42:4000/upload-avatar")
            .post(form)
            .build()

        val httpClient = OkHttpClient()
        val response = httpClient.newCall(request).execute()

        Log.d("TestMe", "upload response: ${response.body?.string()}")
    }
}