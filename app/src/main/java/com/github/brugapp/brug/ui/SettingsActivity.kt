package com.github.brugapp.brug.ui

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.SignInViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

const val EXTRA_SIGN_OUT = "com.github.brugapp.brug.SIGN_OUT"
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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