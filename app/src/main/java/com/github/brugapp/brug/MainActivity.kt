package com.github.brugapp.brug

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.ui.*



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        triggerGoToActivity(R.id.log_on_button, SignInActivity::class.java)
        triggerGoToActivity(R.id.tempButton, ItemsMenuActivity::class.java)
        triggerGoToActivity(R.id.mainCamera, QrCodeScannerActivity::class.java)
        triggerGoToActivity(R.id.chat, ChatActivity::class.java)
        triggerGoToActivity(R.id.signUpButton, RegisterUserActivity::class.java)
        triggerGoToActivity(R.id.mapButton, NavigationViewActivity::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.launchbar, menu)
        return true
    }

    // For Settings button
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun <T> triggerGoToActivity(buttonId: Int, activity: Class<T>?) {
        findViewById<Button>(buttonId).setOnClickListener {
            val myIntent = Intent(this, activity).apply {  }
            startActivity(myIntent)
        }
    }
}
