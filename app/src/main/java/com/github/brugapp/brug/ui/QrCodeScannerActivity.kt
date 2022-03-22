package com.github.brugapp.brug.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.QrCodeScanViewModel


//library found on github: https://github.com/yuriy-budiyev/code-scanner
class QrCodeScannerActivity : AppCompatActivity() {

    private val viewModel:QrCodeScanViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)
        viewModel.checkPermission(this)
        viewModel.codeScanner(this)
    }


    override fun onResume() {
        super.onResume()
        viewModel.startPreview()
    }

    override fun onPause() {
        viewModel.releaseResources()
        super.onPause()
    }

}