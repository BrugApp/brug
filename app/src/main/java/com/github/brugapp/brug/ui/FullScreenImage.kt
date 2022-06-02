package com.github.brugapp.brug.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val urlString: String = intent.getStringExtra("messageUrl").toString()

        val img: ImageView = findViewById(R.id.selectedImage)
        img.setImageURI(Uri.parse(urlString))
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
