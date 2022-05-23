package com.github.brugapp.brug.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.liveData
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.NETWORK_ERROR_MSG
import com.github.brugapp.brug.view_model.SettingsFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


@AndroidEntryPoint
class ProfilePictureSetActivity : AppCompatActivity() {

    @Inject
    lateinit var fragmentFactory: SettingsFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO: PASS THE USER TO THE FRAGMENT
//        val user = intent.extras!!.get(USER_INTENT_KEY) as MyUser

        liveData(Dispatchers.IO) {
            emit(BrugDataCache.isNetworkAvailable())
        }.observe(this){ status ->
            if(!status) Toast.makeText(this, NETWORK_ERROR_MSG, Toast.LENGTH_LONG).show()
        }

        supportFragmentManager.fragmentFactory = fragmentFactory
        setContentView(R.layout.activity_profile_picture)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, ProfileSettingsFragment::class.java, null)
            .commit()
    }
}