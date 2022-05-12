//package com.github.brugapp.brug.ui
//
//import android.content.Intent
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.widget.Button
//import com.github.brugapp.brug.R
//
//class NavigationMenuActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_navigation_menu)
//
//        findViewById<Button>(R.id.seeItemsButton).setOnClickListener {
//            val myIntent = Intent(this, MapBoxActivity::class.java)
//            startActivity(myIntent)
//        }
//
//        findViewById<Button>(R.id.freeNavigationButton).setOnClickListener {
//            val myIntent = Intent(this, LocationTrackingActivity::class.java)
//            startActivity(myIntent)
//        }
//
//        findViewById<Button>(R.id.navigateButton).setOnClickListener {
//            val myIntent = Intent(this, NavigationViewActivity::class.java)
//            startActivity(myIntent)
//        }
//
//        findViewById<Button>(R.id.mapsButton).setOnClickListener {
//            val myIntent = Intent(this, MapsActivity::class.java)
//            startActivity(myIntent)
//        }
//    }
//}