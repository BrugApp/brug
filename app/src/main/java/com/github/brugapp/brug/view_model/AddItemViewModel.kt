package com.github.brugapp.brug.view_model

import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModel

class AddItemViewModel : ViewModel() {

    /**
     * Verifies the information that the user entered for the new item
     *
     * @param nameHelperText the helper text of the item name's text field
     * @param itemNameView the item name's view
     * @return true if the item's information are correct
     */
    fun verifyForm(nameHelperText: TextView, itemNameView: EditText): Boolean {
        //checking if the item's name is valid (i.e non-empty)
        nameHelperText.text = validName(itemNameView)
        return nameHelperText.text.isEmpty()
    }

    private fun validName(itemNameView: EditText): String {
        val nameText = itemNameView.text.toString()
        return if (nameText.isEmpty()) "Name must contain at least 1 character" else ""
    }
}