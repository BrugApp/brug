package com.github.brugapp.brug.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
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


    private val PREFS_NAME = "unlostPrefs"
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

        initNightMode()

        findViewById<SwitchCompat>(R.id.night_mode_toggle).setOnCheckedChangeListener{ _, checked ->
            when(checked) {
                true -> setNightMode(true)
                false -> setNightMode(false)
            }
        }

        setPicAndName()
    }

    private fun initNightMode() {
        val settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val nightMode = settings.contains("nightMode")
        if (nightMode) {
            val isEnabled = settings.getBoolean("nightMode", false)
            setNightMode(isEnabled)
        }
        else {
            setNightMode(false)
        }
    }

    private fun setNightMode(enabled: Boolean){
        val toggle = findViewById<SwitchCompat>(R.id.night_mode_toggle)
        val settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()

        if(enabled) {
            toggle.isChecked = true
            editor.putBoolean("nightMode", true)
            editor.apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        else {
            toggle.isChecked = false
            editor.putBoolean("nightMode", false)
            editor.apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setPicAndName() = runBlocking{
        val pic = findViewById<ImageView>(R.id.settingsUserPic)
        val name = findViewById<TextView>(R.id.settingsUserName)

        val user = UserRepository.getUserFromUID(
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