package com.github.brugapp.brug.ui



import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R

const val EXTRA_SIGN_OUT = "com.github.brugapp.brug.SIGN_OUT"

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val button: Button = findViewById(R.id.changeProfilePictureButton)
        button.setOnClickListener{
            val intent = Intent(this, ProfilePictureSetActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.sign_out_button).setOnClickListener {
            signOut()
        }
    }
    private fun signOut() {
        val myIntent = Intent(this, SignInActivity::class.java).apply {
            putExtra(EXTRA_SIGN_OUT, true)
        }
        startActivity(myIntent)
    }
}