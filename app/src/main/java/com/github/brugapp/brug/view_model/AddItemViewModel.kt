package com.github.brugapp.brug.view_model

import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.ItemType

class AddItemViewModel : ViewModel() {

    fun verifyForm(nameHelperText : TextView, itemNameView : EditText) : Boolean{

        //checking if the item's name is valid (i.e non-empty)
        nameHelperText.text = validName(itemNameView)

        return nameHelperText.text.isEmpty()
    }

    private fun validName(itemNameView : EditText): String {
        val nameText = itemNameView.text.toString()

        return if(nameText.isEmpty()) "Name must contain at least 1 character" else ""
    }



}