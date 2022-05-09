package com.github.brugapp.brug.view_model

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.budiyev.android.codescanner.*
import com.github.brugapp.brug.R

private const val CAMERA_REQUEST_CODE = 101

class QrCodeScanViewModel : ViewModel() {

    private lateinit var codeScanner: CodeScanner

    fun checkPermission(context: Context) {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_DENIED)
            ActivityCompat
                .requestPermissions(
                    context as Activity, arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
    }

    fun codeScanner(activity: Activity) {
        val scannerView = activity.findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(activity.applicationContext, scannerView)
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false
            decodeCallback = DecodeCallback {
                activity.runOnUiThread {
                    activity.findViewById<EditText>(R.id.edit_message).setText(it.text)
                }
            }
            errorCallback = ErrorCallback {
                activity.runOnUiThread {
                    Log.e("", "Camera initialization error: ${it.message}")
                }
            }
        }
    }

    fun startPreview() {
        codeScanner.startPreview()
    }

    fun releaseResources() {
        codeScanner.releaseResources()
    }


}