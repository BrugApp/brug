package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.SettingsFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val EXTRA_SIGN_OUT = "com.github.brugapp.brug.SIGN_OUT"


@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var fragmentFactory: SettingsFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.fragmentFactory = fragmentFactory
        setContentView(R.layout.activity_settings)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, SettingsFragment::class.java, null)
            .commit()
    }



}

