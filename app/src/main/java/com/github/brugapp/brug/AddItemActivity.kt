package com.github.brugapp.brug

import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity


class AddItemActivity : AppCompatActivity() {

    lateinit var itemType : Spinner
    lateinit var description : EditText
    //lateinit var addButton : Button
    //lateinit var itemName : EditText

    companion object {
        const val DESCRIPTION_LIMIT = 60
        //const val MIN_NAME_LENGTH = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        itemType = findViewById(R.id.itemTypeSpinner)
        val types = arrayOf("Wallet", "Bag", "AirPods", "Phone", "Id", "Other")

        itemType.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, types)

        description = findViewById(R.id.itemDescription)
        val filterArray = arrayOfNulls<InputFilter>(1)
        // Limiting the length of the description to DESCRIPTION_LIMIT chars
        filterArray[0] = LengthFilter(DESCRIPTION_LIMIT)

        description.filters = filterArray

        //itemName = findViewById(R.id.itemName)


        //addButton = findViewById(R.id.add_item_button)


    }
     /*
    private fun verifyNameField() : Boolean {

        if(itemName.text.length < MIN_NAME_LENGTH){
            // send a message saying to enter a valid name itemName.setTe
            return false
        }

        return true
    }*/



}
