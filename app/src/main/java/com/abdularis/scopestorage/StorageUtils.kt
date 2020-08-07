package com.abdularis.scopestorage

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception



fun storeBitmap(context: Context, bitmap: Bitmap) {
    try {
        val values = ContentValues().apply{
            put(MediaStore.Images.Media.DISPLAY_NAME,"imgMS.jpeg")
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            put(MediaStore.Images.Media.IS_PENDING,1)
        }

        val contentResolver = context.contentResolver
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val insertedItem = contentResolver.insert(imageCollection, values)

        insertedItem?.let {
            contentResolver.openOutputStream(it).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(insertedItem, values, null, null)
        }
    } catch (e: Exception) {
        Log.d("StorageUtils", "error storing bitmap: $e")
    }
}

fun Activity.showOpenDocumentPicker(
    mimeType: String = "*/*",
    requestCode: Int
) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = mimeType
        addCategory(Intent.CATEGORY_OPENABLE)
    }

    startActivityForResult(intent, requestCode)
}

fun Activity.showOpenDocumentTreePicker(requestCode: Int) {
    startActivityForResult(
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
        requestCode
    )
}