package com.github.brugapp.brug.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.liveData
import com.github.brugapp.brug.PIC_ATTACHMENT_INTENT_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.SELECT_PICTURE_REQUEST_CODE
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.data.UserRepository.getImageUri
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
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

        restoreNightModeToggleState()
        val profilePicButton = findViewById<Button>(R.id.changeProfilePictureButton)
        profilePicButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            ActivityCompat.startActivityForResult(
                this,
                intent,
                SELECT_PICTURE_REQUEST_CODE,
                null
            )
        }

        findViewById<Button>(R.id.sign_out_button).setOnClickListener {
            signOut()
        }

        findViewById<SwitchCompat>(R.id.night_mode_toggle).setOnCheckedChangeListener{ _, checked ->
            when(checked) {
                true -> setNightMode(true)
                false -> setNightMode(false)
            }
        }

        setPicAndName()
    }


    private fun restoreNightModeToggleState() {
        val toggle = findViewById<SwitchCompat>(R.id.night_mode_toggle)
        val settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = settings.getBoolean("nightMode", false)
        toggle.isChecked = isEnabled // disabled by default
    }

    private fun setNightMode(enabled: Boolean){
        val settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()

        if(enabled) {
            editor.putBoolean("nightMode", true)
            editor.apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        else {
            editor.putBoolean("nightMode", false)
            editor.apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        finish()
    }

    private fun setPicAndName() {
        val pic = findViewById<ImageView>(R.id.settingsUserPic)
        val name = findViewById<TextView>(R.id.settingsUserName)

        liveData(Dispatchers.IO) {
            emit(
                UserRepository.getUserFromUID(
                    firebaseAuth.currentUser!!.uid,
                    firestore,
                    firebaseAuth,
                    firebaseStorage
                )
            )
        }.observe(this) { resultUser ->
            if(resultUser != null){
                BrugDataCache.setUserInCache(resultUser)
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "ERROR: User cannot be retrieved !", Snackbar.LENGTH_LONG
                ).show()
            }
        }

        BrugDataCache.getCachedUser().observe(this) { user ->
            val userIcon = user?.getUserIconPath()
            val profilePicDrawable = if(userIcon != null){
                Drawable.createFromPath(userIcon)
            } else null

            if (profilePicDrawable != null) {
                pic.setImageDrawable(resize(profilePicDrawable))
            } else {
                pic.setImageResource(R.mipmap.ic_launcher_round)
            }

            name.text = user?.getFullName()
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE_REQUEST_CODE) {
                val imageUri = getImageUri(data)

                val inputStream = contentResolver?.openInputStream(imageUri)
                val drawable = Drawable.createFromStream(inputStream, imageUri.toString())
                liveData(Dispatchers.IO) {
                    emit(
                        UserRepository.updateUserIcon(
                            firebaseAuth.currentUser!!.uid,
                            drawable,
                            firebaseAuth,
                            firebaseStorage,
                            firestore
                        )
                    )
                }.observe(this) {
                    setPicAndName()
                }
            }
        }
    }


}