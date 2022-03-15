package com.github.brugapp.brug

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.fake.MockDatabase
import com.github.brugapp.brug.model.Item

class AddItemActivity : AppCompatActivity() {

    private lateinit var itemType : Spinner
    private lateinit var description : EditText
    private lateinit var addButton : Button
    private lateinit var itemName : EditText
    private lateinit var nameHelperText : TextView

    companion object {
        const val DESCRIPTION_LIMIT = 60
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

        itemName = findViewById(R.id.itemName)

        nameHelperText = findViewById(R.id.itemNameHelper)

        // allowing the user the verify if the item's name he/she enters is valid
        //nameFocusListener()

        addButton = findViewById(R.id.add_item_button)
        addButton.setOnClickListener {

            if(verifyForm()){
                val itemId = 1
                val newItem = Item(itemName.text.toString(), itemId, description.text.toString())
                MockDatabase.currentUser.addItem(newItem)

                val myIntent = Intent(this, ItemsMenuActivity::class.java).apply {  }
                startActivity(myIntent)
            }
        }
    }

    private fun verifyForm() : Boolean{

        //checking if the item's name is valid (i.e non-empty)
        nameHelperText.text = validName()

        if(nameHelperText.text.isNotEmpty()){

            /* Alert window pops up to explain that name is invalid
            AlertDialog.Builder(this)
                .setTitle("Invalid Name")
                .setMessage(nameHelperText.text)
                .setPositiveButton("Continue"){ _,_ ->

                }
                .show()*/
            return false
        }

        nameHelperText.text = getString(R.string.required)
        return true
    }

    private fun validName(): String {
        val nameText = itemName.text.toString()
        if(nameText.isEmpty()){
            return "Name must contain at least 1 character"
        }

        return ""
    }

}
