package com.andrewleetham.quoridorserver.model

import kotlinx.serialization.Serializable

@Serializable
enum class GamePhase {
    LOBBY,
    RUNNING,
    FINISHED
}

@Serializable
sealed class GameState(

){
    abstract val id: String
    abstract val phase: GamePhase
}
