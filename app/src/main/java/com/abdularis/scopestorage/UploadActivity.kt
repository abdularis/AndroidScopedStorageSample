package com.abdularis.scopestorage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.abdularis.scopestorage.utils.StorageUtil
import com.abdularis.scopestorage.utils.UploadUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_upload.*

class UploadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        showUploadStatus(false, "")
        btnShowPicker.setOnClickListener {
            showOpenDocumentPicker(requestCode = 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                upload(it)
            }
        }
    }

    private fun showUploadStatus(uploading: Boolean, filename: String) {
        btnShowPicker.isEnabled = !uploading
        textStatus.text = if (uploading) "Uploading: $filename" else ""
        progressUpload.visibility = if (uploading) View.VISIBLE else View.GONE
    }

    @SuppressLint("CheckResult")
    private fun upload(uri: Uri) {
        val filename = StorageUtil.queryFileNameFromUri(contentResolver, uri)
        showUploadStatus(true, filename)

        UploadUtil.uploadFile(contentResolver, uri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { resp ->
                showUploadStatus(false, "")
                Log.d("TestMe", "resp: ${resp.code()}")
            }
    }
}