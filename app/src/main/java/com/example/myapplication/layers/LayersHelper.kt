package com.example.myapplication.layers

import com.esri.arcgisruntime.data.ServiceGeodatabase
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
    val serviceLayers: MutableList<ServiceLayer> = mutableListOf()

    private val geoService: ServiceGeodatabase by lazy {
        ServiceGeodatabase(Constants.baseUrl)
    }

    fun firstLoad(_map: ArcGISMap) {
        map = _map
        loadStatus = LoadStatus.LOADING
        geoService.loadAsync()
        geoService.addDoneLoadingListener {
            loadStatus = if (geoService.loadStatus == LoadStatus.LOADED) {
                serviceLayers.clear()
                serviceLayers.addAll(geoService
                    .serviceInfo
                    .layerInfos
                    .filter { it.id in Constants.enabledLayerIds }
                    .map { ServiceLayer(it, geoService, map) })
                LoadStatus.LOADED
            } else {
                LoadStatus.FAILED_TO_LOAD
            }
        }
    }

    fun loadLayer(layerId: Long) {
        serviceLayers.find { it.id == layerId }?.active = true
    }

    fun removeLayer(layerId: Long) {
        serviceLayers.find { it.id == layerId }?.active = false
    }

    fun addStatusChangedListener(listener: LayerLoadStatusChanged) {
        statusListeners.add(listener)
    }

    fun removeStatusChangedListener(listener: LayerLoadStatusChanged) {
        statusListeners.remove(listener)
    }
}
