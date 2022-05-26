package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.layers.LayerLoadStatusChanged
import com.example.myapplication.layers.LayersHelper
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), LocationDisplay.LocationChangedListener,
    LayerLoadStatusChanged, NavigationView.OnNavigationItemSelectedListener {

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val navigationView: NavigationView by lazy {
        activityMainBinding.navigationView
    }

    private val drawerLayout: DrawerLayout by lazy {
        activityMainBinding.drawerLayout
    }

    private val drawerToggle: ActionBarDrawerToggle by lazy {
        ActionBarDrawerToggle(this, drawerLayout, R.string.menu_open, R.string.menu_close)
    }

    private val locationDisplay: LocationDisplay by lazy { mapView.locationDisplay }

    private val baseMap: ArcGISMap by lazy {
        ArcGISMap(BasemapStyle.ARCGIS_STREETS).apply { this.minScale = Constants.defaultMinScale }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setApiKeyForApp()
        setupMap()
        setupPermissions()
    }

    private fun setupView() {
        setContentView(activityMainBinding.root)
        supportActionBar?.hide()
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun setApiKeyForApp() {
        ArcGISRuntimeEnvironment.setApiKey(Constants.apiKey)
    }

    private fun setupMap() {
        LayersHelper.apply {
            addStatusChangedListener(this@MainActivity)
            firstLoad(baseMap)
        }
        mapView.map = baseMap
    }

    private fun setupPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    private fun moveToMyPosition() {
        val currentPosition = locationDisplay.location.position
        mapView.setViewpointAsync(Viewpoint(currentPosition, 200000.0))
        locationDisplay.removeLocationChangedListener(this)
    }

    private fun loadLayers() {
        navigationView.menu.apply {
            clear()
            LayersHelper.serviceLayers.forEach {
                add(0, it.id.toInt(), 0, it.name)
            }
        }
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
        LayersHelper.removeStatusChangedListener(this)
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
        LayersHelper.addStatusChangedListener(this)
    }

    override fun onDestroy() {
        LayersHelper.removeStatusChangedListener(this)
        mapView.dispose()
        super.onDestroy()
    }

    override fun onLocationChanged(locationEvent: LocationDisplay.LocationChangedEvent?) {
        if (locationEvent == null) {
            return
        }
        moveToMyPosition()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        LayersHelper.serviceLayers.find { it.id.toInt() == item.itemId }?.let { serviceLayer ->
            serviceLayer.active = !serviceLayer.active
            item.isChecked = serviceLayer.active
            if (serviceLayer.active) {
                LayersHelper.loadLayer(serviceLayer.id)
            } else {
                LayersHelper.removeLayer(serviceLayer.id)
            }
        }
        return true
    }

    override fun onGeoServiceLoadStatusChanged(status: LoadStatus) {
        if (status == LoadStatus.LOADED) {
            loadLayers()
        }
    }

    override fun onLayerLoadStatusChanged(status: LoadStatus) {

    }
}
