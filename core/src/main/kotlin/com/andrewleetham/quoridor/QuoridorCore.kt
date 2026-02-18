package com.andrewleetham.quoridor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import java.lang.NumberFormatException
import kotlin.math.abs
import kotlin.random.Random

class QuoridorCore (val host: Main) {
    private var players: Array<QuoridorPlayer> = emptyArray()
    private var board: QuoridorBoard = QuoridorBoard(1)
    private var currentPlayerIndex = -1
    private var gameEnd = false
    lateinit var boardCells: Array<Array<Table>>
    lateinit var wallGhost: Image

    fun PrepareGame(playerCount: Int, boardSize: Int = 9): Boolean {
        if (playerCount < 2 || playerCount > 4 || boardSize < 3){
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



    fun RunGame() {
        if (players.isEmpty()){
            println("The game cannot run if it isn't set up!")
            return
        }
        currentPlayerIndex = Math.abs(Random.nextInt()) % players.count()
        gameEnd = false
        var line: String

        while (!gameEnd){
            ShowGameState()
            println()
            println("${players[currentPlayerIndex].playerName}'s turn")
            println("Enter 'help' for detailed information on commands")
            println("Enter 'rules' to see the Quoridor rules")
            println("Enter 'move <row> <column>' to move your piece")
            println("Enter 'wall <row> <column> [v/vertical/h/horizontal]' to place a wall")
            println("Enter 'exit' to stop the game early")
            line = readln().lowercase()
            val split = line.split(' ')
            when (split[0]){
                "help" -> {
                    println("Commands are not case-sensitive. Use only one space between parts of your commands.\n" +
                            "When using 'move' or 'wall', the command will fail if the move you entered is not legal. \n" +
                            "<row> and <column> are to be replaced with the number of the row and column for your move \n" +
                            "Because walls cover the boundaries between two pairs of adjacent spaces, use the coordinate \n" +
                            "of the space in the top left of the wall.\n" +
                            " For example, 'wall 1 1 v' will try to place a wall blocking off the top two rows of the first column.\n" +
                            "'rules' will show the same thing as the Rules option from the main menu \n" +
                            "'exit' will return you to the main menu and reset the game")
                }
                "rules" -> ShowRules()
                "move" -> {
                    var row = Int.MIN_VALUE
                    var col = Int.MIN_VALUE
                    if (split.count() == 3){
                        try {
                            row = split[1].toInt() - 1
                            col = split[2].toInt() - 1
                        } catch(e: NumberFormatException){
                            println("Enter the coordinates of your move as numerals, like 'move 3 4'")
                            println("Press enter to continue")
                            readln()
                            continue
                        }
                        //
                        if (row in 0 until board.boardSize && col in 0 until board.boardSize){
                            if (ValidMoves(players[currentPlayerIndex].position.first,
                                players[currentPlayerIndex].position.second).contains(Pair(row, col))){
                                board.spaces[row][col] = players[currentPlayerIndex].displayChar
                                board.spaces[players[currentPlayerIndex].position.first][players[currentPlayerIndex].position.second] = ' '
                                players[currentPlayerIndex].position = Pair(row, col)
                                if(players[currentPlayerIndex].CheckWin()){
                                    println("${players[currentPlayerIndex].playerName} wins!")
                                    gameEnd = true
                                } else {
                                    currentPlayerIndex = (currentPlayerIndex + 1) % players.count()
                                }
                            } else {
                                println("That is not a position you can move to. Try again.")
                            }
                        } else {
                            println("Please choose a space on the board.")
                        }
                    } else {
                        println("Enter the coordinates of your move separated by one space, like 'move 3 4")
                    }
                }
                "wall" -> {
                    if (players[currentPlayerIndex].walls < 1) {
                        println("You do not have any walls to place")
                        println("Press enter to continue")
                        readln()
                        continue
                    }
                    var row = Int.MIN_VALUE
                    var col = Int.MIN_VALUE
                    if (split.count() == 4){
                        try {
                            row = split[1].toInt() - 1
                            col = split[2].toInt() - 1
                        } catch(e: NumberFormatException){
                            println("Enter the coordinates for your wall as numerals, like 'wall 5 2 h'")
                            println("Press enter to continue")
                            readln()
                            continue
                        }
                        //
                        if (row in 0 until board.boardSize - 1 && col in 0 until board.boardSize - 1){
                            if (split[3].lowercase()[0] in setOf('v', 'h')){
                                val wallType: QuoridorBoard.IntersectType
                                var collision = board.wallIntersects[row][col] != QuoridorBoard.IntersectType.EMPTY
                                if(split[3].lowercase()[0] == 'v'){
                                    wallType = QuoridorBoard.IntersectType.VERTICAL
                                    collision = collision ||
                                            (row != 0 && board.wallIntersects[row - 1][col] == wallType) ||
                                            (row != board.boardSize - 2 && board.wallIntersects[row + 1][col] == wallType)
                                } else {
                                    wallType = QuoridorBoard.IntersectType.HORIZONTAL
                                    collision = collision ||
                                            (col != 0 && board.wallIntersects[row][col - 1] == wallType) ||
                                            (col != board.boardSize - 2 && board.wallIntersects[row][col + 1] == wallType)
                                }
                                if (!collision){
                                    board.wallIntersects[row][col] = wallType
                                    var paths = true
                                    for (player in players){
                                        paths = paths && HasPath(player)
                                    }
                                    if (paths){
                                        players[currentPlayerIndex].walls--
                                        println("Wall Placed")
                                        currentPlayerIndex = (currentPlayerIndex + 1) % players.count()
                                    } else {
                                        board.wallIntersects[row][col] = QuoridorBoard.IntersectType.EMPTY
                                        println("You can't place a wall there, someone wouldn't have a path to their goal")
                                    }
                                } else {
                                 println("You can't place a wall there, it would collide with an existing wall")
                                }
                            } else {
                                println("The options for wall direction are vertical(v) and horizontal(h)")
                            }
                        } else {
                            println("Please choose an intersection on the board.")
                        }
                    } else {
                        println("Enter the coordinates of your wall and its direction separated by one space, like 'wall 5 2 h")
                    }
                }
                "exit" -> {
                    println("Are you sure you want to cancel this game? You won't be able to resume it.")
                    println("Enter 'yes' to confirm")
                    line = readln().lowercase()
                    if (line == "yes") {
                        players = emptyArray()
                        board = QuoridorBoard(1)
                        break
                    }
                }
                else -> println("Sorry, that command wasn't recognized. Try again.")
            }
            println("Press enter to continue")
            readln()
        }
    }

    fun ShowGameState() {
        for (player in players) player.Display()
        board.DisplayBoard()
    }

    fun HasPath(player: QuoridorPlayer): Boolean{
        val start = player.position
        val destinations: Set<Pair<Int, Int>>
        if (player.winOnColumn){
            destinations = (0 until board.boardSize).map { it to player.winCoord }.toSet()
        } else {
            destinations = (0 until board.boardSize).map { player.winCoord to it}.toSet()
        }
        return FindPath(start, mutableSetOf(), destinations)
    }

    fun FindPath(position: Pair<Int, Int>, path: MutableSet<Pair<Int, Int>>, destinations: Set<Pair<Int, Int>>): Boolean {
        if(path.contains(position)) return false
        if(destinations.contains(position)) return true
        path.add(position)
        for (space in ValidMoves(position.first, position.second)){
            if (FindPath(space, path, destinations)) return true
        }
        return false
    }

    fun ValidMoves(startRow: Int, startCol: Int): Set<Pair<Int, Int>> {
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

    fun ShowRules() {
        println("To Win, be the first to get to the far side of the board from where you start.")
        println("This program handles setup for you, and randomly decides who goes first.")
        println("On your turn, either move your piece or place a wall. If you have no walls left, you must move your piece")
        println("Pieces move one space at a time, horizontally or vertically. You cannot move through a wall")
        println("Walls are two spaces wide, and are placed between the spaces. In this program, you choose a space to\n" +
                "place the center of your wall at the bottom right corner of. Walls cannot extend off the board")
        println("Walls can be placed to block opponents, but a path to each player's goal must remain open")
        println("When two pieces are next to each other, and not separated by a wall, you can instead jump over the other\n" +
                "piece to the space on the far side. If you cannot move to that space (because it is off the board, blocked\n" +
                "by a fence, or occupied by a third piece), you may instead jump to either side of that piece, if possible")
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
                    val legalMoves = ValidMoves(
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
        if(players[currentPlayerIndex].CheckWin()){
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
                paths = paths && HasPath(player)
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
