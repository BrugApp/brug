package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ACTION_LOST_ERROR_MSG
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.view_model.AddItemViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

const val DESCRIPTION_LIMIT = 60

@AndroidEntryPoint
class AddItemActivity : AppCompatActivity() {

    private val viewModel: AddItemViewModel by viewModels()

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        // ITEM NAME PART
        val itemName = findViewById<EditText>(R.id.itemName)
        val itemNameHelper = findViewById<TextView>(R.id.itemNameHelper)

        // SPINNER HOLDING THE TYPES
        val itemType = findViewById<Spinner>(R.id.itemTypeSpinner)
        itemType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            ItemType.values()
        )
//        val spinnerHelper = findViewById<TextView>(R.id.spinnerHelper) //MAYBE USELESS

        // Limiting the length of the description to DESCRIPTION_LIMIT chars
        val itemDesc = findViewById<EditText>(R.id.itemDescription)
        itemDesc.filters = arrayOf(LengthFilter(DESCRIPTION_LIMIT))

        val addButton = findViewById<Button>(R.id.add_item_button)
        addButton.setOnClickListener {
            liveData(Dispatchers.IO){
                emit(BrugDataCache.isNetworkAvailable())
            }.observe(this){ result ->
                if(!result) {
                    Toast.makeText(this, ACTION_LOST_ERROR_MSG, Toast.LENGTH_LONG).show()
                }
            }
            addItemOnListener(itemName, itemNameHelper, itemType, itemDesc, firebaseAuth)
        }

        val addNfcButton = findViewById<Button>(R.id.add_nfc_item)
        addNfcButton.setOnClickListener {
            addNfcItemOnListener(itemName, itemNameHelper, itemType.selectedItemId.toInt(), itemDesc.text.toString(), firebaseAuth)
        }
    }

    fun addNfcItemOnListener(
        itemName: EditText,
        itemNameHelper: TextView,
        itemType: Int,
        itemDesc: String,
        firebaseAuth: FirebaseAuth
    ) {
        var newItemID = ""
        if (viewModel.verifyForm(itemNameHelper, itemName)) {
            val newItem = Item(itemName.text.toString(), itemType, itemDesc, false)
            newItemID = UUID.randomUUID().toString().filter { char -> char!='-' }.subSequence(0,20).toString()
            newItem.setItemID(newItemID)
            runBlocking { ItemsRepository.addItemWithItemID(newItem, newItemID,firebaseAuth.currentUser!!.uid, firestore) }
        }
        val myIntent = Intent(this, NFCScannerActivity::class.java)
        myIntent.putExtra("itemid", newItemID).putExtra("write","true")
        startActivity(myIntent)
    }

    private fun addItemOnListener(
        itemName: EditText,
        itemNameHelper: TextView,
        itemType: Spinner,
        itemDesc: EditText,
        firebaseAuth: FirebaseAuth
    ) {
        if (viewModel.verifyForm(itemNameHelper, itemName)) {
            val newItem = Item(itemName.text.toString(), itemType.selectedItemId.toInt(), itemDesc.text.toString(), false)

            viewModel.viewModelScope.launch { ItemsRepository.addItemToUser(newItem, firebaseAuth.currentUser!!.uid, firestore) }

            val myIntent = Intent(this, ItemsMenuActivity::class.java)
            startActivity(myIntent)
        }
    }

}
