package com.abdularis.scopestorage.utils

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.facebook.stetho.okhttp3.StethoInterceptor
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit

object UploadUtil {
    private fun createUploadApiService(): UploadApi {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(StethoInterceptor())
            .callTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .connectTimeout(10, TimeUnit.MINUTES)
            .build()
        return Retrofit.Builder()
            .baseUrl("http://192.168.8.101:4000/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient)
            .build()
            .create(UploadApi::class.java)
    }

    fun uploadFile(
        contentResolver: ContentResolver,
        contentUri: Uri
    ): Observable<retrofit2.Response<ResponseBody>> {

//        Log.d("TestMe", "thread: ${Thread.currentThread().name}")
//
//        var start = System.currentTimeMillis()
//        val filename = StorageUtil.queryFileNameFromUri(contentResolver, contentUri)
//        Log.d("TestMe", "end query filename, take: ${System.currentTimeMillis() - start}")
//
//        start = System.currentTimeMillis()
//
//        val fileRequestBody = ContentUriRequestBody(contentResolver, contentUri)
//        Log.d("TestMe", "end create file body, take: ${System.currentTimeMillis() - start}")
//
//        start = System.currentTimeMillis()
//        val service = createUploadApiService()
//        Log.d("TestMe", "end create upload api, take: ${System.currentTimeMillis() - start}")
//
//        start = System.currentTimeMillis()
//        val file = MultipartBody.Part.createFormData(
//            "document",
//            filename,
//            fileRequestBody
//        )
//        Log.d("TestMe", "end create multipart form, take: ${System.currentTimeMillis() - start}")
//
//
//        start = System.currentTimeMillis()
//        val username = "aris".toRequestBody("text/plain".toMediaTypeOrNull())
//        val id = "10092938".toRequestBody("text/plain".toMediaTypeOrNull())
//        Log.d("TestMe", "end create other parts, take: ${System.currentTimeMillis() - start}")
        return Observable.just(contentUri)
            .flatMap {
                val filename = StorageUtil.queryFileNameFromUri(contentResolver, contentUri)
                val fileRequestBody = ContentUriRequestBody(contentResolver, it)
                val service = createUploadApiService()
                val file = MultipartBody.Part.createFormData(
                    "document",
                    filename,
                    fileRequestBody
                )

                val username = "aris".toRequestBody("text/plain".toMediaTypeOrNull())
                val id = "10092938".toRequestBody("text/plain".toMediaTypeOrNull())
                service.uploadFile(file, username, id)
            }
    }
}