package com.andrewleetham.quoridorserver.model

data class LoadingGameState(
    override val id: String = "LOADING"

): GameState() {
}
