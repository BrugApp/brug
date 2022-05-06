package com.github.brugapp.brug.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.data.mapbox.LocationPermissionHelper
import com.github.brugapp.brug.databinding.ActivityMapBoxBinding
import com.github.brugapp.brug.databinding.HelloWorldLayoutBinding
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.MyItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.navigation.ui.utils.internal.extensions.getBitmap
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference


var mapView: MapView? = null

class MapBoxActivity : AppCompatActivity() {

    private val items: List<MyItem>? = runBlocking {
        Firebase.auth.uid?.let { ItemsRepository.getUserItemsFromUID(it) }
    }
    private val lon = 18.06
    private val lat = 59.31
    private val name = "iPhone"

    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private val viewAnnotationManager: ViewAnnotationManager? = mapView?.viewAnnotationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_box)
        mapView = findViewById(R.id.mapView)
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
            addAnnotationToMap()
//            addViewAnnotation(Point.fromLngLat(lon, lat))
        }
    }

    private fun addAnnotationToMap() {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        if (items != null) {
            for (item in items) {
                item.setLastLocation(lon, lat)
                @DrawableRes val icon: Int = item.getRelatedIcon()
                val lastLocation = item.getLastLocation()
                if (lastLocation != null) {
                    bitmapFromDrawableRes(this@MapBoxActivity, icon)?.let {
                        val annotationApi = mapView?.annotations
                        val pointAnnotationManager = annotationApi?.createPointAnnotationManager()
                        // Set options for the resulting symbol layer.
                        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                            // Define a geographic coordinate.
                            .withPoint(Point.fromLngLat(lastLocation.lon, lastLocation.lat))
                            // Specify the bitmap you assigned to the point annotation
                            // The bitmap will be added to map style automatically.
                            .withIconImage(it)
                            .withIconSize(1.0)
                        // Add the resulting pointAnnotation to the map.
                        val pointAnnotation = pointAnnotationManager?.create(pointAnnotationOptions)
                        // prepare view annotation and connect it by feature id
                        val viewAnnotation = viewAnnotationManager?.addViewAnnotation(
                            resId = R.layout.hello_world_layout,
                            options = viewAnnotationOptions {
                                geometry(Point.fromLngLat(lastLocation.lon, lastLocation.lat))
                                associatedFeatureId(pointAnnotation?.featureIdentifier)
                                anchor(ViewAnnotationAnchor.BOTTOM)
                                offsetY((pointAnnotation?.iconImageBitmap?.height!!).toInt())
                            }
                        )

                    }
                }
            }
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        return sourceDrawable?.getBitmap()
    }

    private fun addViewAnnotation(point: Point) {
        // Define the view annotation
        val viewAnnotation = viewAnnotationManager?.addViewAnnotation(
            // Specify the layout resource id
            resId = R.layout.sample_hello_world_view,
            // Set any view annotation options
            options = viewAnnotationOptions {
                geometry(point)
            }
        )
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