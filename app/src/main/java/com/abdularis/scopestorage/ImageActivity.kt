package com.abdularis.scopestorage

import android.database.Cursor
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.documentfile.provider.DocumentFile
import com.abdularis.scopestorage.utils.StorageUtil
import com.abdularis.scopestorage.utils.UploadUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    private val imageUri: String by lazy {
        intent.extras?.getString(EXTRA_IMAGE_URI).orEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        Glide.with(this)
            .load(intent.data)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("TestMe", "load failed: $e")
                    progressBar.visibility = View.GONE
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("TestMe", "resource ready")
                    progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(imageView)

        val docFile = DocumentFile.fromSingleUri(this, intent.data!!)
        Log.d("TestMe", "-----------------")
        Log.d("TestMe", "${docFile?.name}")
        Log.d("TestMe", "${docFile?.type}")
        Log.d("TestMe", "is virtual: ${docFile?.isVirtual}")
        Log.d("TestMe", "read: ${docFile?.canRead()}")
        Log.d("TestMe", "write: ${docFile?.canWrite()}")

        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.VOLUME_NAME,
            MediaStore.Images.Media.IS_PENDING
        )
        contentResolver.query(intent.data!!, projection, null, null, null).use {
            val v = it?.mapToList { c ->
                "name: ${c.getString(0)}, v: ${c.getString(1)}, pending: ${c.getInt(2)}"
            }
            Log.d("TestMe", "--> $v")
        }


        val start = System.currentTimeMillis()
        Log.d("TestMe", "start")
        val size = StorageUtil.getFileSizeFromUri(this.contentResolver, intent.data!!)
        Log.d("TestMe", "end: ${System.currentTimeMillis() - start}")
        Log.d("TestMe", "==============================================")
        Log.d("TestMe", "file size: $size, (${size.toFloat() / 1024f / 1024f} MB)")

        Log.d("TestMe", "file path: ${StorageUtil.getFilePathFromUri(contentResolver, intent.data!!)}")



        Single.fromCallable {
            UploadUtil.uploadFile(contentResolver, intent.data!!)
        }.subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun <T : Any> Cursor.mapToList(predicate: (Cursor) -> T): List<T> =
        generateSequence {
            if (moveToNext()) predicate(this) else null
        }.toList()

    companion object {
        const val EXTRA_IMAGE_URI = "image_uri"
    }
}