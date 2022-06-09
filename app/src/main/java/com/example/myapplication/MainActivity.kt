package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.TextSymbol
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.layers.LayerLoadStatusChanged
import com.example.myapplication.layers.LayersHelper
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), LocationDisplay.LocationChangedListener,
    LayerLoadStatusChanged, NavigationView.OnNavigationItemSelectedListener {
    private val tag = ".MainActivity"

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val locationDisplay: LocationDisplay by lazy { mapView.locationDisplay }

    private val baseMap: ArcGISMap by lazy {
        ArcGISMap(BasemapStyle.ARCGIS_COMMUNITY).apply {
            minScale = Constants.defaultMinScale
            maxScale = Constants.defaultMaxScale
        }
    }

    private val mapOverlay: GraphicsOverlay by lazy {
        GraphicsOverlay()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setApiKeyForApp()
        setupMap()
        setupInitialLocation(intent)
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
            addStatusChangedListener(tag, this@MainActivity)
            firstLoad(baseMap)
        }
        mapView.map = baseMap
        mapView.graphicsOverlays.add(mapOverlay)
    }

    private fun setupInitialLocation(intent: Intent) {
        val extras = intent.extras
        if (extras != null) {
            val lat = extras.getString("latitude", "undefined").toDoubleOrNull()
            val lng = extras.getString("longitude", "undefined").toDoubleOrNull()
            val orderType = extras.getString("orderType", "undefined")
            val address = extras.getString("address", "undefined")
            if (lat != null && lng != null && orderType != "undefined" && address != "undefined") {
                val initialViewpoint = Viewpoint(lat, lng, Constants.defaultOrderZoom)
                val pinSymbol =
                    PictureMarkerSymbol(Constants.pinUrl).apply {
                        height = Constants.pinSize
                        width = Constants.pinSize
                        offsetY = Constants.pinOffset
                    }
                val orderTypeSymbol = TextSymbol(
                    12F,
                    orderType,
                    Color.BLACK,
                    TextSymbol.HorizontalAlignment.CENTER,
                    TextSymbol.VerticalAlignment.BOTTOM
                ).apply {
                    offsetY = Constants.pinOffset + 40
                    backgroundColor = Color.WHITE
                    fontWeight = TextSymbol.FontWeight.BOLD
                }
                val addressSymbol = TextSymbol(
                    11F,
                    address,
                    Color.BLACK,
                    TextSymbol.HorizontalAlignment.CENTER,
                    TextSymbol.VerticalAlignment.BOTTOM
                ).apply {
                    offsetY = Constants.pinOffset + 22
                    backgroundColor = Color.WHITE
                }
                mapOverlay.graphics.apply {
                    clear()
                    add(Graphic(initialViewpoint.targetGeometry, pinSymbol))
                    add(Graphic(initialViewpoint.targetGeometry, orderTypeSymbol))
                    add(Graphic(initialViewpoint.targetGeometry, addressSymbol))
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                mapView.setViewpointAsync(initialViewpoint)
            }
        }
    }

    private fun setupPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    private fun loadLayers() {
        navigationView.menu.apply {
            clear()
            LayersHelper.serviceLayers.values.forEach {
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            setupInitialLocation(intent)
        }
    }

    override fun onPause() {
        LayersHelper.removeStatusChangedListener(tag)
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
        LayersHelper.addStatusChangedListener(tag, this)
    }

    override fun onDestroy() {
        LayersHelper.removeStatusChangedListener(tag)
        mapView.dispose()
        super.onDestroy()
    }

    override fun onLocationChanged(locationEvent: LocationDisplay.LocationChangedEvent?) {
        if (locationEvent == null) {
            return
        }
        val currentPosition = locationEvent.location.position
        mapView.setViewpointAsync(Viewpoint(currentPosition, Constants.defaultZoom))
        locationDisplay.removeLocationChangedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val shouldActivate = !item.isChecked
        item.isChecked = shouldActivate
        if (shouldActivate) {
            LayersHelper.showLayer(item.itemId.toLong())
        } else {
            LayersHelper.hideLayer(item.itemId.toLong())
        }
        return true
    }

    override fun onGeoServiceLoadStatusChanged(status: LoadStatus) {
        when (status) {
            LoadStatus.LOADING ->
                Toast.makeText(this, "Cargando el servicio de mapas...", Toast.LENGTH_SHORT).show()
            LoadStatus.LOADED -> {
                loadLayers()
                Toast.makeText(this, "Servicio cargado correctamente", Toast.LENGTH_SHORT).show()
            }
            else ->
                Toast.makeText(this, "Error al cargar las redes de gases", Toast.LENGTH_LONG).show()
        }
    }

    override fun onLayerLoadStatusChanged(status: LoadStatus) {
    }
}
