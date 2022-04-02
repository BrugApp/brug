package com.github.brugapp.brug.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.SettingsFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject lateinit var fragmentFactory: SettingsFragmentFactory
    //private val viewModel: SettingsViewModel by viewModels()
    //private val getContent = registerForActivityResult(GetContent()) { uri: Uri? ->
    //    if (uri != null) {
    //        viewModel.setProfilePic(uri, this)
    //    }
    //    val myIntent = Intent(this, SettingsActivity::class.java)
    //    startActivity(myIntent)
    //}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.fragmentFactory = fragmentFactory
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment,SettingsFragment::class.java,null)
            .commit()
       // val button = findViewById<Button>(R.id.loadButton)
       // button.setOnClickListener {
       //     getContent.launch("image/*")
   //  //       ImageContract(this.activityResultRegistry).getImageFromGallery().observe(this) {
   //  //           it?.let { u ->
   //  //               viewModel.setProfilePic(u, this)
   //  //           }
   //  //       }
        }
    }