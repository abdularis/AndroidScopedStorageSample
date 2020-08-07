package com.abdularis.scopestorage

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

fun downloadFile(context: Context, url: Uri, fileName: String) {
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(url).apply {
        setTitle("Ayoo download")
        setDescription("lagi download nich")
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        setVisibleInDownloadsUi(true)
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    }

    dm.enqueue(request)
}

fun writeText(dir: String, fileName: String, text: String) {
    val dirFile = File(dir)
    if (!dirFile.exists()) {
        Log.d("TestMe", "dirFile created: ${dirFile.mkdirs()}")
    }

    val file = File(dirFile.absolutePath + "/" + fileName)
    val fos = FileOutputStream(file)
    val osw = OutputStreamWriter(fos)

    osw.write(text)
    osw.close()
}


fun createFileUri(context: Context, fileName: String) {
    val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val relativeLocation = "HelloPics"

    val contentDetails = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.RELATIVE_PATH, relativeLocation)
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val contentUri = context.contentResolver.insert(imageCollection, contentDetails)
    contentUri?.let {

        context.contentResolver.openFileDescriptor(it, "w").use { parcelFileDesc ->
            ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDesc)
        }

        contentDetails.clear()
        contentDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(it, contentDetails, null, null)
    }
}
