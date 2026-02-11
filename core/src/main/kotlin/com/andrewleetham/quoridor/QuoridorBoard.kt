package com.andrewleetham.quoridor

class QuoridorBoard(val boardSize: Int){
    //Spaces contain the digit of the player in them, or a space if unoccupied
    var spaces: Array<Array<Char>> = Array(boardSize) {Array(boardSize) {' '} }
    //Walls are two spaces wide, and cannot overlap, so the intersections are tracked instead.
    enum class IntersectType {EMPTY, VERTICAL, HORIZONTAL}
    var wallIntersects: Array<Array<IntersectType>> = Array(boardSize - 1) { Array(boardSize - 1) {IntersectType.EMPTY} }

    fun DisplayBoard() {
        var printString = "  "
        for (col in 1..boardSize) printString += "$col "
        println(printString)
        printString = " ┌"
        for (col in 1 until boardSize) printString += "─┬"
        printString += "─┐"
        println(printString)
        for (row in spaces.indices){
            // Protection against looking for out of bounds wall intersections
            val above = if (row > 0) row - 1 else 0
            val below = if (row < boardSize - 1) row else row - 1

            var display = "${row + 1}│"
            for (col in spaces[row].indices){
                display += spaces[row][col]
                display += if (col == boardSize - 1 || (wallIntersects[above][col] != IntersectType.VERTICAL &&
                            wallIntersects[below][col] != IntersectType.VERTICAL))
                    "│"
                else
                    "║"
            }
            println(display)
            if(row < boardSize - 1){
                display = if(wallIntersects[row][0] == IntersectType.HORIZONTAL) " ├═" else " ├─"
                for (col in wallIntersects[row].indices){
                    display += when (wallIntersects[row][col]) {
                        IntersectType.EMPTY -> {
                            if(col == boardSize - 2 || wallIntersects[row][col + 1] != IntersectType.HORIZONTAL) "┼─" else "┼═"
                        }
                        IntersectType.VERTICAL -> {
                            if(col == boardSize - 2 || wallIntersects[row][col + 1] != IntersectType.HORIZONTAL) "╫─" else "╫═"
                        }
                        IntersectType.HORIZONTAL -> "╪═"
                    }
                }
                display += "┤"
                println(display)
            }
        }
        printString = " └"
        for (col in 1 until boardSize) printString += "─┴"
        printString += "─┘"
        println(printString)
    }

    fun getPieceAt(row: Int, col: Int): Int? {
        if(spaces[row][col] != ' ') return spaces[row][col].toString().toInt()
        else return null
    }
}
