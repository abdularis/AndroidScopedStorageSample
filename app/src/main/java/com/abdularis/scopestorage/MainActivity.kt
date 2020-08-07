package com.abdularis.scopestorage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.abdularis.scopestorage.utils.StorageUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("TestMe", Environment.getExternalStorageDirectory().absolutePath)
        Log.d("TestMe", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath)

        button.setOnClickListener {
            downloadFile(this, Uri.parse("https://kopadmin.arisudesu.com/images/logos/logo.png"), "hasil downloadan coy.png")
        }

        button2.setOnClickListener {
            writeText(
                Environment.getExternalStoragePublicDirectory("/${Environment.DIRECTORY_DOWNLOADS}").absolutePath,
                "this_is_my_file.txt",
                "yes hello plus: " + System.currentTimeMillis()
            )
        }

        button3.setOnClickListener {
            this.showOpenDocumentPicker(requestCode = 100)
        }

        button4.setOnClickListener {
            this.showOpenDocumentTreePicker(200)
        }

        btnSave.setOnClickListener {
            saveBitmap()
        }

        btnSaveMedia.setOnClickListener {
            saveBitmapMediaStore()
        }


        Log.d("TestMe", "files dir: ${filesDir.absolutePath}")
        Log.d("TestMe", "cache dir: ${cacheDir.absolutePath}")
        Log.d("TestMe", "external dir: ${getExternalFilesDir("subfolder")?.absolutePath}")
        Log.d("TestMe", "external cache dir: ${externalCacheDir?.absolutePath}")

        ContextCompat.getExternalFilesDirs(this, null).forEach {
            Log.d("TestMe", "ext dir: ${it.absolutePath}")
        }

        val sm = getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val method = sm.javaClass.getMethod("getVolumeList")

        Log.d("TestMe", "method: $method")

        Log.d("TestMe", "result: ${Utils.getExtendedMemoryPath(this)}")


        btnUpload.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("TestMe", "activity result $requestCode: ${data?.data}")

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                Intent(this, ImageActivity::class.java).apply {
                    putExtra(ImageActivity.EXTRA_IMAGE_URI, it)
                    setData(it)
//                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    startActivity(this)
                }
            }
        }
    }

    private val WRITE_REQUEST_CODE: Int = 43
    private fun createFile(mimeType: String, fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as
            // a file (as opposed to a list of contacts or timezones).
            addCategory(Intent.CATEGORY_OPENABLE)

            // Create a file with the requested MIME type.
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        startActivityForResult(intent, WRITE_REQUEST_CODE)
    }


    private fun saveBitmap() {
        val bmp = BitmapFactory.decodeResource(resources, R.drawable.photo)
        Log.d("TestMe", "bitmap decoded")
        StorageUtil.savePictureToPrivateExternalStorage(
            this,
            bmp,
            "output_from_example_app.jpg"
        ).observeOn(AndroidSchedulers.mainThread())
            .subscribe { path ->
                Log.d("TestMe", "saved at: $path")
                Toast.makeText(this, "saved successfully", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveBitmapMediaStore() {
        val bmp = BitmapFactory.decodeResource(resources, R.drawable.photo)
        Log.d("TestMe", "bitmap decoded")
        StorageUtil.savePictureToSharedStorage(
            this,
            bmp,
            "write_to_mediastore.jpg",
            subFolder = "MY_APP_TEST"
        ).observeOn(AndroidSchedulers.mainThread())
            .subscribe { uri ->
                Log.d("TestMe", "saved to $uri, path: ${StorageUtil.getFilePathFromUri(this.contentResolver, uri!!)}")
                Log.d("TestMe", "inserted file size: ${StorageUtil.getFileSizeFromUri(this.contentResolver, uri!!)}")
                Toast.makeText(this, "saved successfully", Toast.LENGTH_SHORT).show()
            }
    }
}