package com.andrewleetham.quoridorserver.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val name: String,
    val position: Pair<Int, Int>,
    val walls: Int
)
