package com.example.myapplication.layers

import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISMapImageSublayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.example.myapplication.Constants

object LayersHelper {
    private val statusListeners: MutableSet<LayerLoadStatusChanged> = mutableSetOf()
    private var loadStatus: LoadStatus = LoadStatus.NOT_LOADED
        set(value) {
            field = value
            statusListeners.forEach { it.onGeoServiceLoadStatusChanged(value) }
        }
    private lateinit var map: ArcGISMap
    val serviceLayers: MutableMap<Long, ServiceLayer> = mutableMapOf()

    private val rootLayer: ArcGISMapImageLayer by lazy {
        ArcGISMapImageLayer(Constants.baseUrl).apply {
            isVisible = false
//            minScale = Constants.defaultMinScale
//            maxScale = Constants.defaultMaxScale
        }
    }

    fun firstLoad(_map: ArcGISMap) {
        map = _map
        loadStatus = LoadStatus.LOADING
        map.operationalLayers.add(rootLayer)
        rootLayer.addDoneLoadingListener {
            serviceLayers.clear()
            loadStatus = if (rootLayer.loadStatus == LoadStatus.LOADED) {
                initSubLayers()
                rootLayer.isVisible = true
                LoadStatus.LOADED
            } else {
                LoadStatus.FAILED_TO_LOAD
            }
        }
    }

    private fun initSubLayers() {
        rootLayer.sublayers.forEach { runSubLayerTree(it as ArcGISMapImageSublayer) }
    }

    private fun runSubLayerTree(subLayer: ArcGISMapImageSublayer) {
        val isRootLayer = Constants.rootLayerIds.contains(subLayer.id)
        val isFeatureLayer = subLayer.id in Constants.featureLayerIds
        subLayer.apply {
            isVisible = isRootLayer
            if (isFeatureLayer) {
                minScale = Constants.defaultMinScale
                maxScale = Constants.defaultMaxScale
            }
        }
        if (isRootLayer) {
            subLayer.sublayers.forEach { runSubLayerTree(it as ArcGISMapImageSublayer) }
        } else if (isFeatureLayer) {
            serviceLayers[subLayer.id] = ServiceLayer(subLayer)
        }
    }

    fun showLayer(layerId: Long) {
        serviceLayers[layerId]?.active = true
    }

    fun hideLayer(layerId: Long) {
        serviceLayers[layerId]?.active = false
    }

    fun addStatusChangedListener(listener: LayerLoadStatusChanged) {
        statusListeners.add(listener)
    }

    fun removeStatusChangedListener(listener: LayerLoadStatusChanged) {
        statusListeners.remove(listener)
    }
}
