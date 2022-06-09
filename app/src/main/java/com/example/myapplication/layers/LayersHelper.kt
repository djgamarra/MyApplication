package com.example.myapplication.layers

import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISMapImageSublayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.example.myapplication.Constants

object LayersHelper {
    private val statusListeners: MutableMap<String, LayerLoadStatusChanged> = mutableMapOf()
    private var loadStatus: LoadStatus = LoadStatus.NOT_LOADED
        set(value) {
            field = value
            statusListeners.forEach { it.value.onGeoServiceLoadStatusChanged(value) }
        }
    private lateinit var map: ArcGISMap
    val serviceLayers: MutableMap<Long, ServiceLayer> = mutableMapOf()

    private val rootLayer: ArcGISMapImageLayer by lazy {
        ArcGISMapImageLayer(Constants.baseUrl).apply {
            isVisible = false
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
        subLayer.apply {
            isVisible = isRootLayer
        }
        if (isRootLayer) {
            subLayer.sublayers.forEach { runSubLayerTree(it as ArcGISMapImageSublayer) }
        } else if (subLayer.id in Constants.featureLayerIds) {
            serviceLayers[subLayer.id] = ServiceLayer(subLayer)
        }
    }

    fun showLayer(layerId: Long) {
        serviceLayers[layerId]?.active = true
    }

    fun hideLayer(layerId: Long) {
        serviceLayers[layerId]?.active = false
    }

    fun addStatusChangedListener(tag: String, listener: LayerLoadStatusChanged) {
        statusListeners[tag] = listener
    }

    fun removeStatusChangedListener(tag: String) {
        statusListeners.remove(tag)
    }
}
