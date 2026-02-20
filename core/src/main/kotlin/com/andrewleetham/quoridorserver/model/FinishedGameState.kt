package com.andrewleetham.quoridorserver.model

import kotlinx.serialization.Serializable

@Serializable
class FinishedGameState (
    override val id: String,
    val winner: String
) : GameState() {
    override var phase: GamePhase = GamePhase.FINISHED
}