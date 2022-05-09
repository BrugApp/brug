package com.github.brugapp.brug.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter


class QrCodeShowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_show)
        //get Extra from intent
        val qrId: String? = intent.getStringExtra("qrId")

        val textView = findViewById<TextView>(R.id.codeId)
        textView.text = qrId
        textView.textSize = 30f
        //print to log the qrId

        val imageView = findViewById<ImageView>(R.id.showQrCode)
        //create Bitmap from string qrId
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(qrId, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            imageView.setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }


    }
}