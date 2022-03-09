package com.github.brugapp.brug

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.log_on_button).setOnClickListener {
            goToLogin()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }


}