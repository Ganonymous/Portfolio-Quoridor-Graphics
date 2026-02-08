package com.andrewleetham.quoridor

class QuoridorPlayer(start: Pair<Int, Int>, boardSize: Int, var walls: Int, val playerName: String, val displayChar: Char) {
    var position: Pair<Int, Int>
    val winCoord: Int
    val winOnColumn: Boolean

    init{
        position = start
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

    fun CheckWin(): Boolean{
        return if(winOnColumn) position.second == winCoord else position.first == winCoord
    }

    fun Display() {
        println("$playerName [$displayChar], Walls Remaining: $walls")
    }

}
