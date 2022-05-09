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
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.mapbox.LocationPermissionHelper
import com.github.brugapp.brug.databinding.ActivityMapBoxBinding
import com.github.brugapp.brug.databinding.SampleHelloWorldViewBinding
import com.github.brugapp.brug.model.MyItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.navigation.ui.utils.internal.extensions.getBitmap
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference

var mapView: MapView? = null

const val EXTRA_DESTINATION_LATITUDE = "com.github.brugapp.brug.DESTINATION_LATITUDE"
const val EXTRA_DESTINATION_LONGITUDE = "com.github.brugapp.brug.DESTINATION_LONGITUDE"

class MapBoxActivity : AppCompatActivity() {

    private val items: List<MyItem>? = runBlocking {
        Firebase.auth.uid?.let { ItemsRepository.getUserItemsFromUID(it) }
    }
    private val lon = 18.06
    private val lat = 59.31
    private val name = "iPhone"

    private lateinit var locationPermissionHelper: LocationPermissionHelper
//    private val viewAnnotationManager: ViewAnnotationManager? = mapView?.viewAnnotationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMapBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setContentView(R.layout.activity_map_box)

        mapView = binding.mapView
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }


//        val item = MyItem("iPhone", ItemType.Phone.ordinal, "", true)
//        item.setLastLocation(lon, lat)
//        items.add(item)
    }

    private fun onMapReady() {
        mapView?.getMapboxMap()?.setCamera(
            CameraOptions.Builder().center(Point.fromLngLat(lon, lat))
                .build()
        )
        mapView?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS,
        ) {
            addIcons()
//            addAnnotationToMap()
//            addViewAnnotation(Point.fromLngLat(lon, lat))
        }
    }

    private fun addIcons() {
        var x = 0
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        if (items != null) {
            for (item in items) {
                if (x == 0){
                    item.setLastLocation(lon, lat)
                    x = 1
                }
                @DrawableRes val icon: Int = item.getRelatedIcon()
                val lastLocation = item.getLastLocation()
                if (lastLocation != null) {
                    bitmapFromDrawableRes(this, icon)?.let { it ->
                        val point: Point = Point.fromLngLat(lastLocation.lon, lastLocation.lat)

                        val annotationPlugin = mapView?.annotations
                        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage(it)
                            .withIconAnchor(IconAnchor.BOTTOM)
                            .withDraggable(true)
                        val pointAnnotationManager = annotationPlugin?.createPointAnnotationManager()
                        val pointAnnotation = pointAnnotationManager?.create(pointAnnotationOptions)

                        val viewAnnotationManager = mapView?.viewAnnotationManager
                        val viewAnnotation = viewAnnotationManager?.addViewAnnotation(
                            resId = R.layout.sample_hello_world_view,
                            options = viewAnnotationOptions {
                                geometry(point)
                                associatedFeatureId(pointAnnotation?.featureIdentifier)
                                anchor(ViewAnnotationAnchor.BOTTOM)
                                offsetY((pointAnnotation?.iconImageBitmap?.height!!).toInt())
                            }
                        )
                        SampleHelloWorldViewBinding.bind(viewAnnotation!!).apply {
                            selectButton.setOnClickListener {
                                val myIntent = Intent(this@MapBoxActivity, NavigationToItemActivity::class.java).apply {
                                    putExtra(EXTRA_DESTINATION_LATITUDE, lastLocation.lat)
                                    putExtra(EXTRA_DESTINATION_LONGITUDE, lastLocation.lon)
                                }
                                startActivity(myIntent)

//                                    b ->
//                                val button = b as Button
//                                val isSelected = button.text.toString().equals("SELECT", true)
//                                val pxDelta = if (isSelected) SELECTED_ADD_COEF_PX else -SELECTED_ADD_COEF_PX
//                                button.text = if (isSelected) "DESELECT" else "SELECT"
//                                viewAnnotationManager.updateViewAnnotation(
//                                    viewAnnotation,
//                                    viewAnnotationOptions {
//                                        selected(isSelected)
//                                    }
//                                )
//                                (button.layoutParams as ViewGroup.MarginLayoutParams).apply {
//                                    bottomMargin += pxDelta
//                                    rightMargin += pxDelta
//                                    leftMargin += pxDelta
//                                }
//                                button.requestLayout()
                            }
                        }

//                        val annotationApi = mapView.annotations
//                        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
//                        // Set options for the resulting symbol layer.
//                        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
//                            // Define a geographic coordinate.
//                            .withPoint(Point.fromLngLat(lastLocation.lon, lastLocation.lat))
//                            // Specify the bitmap you assigned to the point annotation
//                            // The bitmap will be added to map style automatically.
//                            .withIconImage(it)
//                            .withIconSize(1.0)
//                        // Add the resulting pointAnnotation to the map.
//                        val pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
//                        // prepare view annotation and connect it by feature id
//                        val viewAnnotation = viewAnnotationManager?.addViewAnnotation(
//                            resId = R.layout.hello_world_layout,
//                            options = viewAnnotationOptions {
//                                geometry(Point.fromLngLat(lastLocation.lon, lastLocation.lat))
//                                associatedFeatureId(pointAnnotation?.featureIdentifier)
//                                anchor(ViewAnnotationAnchor.BOTTOM)
//                                offsetY((pointAnnotation?.iconImageBitmap?.height!!).toInt())
//                            }
//                        )

                        pointAnnotationManager?.addClickListener { clickedAnnotation ->
                            if (pointAnnotation == clickedAnnotation) {
                                viewAnnotation?.toggleViewVisibility()
                            }
                            true
                        }


//                        // update view annotation geometry if dragging the marker
//                        pointAnnotationManager.addDragListener(object : OnPointAnnotationDragListener {
//                            override fun onAnnotationDragStarted(annotation: Annotation<*>) {
//                            }
//
//                            override fun onAnnotationDrag(annotation: Annotation<*>) {
//                                if (annotation == pointAnnotation) {
//                                    binding.mapView.viewAnnotationManager.updateViewAnnotation(
//                                        viewAnnotation,
//                                        viewAnnotationOptions {
//                                            geometry(pointAnnotation.geometry)
//                                        }
//                                    )
//                                    ItemCalloutViewBinding.bind(viewAnnotation).apply {
//                                        textNativeView.text = "lat=%.2f\nlon=%.2f".format(
//                                            pointAnnotation.geometry.latitude(),
//                                            pointAnnotation.geometry.longitude()
//                                        )
//                                    }
//                                }
//                            }
//
//                            override fun onAnnotationDragFinished(annotation: Annotation<*>) {
//                            }
//                        })


                    }
                }
            }
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