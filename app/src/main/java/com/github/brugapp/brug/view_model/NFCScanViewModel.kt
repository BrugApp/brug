package com.github.brugapp.brug.view_model

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel

private const val NFC_REQUEST_CODE = 1000101

class NFCScanViewModel : ViewModel() {
    fun checkPermission(context: Context){
        val perm = ContextCompat.checkSelfPermission(context,Manifest.permission.NFC)
        if(perm == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(Manifest.permission.NFC), NFC_REQUEST_CODE)
    }
}