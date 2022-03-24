package com.github.brugapp.brug.view_model

import android.widget.*
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.R
import com.github.brugapp.brug.fake.MockDatabase.Companion.currentUser
import com.github.brugapp.brug.fake.MockDatabase.Companion.itemId
import com.github.brugapp.brug.model.Item

class AddItemViewModel : ViewModel() {

    fun addItem(itemNameView : EditText, description : EditText){
        val newItem = Item(itemNameView.text.toString(), description.text.toString(), itemId)
        currentUser.addItem(newItem)
    }

    fun verifyForm(nameHelperText : TextView, itemNameView : EditText) : Boolean{

        //checking if the item's name is valid (i.e non-empty)
        nameHelperText.text = validName(itemNameView)

        if(nameHelperText.text.isNotEmpty()){

            /* Alert window pops up to explain that name is invalid
            Currently commented to make testing easier
            AlertDialog.Builder(this)
                .setTitle("Invalid Name")
                .setMessage(nameHelperText.text)
                .setPositiveButton("Continue"){ _,_ ->
                }
                .show()*/
            return false
        }

        return true
    }

     private fun validName(itemNameView : EditText): String {
        val nameText = itemNameView.text.toString()

        return if(nameText.isEmpty()) "Name must contain at least 1 character" else ""
    }



}