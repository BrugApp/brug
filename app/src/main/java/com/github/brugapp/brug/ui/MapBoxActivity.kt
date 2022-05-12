package com.github.brugapp.brug.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.liveData
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.mapbox.LocationPermissionHelper
import com.github.brugapp.brug.databinding.ActivityMapBoxBinding
import com.github.brugapp.brug.databinding.SampleHelloWorldViewBinding
import com.github.brugapp.brug.model.LonLatCoordinates
import com.github.brugapp.brug.model.MyItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.navigation.ui.utils.internal.extensions.getBitmap
import kotlinx.coroutines.Dispatchers
import java.lang.ref.WeakReference

const val EXTRA_DESTINATION_LATITUDE = "com.github.brugapp.brug.DESTINATION_LATITUDE"
const val EXTRA_DESTINATION_LONGITUDE = "com.github.brugapp.brug.DESTINATION_LONGITUDE"
const val EXTRA_NAVIGATION_MODE = "com.github.brugapp.brug.NAVIGATION_MODE"

class MapBoxActivity : AppCompatActivity() {

    private var items: List<MyItem>? = null
    private val lon = -122.084801
    private val lat = 37.422131

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private lateinit var binding: ActivityMapBoxBinding

    //  GETS THE LIST OF ITEMS RELATED TO THE USER
    private fun initItemsList() = liveData(Dispatchers.IO){
        emit(ItemsRepository.getUserItemsFromUID(Firebase.auth.currentUser!!.uid))
    }.observe(this) { itemsList ->
        items = itemsList
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initItemsList()

        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
    }

    private fun onMapReady() {
        binding.mapView.getMapboxMap().setCamera(
            CameraOptions.Builder().center(Point.fromLngLat(lon, lat))
                .build()
        )
        binding.mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS,
        ) {
            addIcons()
        }
    }

    private fun addIcons() {
        var x = 0
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        if (items != null) {
            for (item in items!!) {
                if (x == 0) {
                    item.setLastLocation(lon, lat)
                    x = 1
                }
                @DrawableRes val icon: Int = item.getRelatedIcon()
                val lastLocation = item.getLastLocation()
                if (lastLocation != null) {
                    bitmapFromDrawableRes(this, icon)?.let { it ->
                        val point: Point = Point.fromLngLat(lastLocation.lon, lastLocation.lat)

                        val annotationPlugin = binding.mapView.annotations
                        val pointAnnotationOptions: PointAnnotationOptions =
                            PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage(it)
                                .withIconAnchor(IconAnchor.BOTTOM)
                                .withDraggable(true)
                        val pointAnnotationManager = annotationPlugin.createPointAnnotationManager()
                        val pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)

                        val viewAnnotationManager = binding.mapView.viewAnnotationManager
                        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
                            resId = R.layout.sample_hello_world_view,
                            options = viewAnnotationOptions {
                                geometry(point)
                                associatedFeatureId(pointAnnotation.featureIdentifier)
                                anchor(ViewAnnotationAnchor.BOTTOM)
                                offsetY((pointAnnotation.iconImageBitmap?.height!!).toInt())
                            }
                        )
                        SampleHelloWorldViewBinding.bind(viewAnnotation).apply {
                            setLinkWithNavigation(walkButton, DirectionsCriteria.PROFILE_WALKING, lastLocation)
                            setLinkWithNavigation(driveButton, DirectionsCriteria.PROFILE_DRIVING, lastLocation)
                            itemNameOnMap.text = item.itemName
                        }
                        viewAnnotation.toggleViewVisibility()

                        pointAnnotationManager.addClickListener { clickedAnnotation ->
                            if (pointAnnotation == clickedAnnotation) {
                                viewAnnotation.toggleViewVisibility()
                            }
                            true
                        }
                    }
                }
            }
        }
    }

    private fun setLinkWithNavigation(button: Button, mode: String, lastLocation: LonLatCoordinates) {
        button.setOnClickListener {
            val myIntent = Intent(
                this@MapBoxActivity,
                NavigationToItemActivity::class.java
            ).apply {
                putExtra(EXTRA_DESTINATION_LATITUDE, lastLocation.lat)
                putExtra(EXTRA_DESTINATION_LONGITUDE, lastLocation.lon)
                putExtra(EXTRA_NAVIGATION_MODE, mode)
            }
            startActivity(myIntent)
        }
    }

    private fun View.toggleViewVisibility() {
        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        return sourceDrawable?.getBitmap()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}