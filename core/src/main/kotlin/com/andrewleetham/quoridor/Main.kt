package com.andrewleetham.quoridor

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.viewport.ScreenViewport


class Main : ApplicationAdapter() {
    enum class GameScreen {MENU, RULES, HELP, GAME, WIN}
    enum class TurnAction {MOVE, WALL_VERTICAL, WALL_HORIZONTAL}
    private lateinit var skin: Skin
    private lateinit var stage: Stage
    private lateinit var game : QuoridorCore
    private lateinit var currentScreen: GameScreen
    private var readyToPlay = false
    private lateinit var root: Table
    private lateinit var currentAction: TurnAction

    override fun create() {
        skin = Skin(Gdx.files.internal("metalui/metal-ui.json"))

        stage = Stage(ScreenViewport())
        Gdx.input.setInputProcessor(stage)



        game = QuoridorCore(this)
        currentScreen = GameScreen.MENU
        root = Table()
        root.setFillParent(true)
        stage.addActor(root)
        buildLayout()


    }

    fun buildLayout() {
        root.clear()
        when (currentScreen) {
            GameScreen.MENU -> root.add(buildMenuScreen()).grow()
            GameScreen.RULES -> root.add(buildRulesScreen()).grow()
            GameScreen.HELP -> root.add(buildHelpScreen()).grow()
            GameScreen.GAME -> root.add(buildGameScreen()).grow()
            GameScreen.WIN -> root.add(buildWinScreen()).grow()
        }
    }

    fun triggerWin() {
        currentScreen = GameScreen.WIN
    }

    private fun buildWinScreen(): Table {
        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        //title
        var label = Label("GAME OVER", skin)
        label.setFontScale(1.5f)
        table.add(label).center()
        table.row()

        label = Label("${game.getCurrentPlayer().playerName} wins!", skin)
        table.add(label).center()
        table.row()

        val textButton = TextButton("Return to Menu", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentScreen = GameScreen.MENU
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
    }

    fun buildMenuScreen(): Table {
        val table = Table()
        table.defaults().pad(15f)
        table.pad(30f)

        //title
        var label = Label("Welcome to Quoridor!", skin)
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
                    0 ->  readyToPlay = game.prepareGame(2, 9)
                    1 -> readyToPlay = game.prepareGame(3, 9)
                    2 -> readyToPlay = game.prepareGame(4, 9)
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

        //Start button
        textButton = TextButton("Start Game", skin)
        textButton.isDisabled = !readyToPlay
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                game.startGame()
                currentAction = TurnAction.MOVE
                currentScreen = GameScreen.GAME
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
                currentScreen = GameScreen.MENU
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
        val table = Table()

        //Display the players
        val playersTable = game.buildPlayersTable(skin)
        table.add(playersTable).growX().colspan(2)
        table.row()

        val boardTable = game.buildBoardTable(skin, currentAction)
        boardTable.name = "boardTable"
        table.add(boardTable).center().expandX()

        val turnOptionsTable = Table()
        val label = Label("Move Options", skin)
        turnOptionsTable.add(label)
        turnOptionsTable.row()
        if (game.getCurrentPlayer().walls < 1) currentAction = TurnAction.MOVE
        var textButton = TextButton("Place Wall (Horizontal)", skin)
        textButton.isDisabled = currentAction == (TurnAction.WALL_HORIZONTAL) || game.getCurrentPlayer().walls < 1
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentAction = TurnAction.WALL_HORIZONTAL
                buildLayout()
            }
        })
        turnOptionsTable.add(textButton).center().expandY()
        turnOptionsTable.row()
        textButton = TextButton("Place Wall (Vertical)", skin)
        textButton.isDisabled = (currentAction == TurnAction.WALL_VERTICAL) || game.getCurrentPlayer().walls < 1
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

        textButton = TextButton("Help", skin)
        textButton.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentScreen = GameScreen.HELP
                buildLayout()
            }
        })
        table.add(textButton).center().colspan(2)

        return  table
    }

    fun updateWallGhost() {
        val boardTable = root.findActor<Table>("boardTable")
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

        val legal = game.isLegalWallPlacement(row, col, horizontal)

        wallGhost.isVisible = true
        wallGhost.color = if (legal) Color(0f, 0f, 1f, 0.4f) else Color(1f,0f,0f,0.5f)


        val cell = game.boardCells[row][col]
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
            game.placeWall(row, col, horizontal)
        }
    }

}

