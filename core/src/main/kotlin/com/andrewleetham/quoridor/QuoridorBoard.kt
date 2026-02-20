package com.andrewleetham.quoridor

class QuoridorBoard(val boardSize: Int){
    //Spaces contain the digit of the player in them, or a space if unoccupied
    var spaces: Array<Array<Char>> = Array(boardSize) {Array(boardSize) {' '} }
    //Walls are two spaces wide, and cannot overlap, so the intersections are tracked instead.
    enum class IntersectType {EMPTY, VERTICAL, HORIZONTAL}
    var wallIntersects: Array<Array<IntersectType>> = Array(boardSize - 1) { Array(boardSize - 1) {IntersectType.EMPTY} }

    fun getPieceAt(row: Int, col: Int): Int? {
        return if(spaces[row][col] != ' ') spaces[row][col].toString().toInt()
        else null
    }

    fun setWalls(placedWalls: List<List<com.andrewleetham.quoridorserver.model.IntersectType>>) {
        for(row in placedWalls.indices) {
            for (col in placedWalls[row].indices) {
                wallIntersects[row][col] = IntersectType.valueOf(placedWalls[row][col].name)
            }
        }
    }
}
