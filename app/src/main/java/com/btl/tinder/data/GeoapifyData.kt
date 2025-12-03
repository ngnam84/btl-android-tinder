package com.btl.tinder.data

import kotlinx.serialization.Serializable

@Serializable
data class GeoapifyResponse(
    val features: List<Feature> = emptyList()
)

@Serializable
data class Feature(
    val properties: Properties
)

@Serializable
data class Properties(
    val lat: Double,
    val lon: Double
)
