package com.andrewleetham.quoridorserver.model

import com.badlogic.gdx.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val name: String,
    val position: Pair<Int, Int>,
    val walls: Int,
    val color: PlayerColor
)

@Serializable
enum class PlayerColor {RED, BLUE, GREEN, YELLOW;
    fun toGdxColor(): Color = when(this) {
        RED -> Color.RED
        BLUE -> Color.BLUE
        GREEN -> Color.GREEN
        YELLOW -> Color.YELLOW
    }
    companion object {
        fun fromGDXColor(color: Color): PlayerColor = when(color) {
            Color.RED -> RED
            Color.BLUE -> BLUE
            Color.GREEN -> GREEN
            else -> YELLOW
        }
    }
}
