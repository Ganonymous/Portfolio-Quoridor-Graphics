package com.andrewleetham.quoridorserver.model

import kotlinx.serialization.Serializable


@Serializable
data class LobbyGameState(
    override val id: String,
    val host: String,
    val players: MutableList<String>
) : GameState() {
    override var phase: GamePhase = GamePhase.LOBBY
}
