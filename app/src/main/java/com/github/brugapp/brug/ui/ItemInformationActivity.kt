package com.github.brugapp.brug.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.github.brugapp.brug.R

private const val NOT_IMPLEMENTED: String = "no information yet"
class ItemInformationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_information)

        //This function will set the text for all the textView
        setTextAllView()
    }

    private fun setTextAllView() {
        val extras = intent.extras
        setTextView(R.id.tv_name, extras, "title")
        setTextView(R.id.item_name, extras, "title")
        setTextView(R.id.item_last_location, extras, "lastLocation")
        setTextView(R.id.item_owner, extras, "owner")
        setTextView(R.id.item_date, extras, "addedOn")
        setTextView(R.id.item_description, extras, "description")

        //get and set the icon
        val icon = findViewById<ImageView>(R.id.img)
        val img: Int = extras?.get("image") as Int
        icon.setImageResource(img)
    }

    private fun setTextView(textId:Int, b:Bundle?, value:String){
        //get value from the Extra
        val str = b?.get(value)

        //if we have no text, we will show a basic text
        if(str == null){
            findViewById<TextView>(textId).text = NOT_IMPLEMENTED
        }else
            findViewById<TextView>(textId).text = str.toString()
    }
}