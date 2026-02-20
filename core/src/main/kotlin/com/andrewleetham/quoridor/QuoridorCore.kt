package com.andrewleetham.quoridor

import com.andrewleetham.quoridorserver.model.IntersectType
import com.andrewleetham.quoridorserver.model.PlayerColor
import com.andrewleetham.quoridorserver.model.PlayerState
import com.andrewleetham.quoridorserver.model.RunningGameState
import com.badlogic.gdx.graphics.Color
import kotlin.math.abs
import kotlin.random.Random

class QuoridorCore () {
    private val boardSize = 9
    private var players: Array<QuoridorPlayer> = emptyArray()
    private var board: QuoridorBoard = QuoridorBoard(9)
    private var currentPlayerIndex = -1
    private var gameEnd = false
    fun fromRunningGameState(state: RunningGameState){
        board = QuoridorBoard(boardSize)
        val playerList = mutableListOf<QuoridorPlayer>()
        for( (i, ps) in state.players.withIndex()){
            val start = when(i){
                0 -> Pair(boardSize / 2, 0)
                1 -> Pair(boardSize / 2, boardSize - 1)
                2 -> Pair(0, boardSize / 2)
                else -> Pair(0, boardSize - 1)
            }

            val color = when(i){
                0 -> Color.BLUE
                1 -> Color.RED
                2 -> Color.GREEN
                else -> Color.YELLOW
            }
            val player = QuoridorPlayer(start, boardSize, ps.walls, ps.name, color)
            player.position = ps.position
            board.spaces[player.position.first][player.position.second] = i.toString()[0]
            playerList.add(player)
        }
        players = playerList.toTypedArray()
        currentPlayerIndex = state.currentPlayerIndex
        board.setWalls(state.placedWalls)

    }

    fun toRunningGameState(gameID: String): RunningGameState {
        return RunningGameState(
            gameID,
            players = players.map{
                PlayerState(it.playerName, it.position, it.walls, PlayerColor.fromGDXColor(it.color))
            },
            currentPlayerIndex,
            placedWalls = board.wallIntersects.map{
                row -> row.map{
                    IntersectType.valueOf(it.name)
            }
            })
    }


    fun prepareGame(playerCount: Int): Boolean {
        if (playerCount !in 2..4){
            println("Invalid Starting conditions")
            return false
        }
        board = QuoridorBoard(boardSize)
        when (playerCount) {
            2 -> {
                val player1 = QuoridorPlayer(Pair(boardSize / 2, 0), boardSize, boardSize + 1, "Player 1", Color.BLUE)
                board.spaces[player1.position.first][player1.position.second] = '0'
                val player2 = QuoridorPlayer(Pair(boardSize / 2, boardSize - 1), boardSize, boardSize + 1, "Player 2", Color.RED)
                board.spaces[player2.position.first][player2.position.second] = '1'
                players = arrayOf(player1, player2)
            }
            3 -> {
                val player1 = QuoridorPlayer(Pair(boardSize / 2, 0), boardSize, (boardSize / 2) + 1, "Player 1", Color.BLUE)
                board.spaces[player1.position.first][player1.position.second] = '0'
                val player2 = QuoridorPlayer(Pair(boardSize / 2, boardSize - 1), boardSize, (boardSize / 2) + 1, "Player 2", Color.RED)
                board.spaces[player2.position.first][player2.position.second] = '1'
                val player3 = QuoridorPlayer(Pair(0, boardSize / 2), boardSize, (boardSize / 2) +1, "Player 3", Color.GREEN)
                board.spaces[player3.position.first][player3.position.second] = '2'
                players = arrayOf(player1, player2, player3)
            }
            4 -> {
                val player1 = QuoridorPlayer(Pair(boardSize / 2, 0), boardSize, (boardSize / 2) + 1, "Player 1", Color.BLUE)
                board.spaces[player1.position.first][player1.position.second] = '0'
                val player2 = QuoridorPlayer(Pair(boardSize / 2, boardSize - 1), boardSize, (boardSize / 2) + 1, "Player 2", Color.RED)
                board.spaces[player2.position.first][player2.position.second] = '1'
                val player3 = QuoridorPlayer(Pair(0, boardSize / 2), boardSize, (boardSize / 2) +1, "Player 3", Color.GREEN)
                board.spaces[player3.position.first][player3.position.second] = '2'
                val player4 = QuoridorPlayer(Pair(boardSize - 1, boardSize / 2), boardSize, (boardSize / 2) +1, "Player 4", Color.YELLOW)
                board.spaces[player4.position.first][player4.position.second] = '3'
                players = arrayOf(player1, player2, player3, player4)
            }
        }

        return true
    }

    fun startGame() {
        if (players.isEmpty()){
            throw Error("Tried to start a game with no players")
        }
        currentPlayerIndex = abs(Random.nextInt()) % players.count()
        gameEnd = false
    }

    fun hasPath(player: QuoridorPlayer): Boolean{
        val start = player.position
        val destinations: Set<Pair<Int, Int>>
        if (player.winOnColumn){
            destinations = (0 until board.boardSize).map { it to player.winCoord }.toSet()
        } else {
            destinations = (0 until board.boardSize).map { player.winCoord to it}.toSet()
        }
        return findPath(start, mutableSetOf(), destinations)
    }

    fun findPath(position: Pair<Int, Int>, path: MutableSet<Pair<Int, Int>>, destinations: Set<Pair<Int, Int>>): Boolean {
        if(path.contains(position)) return false
        if(destinations.contains(position)) return true
        path.add(position)
        for (space in validMoves(position.first, position.second)){
            if (findPath(space, path, destinations)) return true
        }
        return false
    }

    fun validMoves(startRow: Int, startCol: Int): Set<Pair<Int, Int>> {
        val moves = mutableSetOf<Pair<Int, Int>>()


        //Check for legal moves based on moving up
        if (startRow > 0 ){ //there is a row up
            if((startCol == 0 || board.wallIntersects[startRow - 1][startCol - 1] != QuoridorBoard.IntersectType.HORIZONTAL)
                && (startCol == board.boardSize - 1 || board.wallIntersects[startRow - 1][startCol] != QuoridorBoard.IntersectType.HORIZONTAL)){
                // No wall above
                if(board.spaces[startRow - 1][startCol] == ' '){ //space unoccupied
                    moves.add(Pair(startRow - 1, startCol))
                } else { // space occupied. Check for jumps
                    if(startRow > 1 && //there is a row to straight-jump to and
                        (startCol == 0 || board.wallIntersects[startRow - 2][startCol - 1] != QuoridorBoard.IntersectType.HORIZONTAL)
                        && (startCol == board.boardSize - 1 || board.wallIntersects[startRow - 2][startCol] != QuoridorBoard.IntersectType.HORIZONTAL)
                        //No walls blocking the straight jump and
                        && board.spaces[startRow - 2][startCol] == ' '){//destination space is unoccupied
                        moves.add(Pair(startRow - 2, startCol))
                    } else { // Bend jumps are only legal if the straight jump is blocked
                        if(startCol > 0 && //there is a column to bend-jump left to and
                            (startRow == 1 || board.wallIntersects[startRow - 2][startCol - 1] != QuoridorBoard.IntersectType.VERTICAL)
                            && (board.wallIntersects[startRow - 1][startCol - 1] != QuoridorBoard.IntersectType.VERTICAL)
                            // no walls blocking the bend jump and
                            && board.spaces[startRow - 1][startCol - 1] == ' '){ // destination is unoccupied
                            moves.add(Pair(startRow - 1, startCol - 1))
                        }
                        if(startCol < board.boardSize - 1 && //there is a column to bend-jump right to and
                            (startRow == 1 || board.wallIntersects[startRow - 2][startCol] != QuoridorBoard.IntersectType.VERTICAL)
                            && (board.wallIntersects[startRow - 1][startCol] != QuoridorBoard.IntersectType.VERTICAL)
                            // no walls blocking the bend jump and
                            && board.spaces[startRow - 1][startCol + 1] == ' '){ // destination is unoccupied
                            moves.add(Pair(startRow - 1, startCol + 1))
                        }
                    }
                }
            }

        }

        //Check for legal moves based on moving down
        if (startRow < board.boardSize - 1 ){ //there is a row down
            if((startCol == 0 || board.wallIntersects[startRow][startCol - 1] != QuoridorBoard.IntersectType.HORIZONTAL)
                && (startCol == board.boardSize - 1 || board.wallIntersects[startRow][startCol] != QuoridorBoard.IntersectType.HORIZONTAL)){
                // No wall below
                if(board.spaces[startRow + 1][startCol] == ' '){ //space unoccupied
                    moves.add(Pair(startRow + 1, startCol))
                } else { // space occupied. Check for jumps
                    if (startRow < board.boardSize - 2 && //there is a row to straight-jump to and
                        (startCol == 0 || board.wallIntersects[startRow + 1][startCol - 1] != QuoridorBoard.IntersectType.HORIZONTAL)
                        && (startCol == board.boardSize - 1 || board.wallIntersects[startRow + 1][startCol] != QuoridorBoard.IntersectType.HORIZONTAL)
                        //No walls blocking the straight jump and
                        && board.spaces[startRow + 2][startCol] == ' '){//destination space is unoccupied
                        moves.add(Pair(startRow + 2, startCol))
                    } else { // Bend jumps are only legal if the straight jump is blocked
                        if (startCol > 0 && //there is a column to bend-jump left to and
                            (board.wallIntersects[startRow][startCol - 1] != QuoridorBoard.IntersectType.VERTICAL)
                            && (startRow == board.boardSize - 2 || board.wallIntersects[startRow + 1][startCol - 1] != QuoridorBoard.IntersectType.VERTICAL)
                            // no walls blocking the bend jump and
                            && board.spaces[startRow + 1][startCol - 1] == ' '){ // destination is unoccupied
                            moves.add(Pair(startRow + 1, startCol - 1))
                        }
                        if (startCol < board.boardSize - 1 && //there is a column to bend-jump right to and
                            (board.wallIntersects[startRow][startCol] != QuoridorBoard.IntersectType.VERTICAL)
                            && (startRow == board.boardSize - 2 || board.wallIntersects[startRow + 1][startCol] != QuoridorBoard.IntersectType.VERTICAL)
                            // no walls blocking the bend jump and
                            && board.spaces[startRow + 1][startCol + 1] == ' '){ // destination is unoccupied
                            moves.add(Pair(startRow + 1, startCol + 1))
                        }
                    }
                }
            }
        }

        //Check for legal moves based on moving left
        if (startCol > 0 ){ //there is a column left
            if((startRow == 0 || board.wallIntersects[startRow - 1][startCol - 1] != QuoridorBoard.IntersectType.VERTICAL)
                && (startRow == board.boardSize - 1 || board.wallIntersects[startRow][startCol - 1] != QuoridorBoard.IntersectType.VERTICAL)){
                // No wall to the left
                if(board.spaces[startRow][startCol - 1] == ' '){ //space unoccupied
                    moves.add(Pair(startRow, startCol - 1))
                } else { // space occupied. Check for jumps
                    if (startCol > 1 && //there is a column to straight-jump to and
                        (startRow == 0 || board.wallIntersects[startRow - 1][startCol - 2] != QuoridorBoard.IntersectType.VERTICAL)
                        && (startRow == board.boardSize - 1 || board.wallIntersects[startRow][startCol - 2] != QuoridorBoard.IntersectType.VERTICAL)
                        //No walls blocking the straight jump and
                        && board.spaces[startRow][startCol - 2] == ' '){//destination space is unoccupied
                        moves.add(Pair(startRow, startCol - 2))
                    } else { // Bend jumps are only legal if the straight jump is blocked
                        if (startRow > 0 && //there is a row to bend-jump up to and
                            (startCol == 1 || board.wallIntersects[startRow - 1][startCol - 2] != QuoridorBoard.IntersectType.HORIZONTAL)
                            && (board.wallIntersects[startRow - 1][startCol - 1] != QuoridorBoard.IntersectType.HORIZONTAL)
                            // no walls blocking the bend jump and
                            && board.spaces[startRow - 1][startCol - 1] == ' '){ // destination is unoccupied
                            moves.add(Pair(startRow - 1, startCol - 1))
                        }
                        if (startRow < board.boardSize - 1 && //there is a row to bend-jump down to and
                            (startCol == 1 || board.wallIntersects[startRow][startCol - 2] != QuoridorBoard.IntersectType.HORIZONTAL)
                            && (board.wallIntersects[startRow][startCol - 1] != QuoridorBoard.IntersectType.HORIZONTAL)
                            // no walls blocking the bend jump and
                            && board.spaces[startRow + 1][startCol - 1] == ' '){ // destination is unoccupied
                            moves.add(Pair(startRow + 1, startCol - 1))
                        }
                    }
                }
            }

        }

        //Check for legal moves based on moving right
        if (startCol < board.boardSize - 1 ){ //there is a column right
            if((startRow == 0 || board.wallIntersects[startRow - 1][startCol] != QuoridorBoard.IntersectType.VERTICAL)
                && (startRow == board.boardSize - 1 || board.wallIntersects[startRow][startCol] != QuoridorBoard.IntersectType.VERTICAL)){
                // No wall to the right
                if(board.spaces[startRow][startCol + 1] == ' '){ //space unoccupied
                    moves.add(Pair(startRow, startCol + 1))
                } else { // space occupied. Check for jumps
                    if (startCol < board.boardSize - 2 && //there is a column to straight-jump to and
                        (startRow == 0 || board.wallIntersects[startRow - 1][startCol + 1] != QuoridorBoard.IntersectType.VERTICAL)
                        && (startRow == board.boardSize - 1 || board.wallIntersects[startRow][startCol + 1] != QuoridorBoard.IntersectType.VERTICAL)
                        //No walls blocking the straight jump and
                        && board.spaces[startRow][startCol + 2] == ' '){//destination space is unoccupied
                        moves.add(Pair(startRow, startCol + 2))
                    } else { // Bend jumps are only legal if the straight jump is blocked
                        if (startRow > 0 && //there is a row to bend-jump up to and
                            (board.wallIntersects[startRow - 1][startCol] != QuoridorBoard.IntersectType.HORIZONTAL)
                            && (startCol == board.boardSize - 2 || board.wallIntersects[startRow - 1][startCol + 1] != QuoridorBoard.IntersectType.HORIZONTAL)
                            // no walls blocking the bend jump and
                            && board.spaces[startRow - 1][startCol + 1] == ' '){ // destination is unoccupied
                            moves.add(Pair(startRow - 1, startCol + 1))
                        }
                        if (startRow < board.boardSize - 1 && //there is a row to bend-jump down to and
                            (board.wallIntersects[startRow][startCol] != QuoridorBoard.IntersectType.HORIZONTAL)
                            && (startCol == board.boardSize - 2 || board.wallIntersects[startRow][startCol + 1] != QuoridorBoard.IntersectType.HORIZONTAL)
                            // no walls blocking the bend jump and
                            && board.spaces[startRow + 1][startCol + 1] == ' '){ // destination is unoccupied
                            moves.add(Pair(startRow + 1, startCol + 1))
                        }
                    }
                }
            }

        }
        return moves.toSet()
    }



    fun getPlayer(index: Int): QuoridorPlayer {return players[index]}
    fun getCurrentPlayer(): QuoridorPlayer {return players[currentPlayerIndex]}


    fun applyMove(target: Pair<Int, Int>): Boolean {
        val currentPlayer = players[currentPlayerIndex]
        val legal = validMoves(currentPlayer.position.first, currentPlayer.position.second)

        if(target !in legal) {return false}

        board.spaces[target.first][target.second] = currentPlayerIndex.toString()[0]
        board.spaces[currentPlayer.position.first][currentPlayer.position.second] = ' '
        currentPlayer.position = target

        return true
    }

    fun nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.count()
    }

    fun isLegalWallPlacement( target: Pair<Int, Int>, horizontal: Boolean): Boolean {
        val row = target.first
        val col = target.second
        //Out-of-bounds walls are always illegal
        if (col !in 0..board.boardSize - 2 || row !in 0..board.boardSize - 2) return false
        var collision = board.wallIntersects[row][col] != QuoridorBoard.IntersectType.EMPTY
        val wallType = if(horizontal) QuoridorBoard.IntersectType.HORIZONTAL else QuoridorBoard.IntersectType.VERTICAL
        if (horizontal) {
            collision = collision ||
                (col != 0 && board.wallIntersects[row][col - 1] == wallType) ||
                (col != board.boardSize - 2 && board.wallIntersects[row][col + 1] == wallType)
        } else {
            collision = collision ||
                (row != 0 && board.wallIntersects[row - 1][col] == wallType) ||
                (row != board.boardSize - 2 && board.wallIntersects[row + 1][col] == wallType)
        }

        if (!collision) {
            board.wallIntersects[row][col] = wallType
            var paths = true
            for (player in players){
                paths = paths && hasPath(player)
            }
            board.wallIntersects[row][col] = QuoridorBoard.IntersectType.EMPTY
            return paths
        }

        return !collision
    }

    fun applyWall(target: Pair<Int, Int>, horizontal: Boolean): Boolean {
        //Block attempts to place out-of-bounds walls
        if (!isLegalWallPlacement(target, horizontal)) return false
        //Block attempts to place with no walls remaining
        if (players[currentPlayerIndex].walls < 1) return false

        board.wallIntersects[target.first][target.second] =
            if (horizontal) QuoridorBoard.IntersectType.HORIZONTAL else QuoridorBoard.IntersectType.VERTICAL

        players[currentPlayerIndex].walls--
        currentPlayerIndex = (currentPlayerIndex + 1) % players.count()
        return true
    }

    fun getPieceAt(row: Int, col: Int): Int? {
        return if(board.spaces[row][col] != ' ') board.spaces[row][col].toString().toInt()
        else null
    }

    fun getIntersects(): Array<Array<QuoridorBoard.IntersectType>> {
        return board.wallIntersects
    }
}
