package com.example.myapplication.loader

import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap

class LayerLoader {
    private val statusListeners: MutableSet<LayerLoadStatusChanged> = mutableSetOf()
    private var loadStatus: LayerLoaderStatus = LayerLoaderStatus.UNLOADED
        set(value) {
            field = value
            statusListeners.forEach { it.onLoaderStatusChanged(value) }
        }

    private val geoService: ServiceGeodatabase by lazy {
        ServiceGeodatabase(baseUrl)
    }

    fun firstLoad() {
        loadStatus = LayerLoaderStatus.LOADING
        geoService.loadAsync()
        geoService.addDoneLoadingListener {
            loadStatus = if (geoService.loadStatus == LoadStatus.LOADED) {
                LayerLoaderStatus.LOADED
            } else {
                LayerLoaderStatus.ERROR
            }
        }
    }

    fun loadLayer(layerId: Long, map: ArcGISMap) {
        map.operationalLayers.add(FeatureLayer(geoService.getTable(layerId)).apply {
            minScale = defaultMinScale
            maxScale = defaultMaxScale
        })
    }

    fun addStatusChangedListener(listener: LayerLoadStatusChanged) {
        statusListeners.add(listener)
    }

    fun removeStatusChangedListener(listener: LayerLoadStatusChanged) {
        statusListeners.remove(listener)
    }

    companion object {
        private const val baseUrl =
            "https://gis.cl.innovacion-gascaribe.com/arcgis/rest/services/PETIGASCARIBE/REDESGASCARIBE/MapServer/"
        private const val defaultMinScale = 2000000.0
        private const val defaultMaxScale = 0.0
        val instance: LayerLoader by lazy {
            LayerLoader()
        }
    }
}
