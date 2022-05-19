package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val apiKey =
        "AAPK49259e1bedf948118e888db28ac88eccpPKIrH6eeI2Yv1kjlYAzEydv6aDzMSZrLkM3yhtfREyO8Q-DJCCjZRm4aA5XQRje"
    private val baseUrl =
        "https://gis.cl.innovacion-gascaribe.com/arcgis/rest/services/PETIGASCARIBE/REDESGASCARIBE/MapServer/"

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val map: ArcGISMap by lazy {
        ArcGISMap(BasemapStyle.ARCGIS_STREETS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        setApiKeyForApp()
        setupMap()
    }

    private fun setApiKeyForApp() {
        ArcGISRuntimeEnvironment.setApiKey(apiKey)
    }

    private fun setupMap() {
        mapView.map = map
        loadLayer(41)
        loadLayer(40)
        loadLayer(44)
//        loadLayer(25)
//        map.operationalLayers.add(FeatureLayer(ServiceFeatureTable(baseUrl + "41")))
//        map.operationalLayers.add(FeatureLayer(ServiceFeatureTable(baseUrl + "40")))
//        map.operationalLayers.add(FeatureLayer(ServiceFeatureTable(baseUrl + "44")))
//        val layer = FeatureLayer(ServiceFeatureTable(baseUrl + "25")).apply {
//            renderer = SimpleRenderer(SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x0, null))
//        }
//        map.operationalLayers.add(layer)
    }

    private fun loadLayer(layerId: Long) {
//        val serviceTable = ServiceFeatureTable(baseUrl + layerId.toString())
//        val layer = FeatureLayer(serviceTable).apply {
//            renderer = UniqueValueRenderer().apply {
//                defaultSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0x0, 20f)
//            }
//        }
//        val serviceTable = ServiceFeatureTable(baseUrl + layerId.toString())
//        val featureCollection = FeatureCollection()
        ServiceGeodatabase(baseUrl).apply {
            loadAsync()
            addDoneLoadingListener {
                val layer = FeatureLayer(getTable(layerId)).apply {
                    renderer = SimpleRenderer()
                }
                map.operationalLayers.add(layer)
            }
        }
//        map.operationalLayers.add(layer)
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }
}