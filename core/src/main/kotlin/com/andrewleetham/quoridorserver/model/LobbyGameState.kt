package com.andrewleetham.quoridorserver.model

import kotlinx.serialization.Serializable


@Serializable
data class LobbyGameState(
    override val id: String,
    var host: String,
    val players: MutableList<String>
) : GameState() {
}
