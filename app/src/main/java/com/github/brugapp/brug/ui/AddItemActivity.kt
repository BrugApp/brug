package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.AddItemViewModel

const val DESCRIPTION_LIMIT = 60

class AddItemActivity : AppCompatActivity() {

    private lateinit var itemType : Spinner
    private lateinit var description : EditText
    private lateinit var addButton : Button
    private lateinit var itemNameView : EditText
    private lateinit var nameHelperText : TextView

    private val viewModel: AddItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        itemType = findViewById(R.id.itemTypeSpinner)
        val types = arrayOf("Wallet", "Keys","Car keys", "Phone", "Other")
        itemType.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, types)

        description = findViewById(R.id.itemDescription)
        val filterArray = arrayOfNulls<InputFilter>(1)
        // Limiting the length of the description to DESCRIPTION_LIMIT chars
        filterArray[0] = LengthFilter(DESCRIPTION_LIMIT)
        description.filters = filterArray

        itemNameView = findViewById(R.id.itemName)

        nameHelperText = findViewById(R.id.itemNameHelper)

        addButton = findViewById(R.id.add_item_button)
        addButton.setOnClickListener {
            addItemOnListener()
        }
    }



    private fun addItemOnListener(){
        if(viewModel.verifyForm(nameHelperText,itemNameView)){
            viewModel.addItem(itemNameView,description,itemType)

            val myIntent = Intent(this, ItemsMenuActivity::class.java).apply {  }
            startActivity(myIntent)
        }
    }

}
