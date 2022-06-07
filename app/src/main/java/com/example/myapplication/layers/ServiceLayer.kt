package com.example.myapplication.layers

import com.esri.arcgisruntime.layers.ArcGISMapImageSublayer

class ServiceLayer(
    private val layer: ArcGISMapImageSublayer
) {
    val id: Long
        get() = layer.id
    val name: String
        get() = layer.name
    var active: Boolean = false
        set(value) {
            field = value
            layer.isVisible = value
        }
}
