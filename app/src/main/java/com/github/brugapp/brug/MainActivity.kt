package com.github.brugapp.brug

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        triggerGoToActivity(R.id.log_on_button, SignInActivity::class.java)
        triggerGoToActivity(R.id.tempButton, ItemsMenuActivity::class.java)
        triggerGoToActivity(R.id.mainCamera, QrCodeScannerActivity::class.java)
    }

    fun <T> triggerGoToActivity(buttonId: Int, activity: Class<T>?) {
        findViewById<Button>(buttonId).setOnClickListener {
            val myIntent = Intent(this, activity).apply {  }
            startActivity(myIntent)
        }
    }
}



