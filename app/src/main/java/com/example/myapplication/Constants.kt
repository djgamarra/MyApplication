package com.example.myapplication

object Constants {
    const val apiKey =
        "AAPK49259e1bedf948118e888db28ac88eccpPKIrH6eeI2Yv1kjlYAzEydv6aDzMSZrLkM3yhtfREyO8Q-DJCCjZRm4aA5XQRje"
    const val baseUrl =
        "https://gis.cl.innovacion-gascaribe.com/arcgis/rest/services/PETIGASCARIBE/REDESGASCARIBE/MapServer/"
    const val defaultMinScale = 700000.0
    const val defaultMaxScale = 0.0

    // The only visible layers must be:
    // -- 2: Red de Gas
    //  |-- 24: En Servicio
    val rootLayerIds = listOf(2L, 24L)

    //    |-- 25 to 45: Feature layers
    val featureLayerIds = 25L..45L
}
