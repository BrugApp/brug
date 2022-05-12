package com.github.brugapp.brug.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.liveData
import com.github.brugapp.brug.R
import com.github.brugapp.brug.di.sign_in.DatabaseUser
import com.github.brugapp.brug.view_model.SignInViewModel
import com.google.android.gms.common.SignInButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private val viewModel: SignInViewModel by viewModels()

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseStorage: FirebaseStorage

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        findViewById<Button>(R.id.nfc_found_btn).setOnClickListener{
            val myIntent = Intent(this,NFCScannerActivity::class.java)
            startActivity(myIntent)
        }

        // Set Listener for google sign in button
        findViewById<SignInButton>(R.id.sign_in_google_button).setOnClickListener {
            val signInIntent: Intent = viewModel.getSignInIntent()
            getSignInResult.launch(signInIntent)
        }

        findViewById<Button>(R.id.qr_found_btn).setOnClickListener {
            val myIntent = Intent(this, QrCodeScannerActivity::class.java)
            startActivity(myIntent)
        }

        findViewById<Button>(R.id.demo_button).setOnClickListener {
            findViewById<ProgressBar>(R.id.loadingUser).visibility = View.VISIBLE
            // ONLY FOR DEMO MODE
            liveData(Dispatchers.IO){
                emit(viewModel.goToDemoMode(firestore, firebaseAuth))
            }.observe(this){ result ->
                if(result) startActivity(Intent(this, ItemsMenuActivity::class.java))
                else Snackbar.make(
                    this.findViewById(android.R.id.content),
                    "ERROR: Unable to connect for demo mode", Snackbar.LENGTH_LONG
                ).show()
            }
        }

        findViewById<Button>(R.id.mapDemoButton).setOnClickListener {
            val myIntent = Intent(this, NavigationMenuActivity::class.java)
            startActivity(myIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (intent.extras != null) {
            val signOutNeeded: Boolean = intent.extras!!.get(EXTRA_SIGN_OUT) as Boolean
            if (signOutNeeded) {
                runBlocking { viewModel.signOut(firestore) }
            }
        }
        val currentUser = viewModel.getAuth().currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: DatabaseUser?) {
        // If already signed-in display welcome message and sign out button
        if (user != null) {
            findViewById<ProgressBar>(R.id.loadingUser).visibility = View.VISIBLE
            val myIntent = Intent(this, ItemsMenuActivity::class.java)
            startActivity(myIntent)
            findViewById<SignInButton>(R.id.sign_in_google_button).visibility = View.GONE
            findViewById<Button>(R.id.qr_found_btn).visibility = View.GONE
            findViewById<Button>(R.id.nfc_found_btn).visibility = View.GONE
        } else {
            findViewById<SignInButton>(R.id.sign_in_google_button).visibility = View.VISIBLE
            findViewById<Button>(R.id.qr_found_btn).visibility = View.VISIBLE
            findViewById<Button>(R.id.nfc_found_btn).visibility = View.VISIBLE
        }
    }

    val getSignInResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Handle the returned Uri
            if (it.resultCode == Activity.RESULT_OK) {
                // CALL FUNCTION TO CREATE USER & GO TO ITEMSMENUACTIVITY
                liveData(Dispatchers.IO){
                    emit(viewModel.createNewBrugAccount(it.data, firestore))
                }.observe(this){ result ->
                    if(result) startActivity(Intent(this, ItemsMenuActivity::class.java))
                    else Snackbar.make(
                        this.findViewById(android.R.id.content),
                        "ERROR: Unable to connect to your account", Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
}