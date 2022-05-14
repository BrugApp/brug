package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.viewModelScope
import com.github.brugapp.brug.ITEM_INTENT_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.view_model.ItemInformationViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NOT_IMPLEMENTED: String = "no information yet"

@AndroidEntryPoint
class ItemInformationActivity : AppCompatActivity() {

    private val viewModel: ItemInformationViewModel by viewModels()

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_information)
        val item = intent.extras!!.get(ITEM_INTENT_KEY) as MyItem

        val textSet: HashMap<String, String> = viewModel.getText(item, firebaseAuth)
        setTextAllView(textSet)

        setSwitch(item, firebaseAuth)
        qrCodeButton()
    }

    private fun setSwitch(item: MyItem, firebaseAuth: FirebaseAuth) {
        val switch: SwitchCompat = findViewById(R.id.isLostSwitch)
        switch.isChecked = item.isLost()
        switch.setOnCheckedChangeListener { _, isChecked ->
            item.changeLostStatus(isChecked)
            viewModel.viewModelScope.launch {
                val response = ItemsRepository.updateItemFields(
                    item,
                    firebaseAuth.currentUser!!.uid,
                    firestore
                ).onSuccess

                val feedbackStr = if (response) {
                    "Item state has been successfully changed"
                } else {
                    "ERROR: Item state couldn't be saved"
                }
                Snackbar.make(
                    findViewById(android.R.id.content),
                    feedbackStr,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun qrCodeButton() {
        //if button is clicked, go to QrCodeShow
        val button: Button = findViewById(R.id.qrGen)
        button.setOnClickListener {
            val intent = Intent(this, QrCodeShowActivity::class.java)
            //give qrId to QrCodeShow
            intent.putExtra("qrId", viewModel.getQrId())
            startActivity(intent)
        }
    }

    private fun setTextAllView(textSet: HashMap<String, String>) {
        setTextView(R.id.tv_name, textSet["title"])
        setTextView(R.id.item_name, textSet["title"])
        setTextView(R.id.item_last_location, textSet["lastLocation"])
        setTextView(R.id.item_owner, textSet["owner"])
        setTextView(R.id.item_description, textSet["description"])

        //get and set the icon
        val icon = findViewById<ImageView>(R.id.img)
        val img: Int = textSet["image"]!!.toInt()
        icon.setImageResource(img)
    }

    private fun setTextView(textId: Int, value: String?) {

        //if we have no text, we will show a basic text
        if (value == null) {
            findViewById<TextView>(textId).text = NOT_IMPLEMENTED
        } else
            findViewById<TextView>(textId).text = value
    }
}