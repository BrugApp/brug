//package com.github.brugapp.brug.ui
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.github.brugapp.brug.databinding.MapboxActivityNavigationViewBinding
//import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
//import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
//import com.mapbox.navigation.dropin.component.tripsession.TripSessionStarterAction
//import com.mapbox.navigation.dropin.component.tripsession.TripSessionStarterViewModel
//
//@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
//class NavigationViewActivity : AppCompatActivity() {
//
//    private lateinit var binding: MapboxActivityNavigationViewBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = MapboxActivityNavigationViewBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // This allows to simulate your location
//        val tripSessionStarterViewModel = MapboxNavigationApp.getObserver(
//            TripSessionStarterViewModel::class
//        )
//        tripSessionStarterViewModel.invoke(
//            TripSessionStarterAction.EnableReplayTripSession
//        )
//    }
//}