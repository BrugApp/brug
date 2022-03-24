package com.github.brugapp.brug.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.SwitchCompat
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.view_model.ItemInformationViewModel
import com.github.brugapp.brug.view_model.ItemsMenuViewModel

private const val NOT_IMPLEMENTED: String = "no information yet"

class ItemInformationActivity : AppCompatActivity() {
    private val viewModel: ItemInformationViewModel by viewModels()
    private val menuViewModel: ItemsMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_information)

        //This function will set the text for all the textView
        val index: Int = intent.getIntExtra("index",-1)
        val item: Item = menuViewModel.getItemsList()[index]
        val textSet:HashMap<String,String> = viewModel.getText(item)
        setTextAllView(textSet)

        val switch: SwitchCompat = findViewById(R.id.isLostSwitch)
        switch.isChecked = item.isLost()
        switch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setLostValue(isChecked)
        }


    }

    private fun setTextAllView(textSet: HashMap<String,String>) {
        setTextView(R.id.tv_name, textSet["title"])
        setTextView(R.id.item_name, textSet["title"])
        setTextView(R.id.item_last_location, textSet["lastLocation"])
        setTextView(R.id.item_owner, textSet["owner"])
        setTextView(R.id.item_date, textSet["addedOn"])
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