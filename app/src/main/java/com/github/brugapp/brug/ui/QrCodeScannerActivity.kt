package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.github.brugapp.brug.R
import com.github.brugapp.brug.SUCCESS_TEXT
import com.github.brugapp.brug.data.ACTION_LOST_ERROR_MSG
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.view_model.QrCodeScanViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

//library found on github: https://github.com/yuriy-budiyev/code-scanner
@AndroidEntryPoint
class QrCodeScannerActivity : AppCompatActivity() {

    private val viewModel: QrCodeScanViewModel by viewModels()

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseStorage: FirebaseStorage

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    /**
     * TODO
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)
        viewModel.checkPermissions(this)
        viewModel.codeScanner(this)

        //TODO: REMOVE THIS HARDCODED TEXT
        findViewById<EditText>(R.id.edit_message).setText("J7jDsvME15fNKvLssZ9bezpABHn2:9m7SvfslSij6f7iplq68")

        findViewById<Button>(R.id.buttonReportItem).setOnClickListener {
            val context = this
            liveData(Dispatchers.IO) {
                emit(BrugDataCache.isNetworkAvailable())
            }.observe(this) { networkStatus ->
                if(!networkStatus){
                    Toast.makeText(context, ACTION_LOST_ERROR_MSG, Toast.LENGTH_LONG).show()
                } else {
                    liveData(Dispatchers.IO){
                        emit(viewModel.parseTextAndCreateConv(
                            findViewById<EditText>(R.id.edit_message).text.toString(),
                            context,
                            firebaseAuth, firestore, firebaseStorage
                        ))
                    }.observe(this){ resultMsg ->
                        Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()

                        if(resultMsg == SUCCESS_TEXT){
                            val myIntent =
                                if(firebaseAuth.currentUser == null)
                                    Intent(context, SignInActivity::class.java)
                                else
                                    Intent(context, ChatMenuActivity::class.java)
                            startActivity(myIntent)
                        }
                    }
                }
            }
        }
    }


    /**
     * TODO
     *
     */
    override fun onResume() {
        super.onResume()
        viewModel.startPreview()
    }

    /**
     * TODO
     *
     */
    override fun onPause() {
        viewModel.releaseResources()
        super.onPause()
    }

}