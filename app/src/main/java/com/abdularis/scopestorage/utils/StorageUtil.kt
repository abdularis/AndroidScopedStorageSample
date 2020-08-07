package com.abdularis.scopestorage.utils

import android.annotation.TargetApi
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.format.DateUtils
import android.util.Log
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream

object StorageUtil {
    fun savePictureToPrivateExternalStorage(
        context: Context,
        bitmap: Bitmap,
        filename: String
    ): Single<String> {
        return Single.fromCallable {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, DEFAULT_IMAGE_QUALITY, fos)
            }
            file.absolutePath
        }.subscribeOn(Schedulers.io())
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun createImageFile(
        filename: String,
        subFolder: String
    ): File {
        val baseDir =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}${File.separator}"
        val dir = File(if (subFolder.isNotEmpty()) "$baseDir$subFolder" else baseDir)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw IllegalStateException("Failed to create a directory")
            }
        }
        return File(dir, filename)
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun savePictureToSharedStorageSdk28Below(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        subFolder: String
    ): Single<Uri?> {
        return Single.create { singleSubscriber ->
            try {
                val imageFile = createImageFile(filename, subFolder)
                FileOutputStream(imageFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, DEFAULT_IMAGE_QUALITY, fos)
                }
                MediaScannerConnection.scanFile(
                    context, arrayOf(imageFile.absolutePath), arrayOf(MIME_TYPE_JPEG)
                ) { _, uri ->
                    singleSubscriber.onSuccess(uri)
                }
            } catch (e: Exception) {
                singleSubscriber.onError(e)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun savePictureToSharedStorageSdk29Above(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        description: String?,
        subFolder: String
    ): Single<Uri?> {
        return Single.fromCallable {
            val contentResolver = context.contentResolver
            val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val pendingValues = createImagePendingValues(filename, description, subFolder)
            val insertedItem = contentResolver.insert(imageCollection, pendingValues)

            insertedItem?.run {
                contentResolver.openOutputStream(this).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, DEFAULT_IMAGE_QUALITY, out)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    pendingValues.clear()
                    pendingValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(this, pendingValues, null, null)
                }
                this
            }
        }
    }

    fun savePictureToSharedStorage(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        description: String? = null,
        subFolder: String = ""
    ): Single<Uri?> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePictureToSharedStorageSdk29Above(
                context, bitmap, filename, description, subFolder
            )
        } else {
            savePictureToSharedStorageSdk28Below(
                context, bitmap, filename, subFolder
            )
        }.subscribeOn(Schedulers.io())
    }


    @TargetApi(Build.VERSION_CODES.Q)
    private fun createImagePendingValues(
        filename: String,
        description: String?,
        subFolder: String
    ): ContentValues {
        return ContentValues().apply{
            val now = System.currentTimeMillis() / 1000
            put(MediaStore.Images.Media.TITLE, filename)
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.DESCRIPTION, description)
            put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE_JPEG)
            put(MediaStore.Images.Media.DATE_ADDED, now)
            put(MediaStore.Images.Media.DATE_MODIFIED, now)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (subFolder.isNotEmpty()) {
                    put(MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}${File.separator}$subFolder")
                }

                val nextDay = (System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS) / 1000
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.DATE_EXPIRES, nextDay)
            }
        }
    }

    /**
     * uri scheme either content:// or file://
     */
    fun getFileSizeFromUri(contentResolver: ContentResolver, uri: Uri): Long {
        return try {
            val start = System.currentTimeMillis()
            Log.d("TestMe", "open fd start")
            val fd = contentResolver.openFileDescriptor(uri, "r")
            Log.d("TestMe", "open fd end: ${System.currentTimeMillis() - start}")
            fd?.use { it.statSize } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun queryFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        return contentResolver.query(uri, projection, null, null, null)?.use {
            it.moveToFirst()
            it.getString(0)
        } ?: ""
    }

    fun getFilePathFromUri(contentResolver: ContentResolver, uri: Uri): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        return contentResolver.query(uri, projection, null, null, null)?.use {
            it.moveToFirst()
            it.getString(0)
        }
    }

    private const val MIME_TYPE_JPEG = "image/jpeg"
    private const val DEFAULT_IMAGE_QUALITY = 90
}