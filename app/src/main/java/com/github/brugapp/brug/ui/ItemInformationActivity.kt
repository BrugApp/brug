package com.github.brugapp.brug.ui

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.liveData
import com.github.brugapp.brug.ITEM_INTENT_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.view_model.ItemInformationViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

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

        val geocoder = Geocoder(this, Locale.getDefault())
        val textSet: HashMap<String, String> = viewModel.getText(item, firebaseAuth,geocoder)
        setTextAllView(textSet)

        setSwitch(item, firebaseAuth)
        //if user click on the localisation textview, we will open the map
        val localisation = item.getLastLocation()
        if(localisation != null) {
            findViewById<TextView>(R.id.item_last_location).setOnClickListener {
                val intent = Intent(this, MapBoxActivity::class.java)
                intent.putExtra(ITEM_INTENT_KEY, item)
                intent.putExtra(EXTRA_DESTINATION_LONGITUDE, localisation.getLongitude())
                intent.putExtra(EXTRA_DESTINATION_LATITUDE, localisation.getLatitude())
                startActivity(intent)
            }
        }

        qrCodeButton()
    }

    private fun setSwitch(item: MyItem, firebaseAuth: FirebaseAuth) {
        val switch: SwitchCompat = findViewById(R.id.isLostSwitch)
        switch.isChecked = item.isLost()
        switch.setOnCheckedChangeListener { _, isChecked ->
            item.changeLostStatus(isChecked)
            liveData(Dispatchers.IO) {
                emit(
                    ItemsRepository.updateItemFields(
                        item,
                        firebaseAuth.currentUser!!.uid,
                        firestore
                    )
                )
            }.observe(this) { response ->
                val feedbackStr = if (response.onSuccess) {
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
            intent.putExtra("itemName", viewModel.getItemName())
            startActivity(intent)
        }
    }

    private fun setTextAllView(textSet: HashMap<String, String>) {
        setTextView(R.id.tv_name, textSet["title"])
        setTextView(R.id.item_last_location, textSet["lastLocation"])
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