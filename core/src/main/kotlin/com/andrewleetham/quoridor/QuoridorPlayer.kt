package com.andrewleetham.quoridor

import com.andrewleetham.quoridorserver.model.PlayerColor
import com.andrewleetham.quoridorserver.model.PlayerState



class QuoridorPlayer(start: Pair<Int, Int>, boardSize: Int, var walls: Int, val playerName: String, val color: PlayerColor) {
    var position: Pair<Int, Int> = start
    val winCoord: Int
    val winOnColumn: Boolean

    init{
        if (start.first == 0){
            //from top to bottom
            winCoord = boardSize - 1
            winOnColumn = false

        } else if (start.first == boardSize - 1) {
            //from bottom to top
            winCoord = 0
            winOnColumn = false
        } else if (start.second == 0){
            //from left to right
            winCoord = boardSize - 1
            winOnColumn = true
        } else {
            //from right to left
            winCoord = 0
            winOnColumn = true
        }
    }

    fun checkWin(): Boolean{
        return if(winOnColumn) position.second == winCoord else position.first == winCoord
    }

    companion object {
        fun fromPlayerState(state: PlayerState): QuoridorPlayer {
            return QuoridorPlayer(Pair(0,0), 9, state.walls, state.name, state.color)
        }
    }

}
