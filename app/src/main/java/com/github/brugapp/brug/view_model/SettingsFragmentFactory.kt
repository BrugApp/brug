package com.github.brugapp.brug.view_model

import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.brugapp.brug.ui.ProfileSettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject


class SettingsFragmentFactory @Inject constructor(
    val registry: ActivityResultRegistry,
    val firebaseFirestore: FirebaseFirestore,
    val firebaseStorage: FirebaseStorage,
    val firebaseAuth: FirebaseAuth
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            ProfileSettingsFragment::class.java.name -> {
                ProfileSettingsFragment(registry, firebaseAuth, firebaseStorage, firebaseFirestore)
            }
            else -> {
                super.instantiate(classLoader, className)
            }
        }
    }
}