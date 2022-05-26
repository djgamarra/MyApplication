package com.example.myapplication.layers

import com.esri.arcgisruntime.loadable.LoadStatus

interface LayerLoadStatusChanged {
    fun onGeoServiceLoadStatusChanged(status: LoadStatus)
    fun onLayerLoadStatusChanged(status: LoadStatus)
}
