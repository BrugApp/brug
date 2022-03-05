package com.github.brugapp.brug

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.OnCompleteListener


class SignedInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signed_in)

        val userAccount: GoogleSignInAccount? = intent.extras?.get(EXTRA_GOOGLE_ACCOUNT) as GoogleSignInAccount?

        val textView = findViewById<TextView>(R.id.signed_in_user).apply {
            text = "Name: ${userAccount?.displayName}\nEmail: ${userAccount?.email}\nID: ${userAccount?.id}"
        }
    }

    fun signOut(view: View) {
        gsc.signOut()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}