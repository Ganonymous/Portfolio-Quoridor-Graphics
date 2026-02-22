package com.andrewleetham.quoridorserver.model

import kotlinx.serialization.Serializable


@Serializable
sealed class GameState(

){
    abstract val id: String
}
