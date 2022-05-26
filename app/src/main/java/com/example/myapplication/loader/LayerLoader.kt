package com.example.myapplication.loader

import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.example.myapplication.Constants

object LayerLoader {
    private val statusListeners: MutableSet<LayerLoadStatusChanged> = mutableSetOf()
    private var loadStatus: LayerLoaderStatus = LayerLoaderStatus.UNLOADED
        set(value) {
            field = value
            statusListeners.forEach { it.onLoaderStatusChanged(value) }
        }

    private val geoService: ServiceGeodatabase by lazy {
        ServiceGeodatabase(Constants.baseUrl)
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
            minScale = Constants.defaultMinScale
            maxScale = Constants.defaultMaxScale
        })
    }

    fun addStatusChangedListener(listener: LayerLoadStatusChanged) {
        statusListeners.add(listener)
    }

    fun removeStatusChangedListener(listener: LayerLoadStatusChanged) {
        statusListeners.remove(listener)
    }
}
