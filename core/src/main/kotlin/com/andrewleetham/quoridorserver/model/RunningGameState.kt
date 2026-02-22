package com.andrewleetham.quoridorserver.model

import kotlinx.serialization.Serializable

@Serializable
enum class IntersectType{
    EMPTY, VERTICAL, HORIZONTAL
}

@Serializable
data class RunningGameState(
    override val id: String,
    val players: List<PlayerState>,
    val currentPlayerIndex: Int,
    val placedWalls: List<List<IntersectType>>

) : GameState(){
}
