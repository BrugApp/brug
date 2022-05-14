package com.github.brugapp.brug.ui


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val EXTRA_SIGN_OUT = "com.github.brugapp.brug.SIGN_OUT"
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val button: Button = findViewById(R.id.changeProfilePictureButton)
        button.setOnClickListener {
            val intent = Intent(this, ProfilePictureSetActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.sign_out_button).setOnClickListener {
            val myIntent = Intent(this, SignInActivity::class.java).apply {
                putExtra(EXTRA_SIGN_OUT, true)
            }
            this.startActivity(myIntent)
        }
    }


}