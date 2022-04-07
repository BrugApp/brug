package com.github.brugapp.brug.view_model

import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.brugapp.brug.ui.ProfileSettingsFragment
import javax.inject.Inject



class SettingsFragmentFactory @Inject constructor(
    private val registry: ActivityResultRegistry
): FragmentFactory(){
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className){
            ProfileSettingsFragment::class.java.name -> {
                ProfileSettingsFragment(registry)
            }
            else -> {super.instantiate(classLoader, className)}
        }
    }
}