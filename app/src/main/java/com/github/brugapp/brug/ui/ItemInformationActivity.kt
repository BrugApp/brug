package com.github.brugapp.brug.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.liveData
import com.github.brugapp.brug.ITEM_INTENT_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepo
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.view_model.ItemInformationViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers

private const val NOT_IMPLEMENTED: String = "no information yet"

class ItemInformationActivity : AppCompatActivity() {

    private val viewModel: ItemInformationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_information)

        val item = intent.extras!!.get(ITEM_INTENT_KEY) as MyItem

        val textSet: HashMap<String,String> = viewModel.getText(item)
        setTextAllView(textSet)

        val switch: SwitchCompat = findViewById(R.id.isLostSwitch)
        switch.isChecked = item.isLost()
        switch.setOnCheckedChangeListener { _, isChecked ->
            item.changeLostStatus(isChecked)
            liveData(Dispatchers.IO){
                emit(ItemsRepo.updateItemFields(item, Firebase.auth.currentUser!!.uid))
            }.observe(this){ response ->

                val feedbackStr = if(response.onSuccess){
                    "Item state has been successfully changed"
                } else {
                    "ERROR: Item state couldn't be saved"
                }

                Snackbar.make(
                    findViewById(android.R.id.content),
                    feedbackStr,
                    Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setTextAllView(textSet: HashMap<String,String>) {
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

    private fun setTextView(textId:Int, value:String?){

        //if we have no text, we will show a basic text
        if(value == null){
            findViewById<TextView>(textId).text = NOT_IMPLEMENTED
        }else
            findViewById<TextView>(textId).text = value
    }
}