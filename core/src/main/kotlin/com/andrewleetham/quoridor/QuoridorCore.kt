package com.andrewleetham.quoridor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import kotlin.math.abs
import kotlin.random.Random

class QuoridorCore (val host: Main) {
    private var players: Array<QuoridorPlayer> = emptyArray()
    private var board: QuoridorBoard = QuoridorBoard(1)
    private var currentPlayerIndex = -1
    private var gameEnd = false
    lateinit var boardCells: Array<Array<Table>>
    lateinit var wallGhost: Image

    fun prepareGame(playerCount: Int, boardSize: Int = 9): Boolean {
        if (playerCount !in 2..4 || boardSize < 3){
            println("Invalid Starting conditions")
            return false
        }
        board = QuoridorBoard(boardSize)
        when (playerCount) {
            2 -> {
                val player1 = QuoridorPlayer(Pair(boardSize / 2, 0), boardSize, boardSize + 1, "Player 1", Color.BLUE,'1')
                board.spaces[player1.position.first][player1.position.second] = '0'
                val player2 = QuoridorPlayer(Pair(boardSize / 2, boardSize - 1), boardSize, boardSize + 1, "Player 2", Color.RED, '2')
                board.spaces[player2.position.first][player2.position.second] = '1'
                players = arrayOf(player1, player2)
            }
            3 -> {
                val player1 = QuoridorPlayer(Pair(boardSize / 2, 0), boardSize, (boardSize / 2) + 1, "Player 1", Color.BLUE, '1')
                board.spaces[player1.position.first][player1.position.second] = '0'
                val player2 = QuoridorPlayer(Pair(boardSize / 2, boardSize - 1), boardSize, (boardSize / 2) + 1, "Player 2", Color.RED, '2')
                board.spaces[player2.position.first][player2.position.second] = '1'
                val player3 = QuoridorPlayer(Pair(0, boardSize / 2), boardSize, (boardSize / 2) +1, "Player 3", Color.GREEN, '3')
                board.spaces[player3.position.first][player3.position.second] = '2'
                players = arrayOf(player1, player2, player3)
            }
            4 -> {
                val player1 = QuoridorPlayer(Pair(boardSize / 2, 0), boardSize, (boardSize / 2) + 1, "Player 1", Color.BLUE, '1')
                board.spaces[player1.position.first][player1.position.second] = '0'
                val player2 = QuoridorPlayer(Pair(boardSize / 2, boardSize - 1), boardSize, (boardSize / 2) + 1, "Player 2", Color.RED, '2')
                board.spaces[player2.position.first][player2.position.second] = '1'
                val player3 = QuoridorPlayer(Pair(0, boardSize / 2), boardSize, (boardSize / 2) +1, "Player 3", Color.GREEN, '3')
                board.spaces[player3.position.first][player3.position.second] = '2'
                val player4 = QuoridorPlayer(Pair(boardSize - 1, boardSize / 2), boardSize, (boardSize / 2) +1, "Player 4", Color.YELLOW, '4')
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


    fun buildPlayersTable(skin: Skin): Table{
        val table = Table()

        table.add(players[0].buildPlayerDisplay(skin)).growX()
        table.add(players[1].buildPlayerDisplay(skin)).growX()
        if(players.count() > 2){
            table.row()
            table.add(players[2].buildPlayerDisplay(skin)).growX()
            if (players.count() == 4){
                table.add(players[3].buildPlayerDisplay(skin)).growX()
            }
        }

        table.row()
        val label = Label("${players[currentPlayerIndex].playerName}'s Turn", skin)
        table.add(label).colspan(2).center().pad(10f)


        return table
    }

    fun getCurrentPlayer(): QuoridorPlayer {return players[currentPlayerIndex]}

    fun buildBoardTable(skin: Skin, intendedAction: Main.TurnAction): Table {
        val cellSize = 48f
        val gap = 12f

        val table = Table()
        table.defaults().pad(gap / 2) // groove spacing
        table.background = skin.getDrawable("rect")


        boardCells = Array(9) { Array(9) { Table() } }

        for (row in 0 .. 8) { // board tracks top-down
            for (col in 0..8) {
                val cellTable = Table()
                cellTable.background = skin.getDrawable("button-pressed")
                cellTable.background?.let { cellTable.color = Color.LIGHT_GRAY }

                // piece
                val pieceIndex = board.getPieceAt(row, col)
                if (pieceIndex != null) {
                    val piece = Image(skin.getDrawable("radio-on"))
                    piece.setScaling(Scaling.fit)
                    piece.setColor(players[pieceIndex].color)

                    // size relative to cell
                    val pawnSize = cellSize
                    piece.setSize(pawnSize, pawnSize)
                    val xOffset = pawnSize * 0.1f
                    piece.setPosition(
                        ((cellSize - pawnSize) / 2f) + xOffset,
                        (cellSize - pawnSize) / 2f
                    )

                    cellTable.addActor(piece)
                }

                // highlight
                if (intendedAction == Main.TurnAction.MOVE) {
                    val legalMoves = validMoves(
                        players[currentPlayerIndex].position.first,
                        players[currentPlayerIndex].position.second
                    )
                    if (Pair(row, col) in legalMoves) {
                        val highlight = Image(skin.getDrawable("white"))
                        highlight.color = Color(0f, .25f, 1f, 0.3f)
                        highlight.setSize(cellSize, cellSize)
                        highlight.setPosition(0f, 0f)
                        cellTable.addActor(highlight)

                        cellTable.addListener(object : ClickListener() {
                            override fun clicked(event: InputEvent?, x2: Float, y2: Float) {
                                performMove(Pair(row, col))
                            }
                        })
                    }
                }

                boardCells[row][col] = cellTable
                table.add(cellTable).size(cellSize)
            }
            table.row()
        }

        table.pack()
        table.layout()

        // Place walls
        val walls = board.wallIntersects

        for (x in 0 until 8) {
            for (y in 0 until 8) {
                when (walls[x][y]) {
                    QuoridorBoard.IntersectType.EMPTY -> { /* nothing */ }

                    QuoridorBoard.IntersectType.HORIZONTAL -> {
                        val wall = Image(skin.getDrawable("button")) // reuse drawable
                        wall.setColor(Color.DARK_GRAY)

                        // anchor to the bottom-left cell of the 2x2 intersection
                        val cell = boardCells[x][y]
                        val boardLocal = cell.localToActorCoordinates(table,Vector2(0f, 0f))

                        wall.setSize(cellSize * 2 + gap, gap)
                        wall.setPosition(boardLocal.x, boardLocal.y - gap)

                        table.addActor(wall)
                    }

                    QuoridorBoard.IntersectType.VERTICAL -> {
                        val wall = Image(skin.getDrawable("button"))
                        wall.setColor(Color.DARK_GRAY)

                        val cell = boardCells[x][y]
                        val boardLocal = cell.localToActorCoordinates(table,Vector2(0f, 0f))

                        wall.setSize(gap, cellSize * 2 + gap)
                        wall.setPosition(boardLocal.x + cellSize, boardLocal.y - (cellSize + gap))

                        table.addActor(wall)
                    }
                }
            }
        }


        // wall ghost (floats above board)
        wallGhost = Image(skin.getDrawable("white"))
        wallGhost.color = Color(0f,0f,1f,0.4f)
        wallGhost.isVisible = false
        wallGhost.name = "wallGhost"
        table.addActor(wallGhost)

        return table
    }

    fun performMove(target: Pair<Int, Int>) {
        board.spaces[target.first][target.second] = currentPlayerIndex.toString()[0]
        board.spaces[players[currentPlayerIndex].position.first][players[currentPlayerIndex].position.second] = ' '
        players[currentPlayerIndex].position = target
        if(players[currentPlayerIndex].checkWin()){
            host.triggerWin()
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.count()
        }
        host.buildLayout()
    }

    fun isLegalWallPlacement( row: Int, col: Int, horizontal: Boolean): Boolean {
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

    fun placeWall(row: Int, col: Int, horizontal: Boolean) {
        //Block attempts to place out-of-bounds walls
        if (col !in 0..board.boardSize - 2 || row !in 0..board.boardSize - 2) return
        //Block attempts to place with no walls remaining
        if (players[currentPlayerIndex].walls < 1) return

        val newWall = if(horizontal) QuoridorBoard.IntersectType.HORIZONTAL else QuoridorBoard.IntersectType.VERTICAL
        board.wallIntersects[row][col] = newWall
        players[currentPlayerIndex].walls--
        currentPlayerIndex = (currentPlayerIndex + 1) % players.count()
        host.buildLayout()
    }
}
