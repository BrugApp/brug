package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.View
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
import com.github.brugapp.brug.view_model.QrCodeScanViewModel
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


    /**
     * TODO
     *
     * @param savedInstanceState
     */

    lateinit var itemDesc : EditText
    lateinit var itemName : EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        // ITEM NAME PART
        val itemNameHelper = findViewById<TextView>(R.id.itemNameHelper)
        itemName = findViewById(R.id.itemName)

        // Limiting the length of the description to DESCRIPTION_LIMIT chars
        itemDesc = findViewById(R.id.itemDescription)
        itemDesc.filters = arrayOf(LengthFilter(DESCRIPTION_LIMIT))

        // SPINNER HOLDING THE TYPES
        val itemType = findViewById<Spinner>(R.id.itemTypeSpinner)
        itemType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            ItemType.values()
        )

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

    /**
     * TODO
     *
     * @param itemName
     * @param itemNameHelper
     * @param itemType
     * @param itemDesc
     * @param firebaseAuth
     */
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

    /**
     * TODO
     *
     * @param itemName
     * @param itemNameHelper
     * @param itemType
     * @param itemDesc
     * @param firebaseAuth
     */
    private fun addItemOnListener(
        itemName: EditText,
        itemNameHelper: TextView,
        itemType: Spinner,
        itemDesc: EditText,
        firebaseAuth: FirebaseAuth
    ) {
        if (viewModel.verifyForm(itemNameHelper, itemName)) {
            val newItem = Item(
                itemName.text.toString(),
                itemType.selectedItemId.toInt(),
                itemDesc.text.toString(),
                false
            )

            viewModel.viewModelScope.launch {
                ItemsRepository.addItemToUser(
                    newItem,
                    firebaseAuth.currentUser!!.uid,
                    firestore
                )
            }

            val myIntent = Intent(this, ItemsMenuActivity::class.java)
            startActivity(myIntent)
        }
    }

}
