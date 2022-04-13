package com.github.brugapp.brug.view_model

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.fake.MockDatabase

class SettingsViewModel: ViewModel() {
    fun setProfilePic(drawable: Drawable?) {
        MockDatabase.currentUser.setProfilePicture(drawable)
    }

    fun getProfilePic(): Drawable? {
        return MockDatabase.currentUser.getProfilePicture()
    }

    fun getUsername(): String {
        return MockDatabase.currentUser.getFirstName() + " " + MockDatabase.currentUser.getLastName()
    }

}