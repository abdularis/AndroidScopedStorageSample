package com.abdularis.scopestorage.utils

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApi {
    @Multipart
    @POST("upload-avatar")
    fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("username") username: RequestBody,
        @Part("id") id: RequestBody
    ): Observable<Response<ResponseBody>>
}