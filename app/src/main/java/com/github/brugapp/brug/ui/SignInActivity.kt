package com.github.brugapp.brug.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ACTION_LOST_ERROR_MSG
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.view_model.SignInViewModel
import com.google.android.gms.common.SignInButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            val context = this
            findViewById<ProgressBar>(R.id.loadingUser).visibility = View.VISIBLE
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                if(!BrugDataCache.isNetworkAvailable()){
                    findViewById<ProgressBar>(R.id.loadingUser).visibility = View.GONE
                    Toast.makeText(context, ACTION_LOST_ERROR_MSG, Toast.LENGTH_LONG).show()
                } else {
                    val signInIntent: Intent = viewModel.getSignInIntent()
                    getSignInResult.launch(signInIntent)
                }
            }
        }

        findViewById<Button>(R.id.qr_found_btn).setOnClickListener {
            val myIntent = Intent(this, QrCodeScannerActivity::class.java)
            startActivity(myIntent)
        }

        findViewById<Button>(R.id.demo_button).setOnClickListener {
            findViewById<ProgressBar>(R.id.loadingUser).visibility = View.VISIBLE
            // ONLY FOR DEMO MODE
            val context = this
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                if(!BrugDataCache.isNetworkAvailable()){
                    findViewById<ProgressBar>(R.id.loadingUser).visibility = View.GONE
                    Toast.makeText(context, ACTION_LOST_ERROR_MSG, Toast.LENGTH_LONG).show()
                } else {
                    val result = viewModel.goToDemoMode(firestore, firebaseAuth, firebaseStorage)
                    if(result) {
                        val user = UserRepository.getUserFromUID(
                            firebaseAuth.uid!!,
                            firestore,
                            firebaseAuth,
                            firebaseStorage
                        )
                        if(user != null){
                            BrugDataCache.setUserInCache(user)
                            startActivity(Intent(context, ItemsMenuActivity::class.java))
                        }
                    }
                    else Snackbar.make(
                        context.findViewById(android.R.id.content),
                        "ERROR: Unable to connect for demo mode", Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(viewModel.getAuth().currentUser != null){
            if(intent.extras != null && intent.extras!!.containsKey(EXTRA_SIGN_OUT)){
                viewModel.viewModelScope.launch(Dispatchers.IO) { viewModel.signOut(firestore) }
            } else {
                val myIntent = Intent(this, ItemsMenuActivity::class.java)
                startActivity(myIntent)
            }
        }
    }

    val getSignInResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Handle the returned Uri
            if (it.resultCode == Activity.RESULT_OK) {
                // CALL FUNCTION TO CREATE USER & GO TO ITEMSMENUACTIVITY
                liveData(Dispatchers.IO){
                    emit(viewModel.createNewBrugAccount(it.data, firestore, firebaseAuth, firebaseStorage))
                }.observe(this){ result ->
                    val context = this

                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        val user = UserRepository.getUserFromUID(
                            firebaseAuth.uid!!,
                            firestore,
                            firebaseAuth,
                            firebaseStorage
                        )
                        if(result && user != null) {
                            BrugDataCache.setUserInCache(user)
                            startActivity(Intent(context, ItemsMenuActivity::class.java))
                        }
                    else {
                        Snackbar.make(
                                context.findViewById(android.R.id.content),
                                "ERROR: Unable to connect to your account", Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
}