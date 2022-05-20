package com.github.brugapp.brug.ui


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.UserRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

const val EXTRA_SIGN_OUT = "com.github.brugapp.brug.SIGN_OUT"

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseStorage: FirebaseStorage

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
            signOut()
        }

        setPicAndName()
    }

    private fun setPicAndName() = runBlocking{

        val pic = findViewById<ImageView>(R.id.settingsUserPic)
        val name = findViewById<TextView>(R.id.settingsUserName)

        val user = UserRepository.getMinimalUserFromUID(
            firebaseAuth.currentUser!!.uid,
            firestore,
            firebaseAuth,
            firebaseStorage
        )

        if (user == null) {
            Snackbar.make(findViewById(android.R.id.content),
                "ERROR: User cannot be retrieved !", Snackbar.LENGTH_LONG)
                .show()

        } else {
            val profilePicDrawable = Drawable.createFromPath(user.getUserIconPath())

            if (profilePicDrawable != null) {
                pic.setImageDrawable(resize(profilePicDrawable))
            } else {
                pic.setImageResource(R.mipmap.ic_launcher_round)
            }

            name.text = user.getFullName()
        }

    }

    private fun signOut() {
        val myIntent = Intent(this, SignInActivity::class.java).apply {
            putExtra(EXTRA_SIGN_OUT, true)
        }
        startActivity(myIntent)
    }

    private fun resize(image: Drawable?): Drawable? {
        if (image == null) return null
        val b = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(b, 200, 200, false)
        return BitmapDrawable(resources, bitmapResized)
    }
}