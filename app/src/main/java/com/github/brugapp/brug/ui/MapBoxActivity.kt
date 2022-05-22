package com.github.brugapp.brug.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.github.brugapp.brug.ITEMS_TEST_LIST_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.mapbox.LocationPermissionHelper
import com.github.brugapp.brug.databinding.ActivityMapBoxBinding
import com.github.brugapp.brug.databinding.OnItemMapClickViewBinding
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.navigation.ui.utils.internal.extensions.getBitmap
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import javax.inject.Inject

const val EXTRA_DESTINATION_LATITUDE = "com.github.brugapp.brug.DESTINATION_LATITUDE"
const val EXTRA_DESTINATION_LONGITUDE = "com.github.brugapp.brug.DESTINATION_LONGITUDE"
const val EXTRA_MAP_ZOOM = "com.github.brugapp.brug.MAP_ZOOM"
const val EXTRA_NAVIGATION_MODE = "com.github.brugapp.brug.NAVIGATION_MODE"

@AndroidEntryPoint
class MapBoxActivity : AppCompatActivity() {
    private var lon = -122.07131270212334
    private var lat = 37.411793498806624
    private var zoom = 9.0

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private lateinit var binding: ActivityMapBoxBinding

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras != null) {
            (intent.extras!!.get(EXTRA_DESTINATION_LATITUDE) as Double?)?.apply {
                lat = this
            }
            (intent.extras!!.get(EXTRA_DESTINATION_LONGITUDE) as Double?)?.apply {
                lon = this
            }
            (intent.extras!!.get(EXTRA_MAP_ZOOM) as Double?)?.apply {
                zoom = this
            }
        }

        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
    }

    private fun onMapReady() {
        binding.mapView.getMapboxMap().setCamera(
            CameraOptions.Builder().center(Point.fromLngLat(lon, lat))
                .zoom( zoom)
                .build()
        )
        binding.mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS,
        ) {
            addIcons()
        }
    }

    private fun addIcons() {
        val itemsTestList =
            if(intent.extras != null && intent.extras!!.containsKey(ITEMS_TEST_LIST_KEY)){
                intent.extras!!.get(ITEMS_TEST_LIST_KEY) as MutableList<Item>
            } else null

        if(itemsTestList == null){
            // First it fetches the items from Firebase, so that the list of items is fresh
            ItemsRepository.getRealtimeUserItemsFromUID(firebaseAuth.uid!!, this, firestore)
        } else {
            BrugDataCache.setItemsInCache(itemsTestList)
        }

        val annotationPlugin = binding.mapView.annotations
        val pointAnnotationManager = annotationPlugin.createPointAnnotationManager()
        val viewAnnotationManager = binding.mapView.viewAnnotationManager

        // Create an instance of the Annotation API and get the PointAnnotationManager.
        BrugDataCache.getCachedItems().observe(this) { items ->
            // First we flush the contents of the map
            pointAnnotationManager.deleteAll()
            viewAnnotationManager.removeAllViewAnnotations()

            for (item in items) {
                @DrawableRes val icon: Int = item.getRelatedIcon()
                val lastLocation = item.getLastLocation()
                if (lastLocation != null) {
                    bitmapFromDrawableRes(this, icon)?.let { it ->
                        val point: Point = Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude())

                        val pointAnnotationOptions: PointAnnotationOptions =
                            PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage(it)
                                .withIconSize(2.0)
                                .withIconAnchor(IconAnchor.CENTER)
                                .withDraggable(true)
                        val pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)

                        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
                            resId = R.layout.on_item_map_click_view,
                            options = viewAnnotationOptions {
                                geometry(point)
                                associatedFeatureId(pointAnnotation.featureIdentifier)
                                anchor(ViewAnnotationAnchor.BOTTOM)
                                offsetY((pointAnnotation.iconImageBitmap?.height!!).toInt())
                            }
                        )
                        OnItemMapClickViewBinding.bind(viewAnnotation).apply {
                            setLinkWithNavigation(walkButton, DirectionsCriteria.PROFILE_WALKING, lastLocation)
                            setLinkWithNavigation(driveButton, DirectionsCriteria.PROFILE_DRIVING, lastLocation)
                            itemNameOnMap.text = item.itemName
                        }

                        pointAnnotationManager.addClickListener{ clickedAnnotation ->
                            Log.e("ANNOTATION CLICKED", clickedAnnotation.point.toString())
                            val pointID = clickedAnnotation.featureIdentifier
                            viewAnnotationManager.getViewAnnotationByFeatureId(
                                pointID
                            )!!.toggleViewVisibility()

                            true
                        }
                    }
                }
            }
        }
    }

    private fun setLinkWithNavigation(button: Button, mode: String, lastLocation: LocationService) {
        button.setOnClickListener {
            val myIntent = Intent(
                this@MapBoxActivity,
                NavigationToItemActivity::class.java
            ).apply {
                putExtra(EXTRA_DESTINATION_LATITUDE, lastLocation.getLatitude())
                putExtra(EXTRA_DESTINATION_LONGITUDE, lastLocation.getLongitude())
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