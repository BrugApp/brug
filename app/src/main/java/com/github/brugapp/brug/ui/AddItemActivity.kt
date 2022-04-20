package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepo
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.view_model.AddItemViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking

const val DESCRIPTION_LIMIT = 60

class AddItemActivity : AppCompatActivity() {

    private val viewModel: AddItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        // ITEM NAME PART
        val itemName = findViewById<EditText>(R.id.itemName)
        val itemNameHelper = findViewById<TextView>(R.id.itemNameHelper)

        // SPINNER HOLDING THE TYPES
        val itemType = findViewById<Spinner>(R.id.itemTypeSpinner)
        itemType.adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1,
            ItemType.values())
//        val spinnerHelper = findViewById<TextView>(R.id.spinnerHelper) //MAYBE USELESS

        // Limiting the length of the description to DESCRIPTION_LIMIT chars
        val itemDesc = findViewById<EditText>(R.id.itemDescription)
        itemDesc.filters = arrayOf(LengthFilter(DESCRIPTION_LIMIT))

        val addButton = findViewById<Button>(R.id.add_item_button)
        addButton.setOnClickListener {
            addItemOnListener(itemName, itemNameHelper, itemType, itemDesc)
        }
    }

    private fun addItemOnListener(itemName: EditText,
                                  itemNameHelper: TextView,
                                  itemType: Spinner,
                                  itemDesc: EditText){
        if(viewModel.verifyForm(itemNameHelper,itemName)){
//               user.addItemToList(newItem)
            val newItem = MyItem(
                itemName.text.toString(),
                itemType.selectedItemId.toInt(),
                itemDesc.text.toString(),
                false)

            runBlocking {ItemsRepo.addItemToUser(newItem, Firebase.auth.currentUser!!.uid)}

            val myIntent = Intent(this, ItemsMenuActivity::class.java)
            startActivity(myIntent)
        }
    }

}
