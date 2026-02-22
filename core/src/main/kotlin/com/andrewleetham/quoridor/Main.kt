package com.andrewleetham.quoridor

import com.andrewleetham.quoridor.controller.LocalController
import com.andrewleetham.quoridor.controller.OnlineController
import com.andrewleetham.quoridor.controller.QuoridorController
import com.andrewleetham.quoridorserver.model.FinishedGameState
import com.andrewleetham.quoridorserver.model.LoadingGameState
import com.andrewleetham.quoridorserver.model.LobbyGameState
import com.andrewleetham.quoridorserver.model.RunningGameState
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScreenViewport


class Main : ApplicationAdapter() {
    enum class GameScreen {MAIN_MENU, LOCAL_MENU, ONLINE_MENU, ONLINE_LOBBY, RULES, HELP, GAME, WIN}
    enum class TurnAction {MOVE, WALL_VERTICAL, WALL_HORIZONTAL}
    private lateinit var skin: Skin
    private lateinit var stage: Stage
    private lateinit var controller: QuoridorController
    private lateinit var currentScreen: GameScreen
    private var readyToPlay = false
    private lateinit var root: Table
    private lateinit var currentAction: TurnAction
    private lateinit var proxy: ServerProxy
    private var message = ""
    private var myName = ""

    override fun create() {
        skin = Skin(Gdx.files.internal("metalui/metal-ui.json"))
        proxy = ServerProxy.retrieveInstance()
        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        skin.add("playerBlue", makeColorDrawable(Color.BLUE), Drawable::class.java)
        skin.add("playerRed", makeColorDrawable(Color.RED), Drawable::class.java)
        skin.add("playerGreen", makeColorDrawable(Color.GREEN), Drawable::class.java)
        skin.add("playerYellow", makeColorDrawable(Color.YELLOW), Drawable::class.java)


        currentScreen = GameScreen.MAIN_MENU
        root = Table()
        root.setFillParent(true)
        stage.addActor(root)
        buildLayout()


    }

    fun UiCall(block: () -> Unit){
        Gdx.app.postRunnable(block)
    }

    fun buildLayout() {
        root.clear()
        if (message.isEmpty()){
            when (currentScreen) {
                GameScreen.MAIN_MENU -> root.add(buildMainMenuScreen()).grow()
                GameScreen.ONLINE_MENU -> root.add(buildOnlineMenuScreen()).grow()
                GameScreen.ONLINE_LOBBY -> root.add(buildOnlineLobbyScreen()).grow()
                GameScreen.LOCAL_MENU -> root.add(buildLocalMenuScreen()).grow()
                GameScreen.RULES -> root.add(buildRulesScreen()).grow()
                GameScreen.HELP -> root.add(buildHelpScreen()).grow()
                GameScreen.GAME -> root.add(buildGameScreen()).grow()
                GameScreen.WIN -> root.add(buildWinScreen()).grow()
            }
        } else {
            root.add(buildMessageLayer()).grow()
        }
    }

    fun buildMessageLayer(): Table {
        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        var label = Label("MESSAGE:", skin)
        label.setFontScale(1.5f)
        table.add(label).center()
        table.row()

        label = Label(message, skin)
        table.add(label).center()
        table.row()

        val textButton = TextButton("Close message", skin)
        textButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                message = ""
                buildLayout()
            }
        })

        table.add(textButton).center()

        return table
    }

    fun buildMainMenuScreen(): Table {
        if (::controller.isInitialized) {
            if (controller is OnlineController){
                (controller as OnlineController).stopPolling()
            }
        }
        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        //title
        val label = Label("Welcome to Quoridor!", skin)
        label.setFontScale(1.5f)
        table.add(label).center()
        table.row()

        //Rules button
        var textButton = TextButton("Rules", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentScreen = GameScreen.RULES
                buildLayout()
            }
        })
        table.add(textButton).center()
        table.row()

        //Online button
        textButton = TextButton("Play Online", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentScreen = GameScreen.ONLINE_MENU
                buildLayout()
            }
        })
        table.add(textButton).center()
        table.row()

        //Local button
        textButton = TextButton("Start Local Game", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentScreen = GameScreen.LOCAL_MENU
                if (::controller.isInitialized) (controller as? OnlineController)?.dispose()
                controller = LocalController(QuoridorCore(), "Local", onStateUpdated = {
                    currentScreen = if((controller as LocalController).gameWon) GameScreen.WIN else GameScreen.GAME
                    buildLayout()
                })
                buildLayout()
            }
        })
        table.add(textButton).center()
        table.row()

        return table
    }

    fun buildOnlineMenuScreen(): Table {
        var name = ""
        var gameId = ""
        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        //title
        var label = Label("Start or Join Online Game", skin)
        label.setFontScale(1.5f)
        table.add(label).center()
        table.row()

        label = Label("Your name:", skin)
        table.add(label).center()
        table.row()

        var textField = TextField("", skin)

        var textButton = TextButton("Create Online Game", skin)
        textButton.isDisabled = true
        textButton.name = "createGameButton"
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                proxy.createGame(name) {result ->
                    UiCall {
                        if (!result.success) {
                            message = "Failed to set up online game. Try again"
                            buildLayout()
                        } else {
                            myName = name

                            if (::controller.isInitialized) (controller as? OnlineController)?.dispose()

                            controller = OnlineController(result.gameState!!.id, myName, onStateUpdated = {
                                UiCall{
                                    currentScreen = when (it) {
                                        is LoadingGameState -> GameScreen.ONLINE_LOBBY
                                        is LobbyGameState -> GameScreen.ONLINE_LOBBY
                                        is RunningGameState -> GameScreen.GAME
                                        is FinishedGameState -> GameScreen.WIN
                                    }
                                    buildLayout()
                                }
                            }, onEmitMessage = {
                                UiCall {
                                    message = it
                                    buildLayout()
                                }
                            })
                            (controller as OnlineController).startPolling()
                            currentScreen = GameScreen.ONLINE_LOBBY
                            buildLayout()
                        }
                    }
                }

            }
        })

        textField.setTextFieldListener(TextField.TextFieldListener(
            fun (field: TextField, c: Char) {
                name = field.text
                table.findActor<TextButton>("createGameButton").isDisabled = name.isEmpty()
                table.findActor<TextButton>("joinGameButton").isDisabled = gameId.isEmpty() || name.isEmpty()

            }))


        table.add(textField).center()
        table.row()
        table.add(textButton).center()
        table.row()

        label = Label("Game Code:", skin)
        table.add(label).center()
        table.row()

        textField = TextField("", skin)


        textButton = TextButton("Join Online Game", skin)
        textButton.isDisabled = true
        textButton.name = "joinGameButton"
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                proxy.joinGame(name, gameId) {result ->
                    UiCall{
                        if (!result.success) {
                            message = result.errorMessage!!
                            buildLayout()
                        } else {
                            myName = name
                            if (::controller.isInitialized) (controller as? OnlineController)?.dispose()
                            controller = OnlineController(result.gameState!!.id, myName, onStateUpdated = {
                                UiCall{
                                    currentScreen = when (it) {
                                        is LoadingGameState -> GameScreen.ONLINE_LOBBY
                                        is LobbyGameState -> GameScreen.ONLINE_LOBBY
                                        is RunningGameState -> GameScreen.GAME
                                        is FinishedGameState -> GameScreen.WIN
                                    }
                                    buildLayout()
                                }
                            }, onEmitMessage = {
                                UiCall {
                                    message = it
                                    buildLayout()
                                }
                            })
                            (controller as OnlineController).startPolling()
                            currentScreen = when (result.gameState) {
                                is LoadingGameState -> GameScreen.ONLINE_LOBBY
                                is LobbyGameState -> GameScreen.ONLINE_LOBBY
                                is RunningGameState -> GameScreen.GAME
                                is FinishedGameState -> GameScreen.WIN
                            }
                            buildLayout()
                        }
                    }
                }

            }
        })

        textField.setTextFieldListener(TextField.TextFieldListener(
            fun (field: TextField, c: Char) {
                gameId = field.text
                table.findActor<TextButton>("joinGameButton").isDisabled = gameId.isEmpty() || name.isEmpty()
            }))

        table.add(textField).center()
        table.row()

        table.add(textButton).center()
        table.row()

        textButton = TextButton("Return to Main Menu", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentScreen = GameScreen.MAIN_MENU
                buildLayout()
            }
        })

        table.add(textButton).center()
        table.row()


        return table
    }

    fun buildLoading(): Table {
        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        //title
        val label = Label("Loading Game, please wait.", skin)
        label.setFontScale(1.5f)
        table.add(label).center()

        return table
    }

    fun makeColorDrawable(color: Color): Drawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        return TextureRegionDrawable(TextureRegion(texture))
    }


    fun buildOnlineLobbyScreen(): Table {
        if ((controller as OnlineController).getGameState() is LoadingGameState) return buildLoading()
        val lobbyState = (controller as OnlineController).getGameState() as LobbyGameState
        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        //title
        var label = Label("Online Game Lobby", skin)
        label.setFontScale(1.5f)
        table.add(label).center()
        table.row()

        label = Label("Game ID: ${lobbyState.id}", skin)
        table.add(label).center()
        table.row()

        for ((i, player) in lobbyState.players.withIndex()){

            val playerTable = Table()
            playerTable.background = when (i){
                0 -> skin.getDrawable("playerBlue")
                1 -> skin.getDrawable("playerRed")
                2 -> skin.getDrawable("playerGreen")
                else -> skin.getDrawable("playerYellow")
            }
            playerTable.pad(10f)

            label = if (myName == player)Label("$player <- You", skin) else Label(player, skin)
            playerTable.add(label).center()
            table.add(playerTable).center()
            table.row()
        }

        var textButton = TextButton("Quit Lobby", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {

                proxy.quitGame(myName, lobbyState.id) {result ->
                    UiCall{
                        if (!result.success) {
                            message = result.errorMessage!!
                            buildLayout()
                        } else {
                            currentScreen = GameScreen.MAIN_MENU
                            buildLayout()
                        }
                    }
                }

            }
        })
        table.add(textButton).center()
        table.row()

        if (myName == lobbyState.host){
            textButton = TextButton("Start Game", skin)
            textButton.isDisabled = lobbyState.players.count() < 2
            textButton.addListener(object : ChangeListener(){
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    (controller as OnlineController).startGame()
                    textButton = TextButton("Please wait", skin)
                    textButton.isDisabled = true

                }
            })
            table.add(textButton).center()
        }

        return table
    }


    fun buildWinScreen(): Table {
        if (controller is OnlineController){
            (controller as OnlineController).stopPolling()
        }
        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        //title
        var label = Label("GAME OVER", skin)
        label.setFontScale(1.5f)
        table.add(label).center()
        table.row()

        label = Label("${controller.getWinner()} wins!", skin)
        table.add(label).center()
        table.row()

        val textButton = TextButton("Return to Menu", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentScreen = GameScreen.MAIN_MENU
                buildLayout()
            }
        })
        table.add(textButton).center()

        return table
    }

    override fun render() {
        Gdx.gl.glClearColor(.9f, .9f, .9f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (currentScreen == GameScreen.GAME) updateWallGhost()
        stage.act()
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        skin.dispose()
        stage.dispose()
        ServerProxy.retrieveInstance().dispose()
        if (::controller.isInitialized) {
            if (controller is OnlineController){
                (controller as OnlineController).stopPolling()
                (controller as OnlineController).dispose()
            }
        }
    }

    fun buildLocalMenuScreen(): Table {
        val localController = controller as LocalController

        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        //title
        var label = Label("Start Local Game", skin)
        label.setFontScale(1.5f)
        table.add(label).center()
        table.row()

        //player select
        label = Label("How Many Players?", skin)
        table.add(label).center()
        table.row().padTop(5f)
        val playerSelectTable = Table()
        playerSelectTable.defaults().pad(10f)
        val playerButtons = object: ButtonGroup<CheckBox>(){
            override fun canCheck(button: CheckBox?, newState: Boolean): Boolean {
                val returnVal = super.canCheck(button, newState)
                when(checkedIndex){
                    0 -> readyToPlay = localController.prepareGame(2)
                    1 -> readyToPlay = localController.prepareGame(3)
                    2 -> readyToPlay = localController.prepareGame(4)
                    -1 -> readyToPlay = false
                }
                return returnVal
            }
        }
        var checkbox = CheckBox("2 Players", skin, "radio")
        checkbox.isChecked = false
        playerSelectTable.add(checkbox)
        playerButtons.add(checkbox)
        checkbox = CheckBox("3 Players", skin, "radio")
        playerSelectTable.add(checkbox)
        playerButtons.add(checkbox)
        checkbox = CheckBox("4 Players", skin, "radio")
        playerSelectTable.add(checkbox)
        playerButtons.add(checkbox)
        playerButtons.setMaxCheckCount(1)
        table.add(playerSelectTable)
        table.row()

        //Start button
        var textButton = TextButton("Start Game", skin)
        textButton.isDisabled = !readyToPlay
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                localController.startGame()
                currentAction = TurnAction.MOVE
                currentScreen = GameScreen.GAME
                buildLayout()
            }
        })
        table.add(textButton).center()
        table.row()

        textButton = TextButton("Return to Main Menu", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentScreen = GameScreen.MAIN_MENU
                buildLayout()
            }
        })
        table.add(textButton).center()


        return  table
    }

    fun buildRulesScreen(): Table {
        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        //title
        var label = Label("Quoridor Rules", skin)
        label.setFontScale(1.5f)
        table.add(label).center()
        table.row()

        label = Label("To Win, be the first to get to the far side of the board from where you start.\n" +
            "This program handles setup for you, and randomly decides who goes first.\n" +
            "On your turn, either move your piece or place a wall. If you have no walls left, you must move your piece.\n" +
            "Pieces move one space at a time, horizontally or vertically. You cannot move through a wall.\n" +
            "Walls are two spaces wide, and are placed between the spaces. Walls cannot extend off the board.\n" +
            "Walls can be placed to block opponents, but a path to each player's goal must remain open.\n" +
            "When two pieces are next to each other, and not separated by a wall, you can instead jump over the other piece to the space on the far side. If you cannot move to that space (because it is off the board, blocked by a wall, or occupied by a third piece), you may instead jump to either side of that piece, if possible.", skin)
        label.setWrap(true)
        table.add(label).center().growX()
        table.row()

        val textButton = TextButton("Back to Menu", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentScreen = GameScreen.MAIN_MENU
                buildLayout()
            }
        })
        table.add(textButton).center()

        return  table
    }

    fun buildHelpScreen(): Table {
        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        //title
        var label = Label("Quoridor Help", skin)
        label.setFontScale(1.5f)
        table.add(label).center()
        table.row()

        label = Label("The boxes at the top of the screen show the players in order, as well as how many walls each has left.\n" +
            "Below that is a line saying whose turn it is.\n" +
            "On the right are three buttons to allow you to choose to move your piece or place a wall. The system defaults to moving whenever the game loads, or to the same action as the previous player. If you have no walls remaining, the buttons for wall placement will not function.\n" +
            "To the left is the board. Each space shows as a square. Each player's piece appears as a colored dotted circle of the same color as their box at the top of the screen. Walls appear as dark rectangles between the spaces. \n" +
            "When you have selected to move your piece, the spaces you can move to are highlighted in blue. Click on one of these highlighted spaces to move there.\n" +
            "When you have selected to place a wall, hovering the mouse over the board will add a preview of a wall based on the current mouse position. It will be blue if that is a legal place to put a wall, or red if you cannot put a wall there. When the preview is blue, simply click to place a wall at the preview position.", skin )
        label.setWrap(true)
        table.add(label).center().growX()
        table.row()

        val textButton = TextButton("Back to Game", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentScreen = GameScreen.GAME
                buildLayout()
            }
        })
        table.add(textButton).center()

        return  table
    }

    fun buildGameScreen(): Table {
        if (!::currentAction.isInitialized) currentAction = TurnAction.MOVE
        if (controller.getGameState() is LoadingGameState) return buildLoading()
        val state = controller.getGameState() as RunningGameState
        val table = Table()

        //Display the players
        val playersTable = buildPlayersTable(state)
        table.add(playersTable).growX().colspan(2)
        table.row()

        val boardTable = buildBoardTable(state)
        boardTable.name = "boardTable"
        table.add(boardTable).center().expandX()

        val turnOptionsTable = Table()
        val label = Label("Move Options", skin)
        turnOptionsTable.add(label)
        turnOptionsTable.row()
        if (state.players[state.currentPlayerIndex].walls < 1) currentAction = TurnAction.MOVE
        var textButton = TextButton("Place Wall (Horizontal)", skin)
        textButton.isDisabled = currentAction == (TurnAction.WALL_HORIZONTAL) || state.players[state.currentPlayerIndex].walls < 1
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentAction = TurnAction.WALL_HORIZONTAL
                buildLayout()
            }
        })
        turnOptionsTable.add(textButton).center().expandY()
        turnOptionsTable.row()
        textButton = TextButton("Place Wall (Vertical)", skin)
        textButton.isDisabled = (currentAction == TurnAction.WALL_VERTICAL) || state.players[state.currentPlayerIndex].walls < 1
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentAction = TurnAction.WALL_VERTICAL
                buildLayout()
            }
        })
        turnOptionsTable.add(textButton).center().expandY()
        turnOptionsTable.row()
        textButton = TextButton("Move", skin)
        textButton.isDisabled = currentAction == TurnAction.MOVE
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentAction = TurnAction.MOVE
                buildLayout()
            }
        })
        turnOptionsTable.add(textButton).center().expandY()
        table.add(turnOptionsTable).grow()

        table.row()

        if (controller is LocalController){
            textButton = TextButton("Help", skin)
            textButton.addListener(object : ChangeListener(){
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    currentScreen = GameScreen.HELP
                    buildLayout()
                }
            })
            table.add(textButton).center().colspan(2)
        }

        return  table
    }

    fun updateWallGhost() {
        val boardTable = root.findActor<Table>("boardTable")
        if (boardTable != null) {
            val wallGhost = boardTable.findActor<Image>("wallGhost")
            if (currentAction == TurnAction.MOVE) {
                wallGhost.isVisible = false
                return
            }

            val mouse = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            stage.screenToStageCoordinates(mouse)

            val cellSize = 48f
            val gap = 12f

            val local = Vector2(mouse)
            boardTable.stageToLocalCoordinates(local)

            // Remove table padding
            local.x -= boardTable.padLeft
            local.y -= boardTable.padBottom

            val stride = cellSize + gap

            var col = (local.x / stride).toInt()
            var row = 8 - (local.y / stride).toInt()

            // Furthest column and row can map to walls sensibly
            if (col == 8) col = 7
            if (row == 8) row = 7

            // determine wall orientation
            val horizontal = currentAction == TurnAction.WALL_HORIZONTAL


            if (col !in 0..7 || row !in 0..7) {
                wallGhost.isVisible = false
                return
            }
            val core = QuoridorCore()
            core.fromRunningGameState(controller.getGameState() as RunningGameState)
            val legal = core.isLegalWallPlacement(Pair(row, col), horizontal)

            wallGhost.isVisible = true
            wallGhost.color = if (legal) Color(0f, 0f, 1f, 0.4f) else Color(1f,0f,0f,0.5f)


            val cell = root.findActor<Table>("Cell-$row-$col")
            val stagePos = cell.localToStageCoordinates(Vector2(0f,0f))
            val boardLocal = boardTable.stageToLocalCoordinates(stagePos)
            if (horizontal) {
                wallGhost.setSize(cellSize * 2 + gap, gap)
                wallGhost.setPosition(boardLocal.x, boardLocal.y - gap)
            } else {
                wallGhost.setSize(gap, cellSize * 2 + gap)
                wallGhost.setPosition(boardLocal.x + cellSize, boardLocal.y - stride)
            }

            if (legal && Gdx.input.justTouched()) {
                controller.requestWall(Pair(row, col), horizontal)
            }
        }
    }

    fun buildPlayersTable(state: RunningGameState): Table{
        val table = Table()

        table.add(buildPlayerDisplay(QuoridorPlayer.fromPlayerState(state.players[0]))).growX()
        table.add(buildPlayerDisplay(QuoridorPlayer.fromPlayerState(state.players[1]))).growX()
        if(state.players.count() > 2){
            table.row()
            table.add(buildPlayerDisplay(QuoridorPlayer.fromPlayerState(state.players[2]))).growX()
            if (state.players.count() == 4){
                table.add(buildPlayerDisplay(QuoridorPlayer.fromPlayerState(state.players[3]))).growX()
            }
        }

        table.row()
        val label = Label("${state.players[state.currentPlayerIndex].name}'s Turn", skin)
        table.add(label).colspan(2).center().pad(10f)


        return table
    }

    fun buildBoardTable(state: RunningGameState): Table {
        val core = QuoridorCore()
        core.fromRunningGameState(state)
        val cellSize = 48f
        val gap = 12f

        val table = Table()
        table.defaults().pad(gap / 2) // groove spacing
        table.background = skin.getDrawable("rect")


        val boardCells = Array(9) { Array(9) { Table() } }

        for (row in 0 .. 8) { // board tracks top-down
            for (col in 0..8) {
                val cellTable = Table()
                cellTable.name = "Cell-$row-$col"
                cellTable.background = skin.getDrawable("button-pressed")
                cellTable.background?.let { cellTable.color = Color.LIGHT_GRAY }

                // piece
                val pieceIndex = core.getPieceAt(row, col)
                if (pieceIndex != null) {
                    val piece = Image(skin.getDrawable("radio-on"))
                    piece.setScaling(Scaling.fit)
                    piece.setColor(core.getPlayer(pieceIndex).color.toGdxColor())

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
                if (currentAction == TurnAction.MOVE) {
                    val legalMoves = core.validMoves(
                        core.getPlayer(state.currentPlayerIndex).position.first,
                        core.getPlayer(state.currentPlayerIndex).position.second
                    )
                    if (Pair(row, col) in legalMoves) {
                        val highlight = Image(skin.getDrawable("white"))
                        highlight.color = Color(0f, .25f, 1f, 0.3f)
                        highlight.setSize(cellSize, cellSize)
                        highlight.setPosition(0f, 0f)
                        cellTable.addActor(highlight)

                        cellTable.addListener(object : ClickListener() {
                            override fun clicked(event: InputEvent?, x2: Float, y2: Float) {
                                controller.requestMove(Pair(row, col))
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
        val walls = core.getIntersects()

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
        val wallGhost = Image(skin.getDrawable("white"))
        wallGhost.color = Color(0f,0f,1f,0.4f)
        wallGhost.isVisible = false
        wallGhost.name = "wallGhost"
        table.addActor(wallGhost)

        return table
    }

    fun buildPlayerDisplay(player: QuoridorPlayer): Table {
        val table = Table()
        table.defaults().pad(4f)
        val backgroundMap = Pixmap(1, 1, Pixmap.Format.RGB888)
        backgroundMap.setColor(player.color.toGdxColor())
        backgroundMap.fill()
        val background = TextureRegionDrawable(TextureRegion(Texture(backgroundMap)))
        backgroundMap.dispose()
        table.background = background
        table.pad(10f)

        var label = Label(player.playerName, skin)
        table.add(label).colspan(11).center()
        table.row()

        label = Label("Walls:", skin)
        table.add(label).growX()

        for (i in 1..10){
            val text = if(player.walls >= i) "[#]" else "[ ]"
            label = Label(text, skin)
            table.add(label).width(20f).center().growX()
        }

        return table
    }

}

