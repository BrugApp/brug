package com.github.brugapp.brug

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.github.brugapp.brug.messaging.MyFCMMessagingService

private const val CAMERA_REQUEST_CODE = 101

//library found on github: https://github.com/yuriy-budiyev/code-scanner
class QrCodeScannerActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)
        checkPermission()
        codeScanner()

        findViewById<Button>(R.id.buttonReportItem).setOnClickListener {
            displayReportNotification()
        }
    }
    private fun checkPermission(){
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if ( permission == PackageManager.PERMISSION_DENIED)
            ActivityCompat
                .requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE)
    }

    private fun codeScanner() {
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false
            decodeCallback = DecodeCallback {
                runOnUiThread {
                    findViewById<EditText>(R.id.editTextReportItem).setText(it.text)
                }
            }
            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("", "Camera initialization error: ${it.message}")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun displayReportNotification() {
        MyFCMMessagingService.sendNotification(this, "Item found",
            "One of your Items was found !")
    }

}