package com.github.brugapp.brug

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        triggerGoToActivity(R.id.log_on_button, SignInActivity::class.java)
        triggerGoToActivity(R.id.tempButton, ItemsMenuActivity::class.java)
        triggerGoToActivity(R.id.mainCamera, QrCodeScannerActivity::class.java)
        triggerGoToActivity(R.id.action_settings, SettingsActivity::class.java)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.launchbar, menu)
        return true
    }


    private fun <T> triggerGoToActivity(buttonId: Int, activity: Class<T>?) {
        findViewById<Button>(buttonId).setOnClickListener {
            val myIntent = Intent(this, activity).apply {  }
            startActivity(myIntent)
        }
    }

}
