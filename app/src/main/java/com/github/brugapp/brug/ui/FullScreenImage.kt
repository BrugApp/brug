package com.github.brugapp.brug.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R

/**
 * Class of the FullScreenImage activity
 * Used to display PicMessages in full screen
 */
class FullScreenImage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val urlString: String = intent.getStringExtra("messageUrl").toString()

        val img: ImageView = findViewById(R.id.selectedImage)
        img.setImageURI(Uri.parse(urlString))
    }
}
