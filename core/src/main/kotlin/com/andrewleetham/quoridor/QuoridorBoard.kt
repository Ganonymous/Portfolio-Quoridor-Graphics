package com.andrewleetham.quoridor

class QuoridorBoard(val boardSize: Int){
    //Spaces contain the digit of the player in them, or a space if unoccupied
    var spaces: Array<Array<Char>> = Array(boardSize) {Array(boardSize) {' '} }
    //Walls are two spaces wide, and cannot overlap, so the intersections are tracked instead.
    enum class IntersectType {EMPTY, VERTICAL, HORIZONTAL}
    var wallIntersects: Array<Array<IntersectType>> = Array(boardSize - 1) { Array(boardSize - 1) {IntersectType.EMPTY} }

    fun getPieceAt(row: Int, col: Int): Int? {
        if(spaces[row][col] != ' ') return spaces[row][col].toString().toInt()
        else return null
    }
}
