package com.github.brugapp.brug.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.USER_INTENT_KEY
import com.github.brugapp.brug.model.MyUser
import com.github.brugapp.brug.view_model.SettingsFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ProfilePictureSetActivity : AppCompatActivity() {

    @Inject lateinit var fragmentFactory: SettingsFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //FETCHES THE USER FROM SETTINGSACTIVITY & PASS IT TO THE FRAGMENT
        val user = intent.extras!!.get(USER_INTENT_KEY) as MyUser

        supportFragmentManager.fragmentFactory = fragmentFactory
        setContentView(R.layout.activity_profile_picture)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment,
                ProfileSettingsFragment.newInstance(fragmentFactory.registry, user)
            ).commit()
        }
    }