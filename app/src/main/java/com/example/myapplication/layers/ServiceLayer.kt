package com.example.myapplication.layers

import com.esri.arcgisruntime.arcgisservices.IdInfo
import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.example.myapplication.Constants

class ServiceLayer(
    layer: IdInfo,
    private val geoService: ServiceGeodatabase,
    private val map: ArcGISMap
) {
    val id = layer.id
    val name = layer.name ?: ""
    private var layer: FeatureLayer? = null
    var active: Boolean = false
        set(value) {
            field = value
            if (value) {
                if (layer == null) {
                    layer = FeatureLayer(geoService.getTable(id)).apply {
                        minScale = Constants.defaultMinScale
                        maxScale = Constants.defaultMaxScale
                    }
                    map.operationalLayers.add(layer)
                } else {
                    layer!!.isVisible = true
                }
            } else {
                layer!!.isVisible = false
            }
        }
}
