package com.github.brugapp.brug

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goToItems(view: View){
        val myIntent = Intent(this, ItemsMenuActivity::class.java).apply{}
        startActivity(myIntent)
    }
}