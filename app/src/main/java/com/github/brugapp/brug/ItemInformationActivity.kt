package com.github.brugapp.brug

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class ItemInformationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_information)

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
        val icon = findViewById<ImageView>(R.id.img)
        val img: Int = extras?.get("image") as Int
        icon.setImageResource(img)
    }

    @SuppressLint("SetTextI18n")
    private fun setTextView(textId:Int, b:Bundle?, value:String){
        val str = b?.get(value)

        if(str == null){
            findViewById<TextView>(textId).text = "TODO"
        }else
            findViewById<TextView>(textId).text = b.get(value).toString()
    }
}