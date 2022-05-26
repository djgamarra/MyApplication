package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.loader.LayerLoadStatusChanged
import com.example.myapplication.loader.LayerLoader
import com.example.myapplication.loader.LayerLoaderStatus

class MainActivity : AppCompatActivity(), LocationDisplay.LocationChangedListener,
    LayerLoadStatusChanged {

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val locationDisplay: LocationDisplay by lazy { mapView.locationDisplay }

    private val map: ArcGISMap by lazy {
        ArcGISMap(BasemapStyle.ARCGIS_STREETS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        supportActionBar?.hide()
        setApiKeyForApp()
        setupMap()
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    private fun setApiKeyForApp() {
        ArcGISRuntimeEnvironment.setApiKey(Constants.apiKey)
    }

    private fun setupMap() {
        LayerLoader.apply {
            addStatusChangedListener(this@MainActivity)
            firstLoad()
        }
        mapView.map = map
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationDisplay.startAsync()
            locationDisplay.addLocationChangedListener(this)
        }
    }

    override fun onPause() {
        LayerLoader.removeStatusChangedListener(this)
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
        LayerLoader.addStatusChangedListener(this)
    }

    override fun onDestroy() {
        LayerLoader.removeStatusChangedListener(this)
        mapView.dispose()
        super.onDestroy()
    }

    private fun moveToMyPosition() {
        val currentPosition = locationDisplay.location.position
        mapView.setViewpointAsync(Viewpoint(currentPosition, 200000.0))
        locationDisplay.removeLocationChangedListener(this)
    }

    override fun onLocationChanged(locationEvent: LocationDisplay.LocationChangedEvent?) {
        if (locationEvent == null) {
            return
        }
        moveToMyPosition()
    }

    override fun onLoaderStatusChanged(status: LayerLoaderStatus) {
        if (status == LayerLoaderStatus.LOADED) {
            LayerLoader.apply {
                loadLayer(41, map)
                loadLayer(40, map)
                loadLayer(44, map)
                loadLayer(36, map)
                loadLayer(25, map)
                loadLayer(26, map)
                loadLayer(31, map)
            }
        }
    }
}
