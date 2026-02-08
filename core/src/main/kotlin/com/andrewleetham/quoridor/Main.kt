package com.andrewleetham.quoridor

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import javax.swing.event.ChangeEvent


class Main : ApplicationAdapter() {
    enum class GameScreen {MENU, RULES, HELP, GAME}
    private var skin: Skin? = null
    private var stage: Stage? = null
    private var game : QuoridorCore? = null
    private var currentScreen: GameScreen? = null
    private var readyToPlay = false
    private var root: Table? = null

    override fun create() {
        skin = Skin(Gdx.files.internal("metalui/metal-ui.json"))

        stage = Stage(ScreenViewport())
        Gdx.input.setInputProcessor(stage)



        game = QuoridorCore()
        currentScreen = GameScreen.MENU
        root = Table()
        root!!.setFillParent(true)
        stage!!.addActor(root)
        buildLayout()


    }

    fun buildLayout() {
        root!!.clear()
        when (currentScreen!!) {
            GameScreen.MENU -> root!!.add(buildMenuScreen())
            GameScreen.RULES -> root!!.add(buildRulesScreen())
            GameScreen.HELP -> root!!.add(buildHelpScreen())
            GameScreen.GAME -> root!!.add(buildGameScreen())
        }
    }

    override fun render() {
        Gdx.gl.glClearColor(.9f, .9f, .9f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage!!.act()
        stage!!.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage!!.viewport.update(width, height, true)
    }

    override fun dispose() {
        skin!!.dispose()
        stage!!.dispose()
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
        var playerSelectTable = Table()
        playerSelectTable.defaults().pad(10f)
        var playerButtons = object: ButtonGroup<CheckBox>(){
            override fun canCheck(button: CheckBox?, newState: Boolean): Boolean {
                val returnVal = super.canCheck(button, newState)
                when(checkedIndex){
                    0 ->  readyToPlay = game!!.PrepareGame(2, 9)
                    1 -> readyToPlay = game!!.PrepareGame(3, 9)
                    2 -> readyToPlay = game!!.PrepareGame(4, 9)
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

        label = Label("This is a placeholder. I'll type in the rules later", skin)
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



        return  table
    }

    fun buildGameScreen(): Table {
        val table = Table()

        return  table
    }
}
fun main() {

    val game = QuoridorCore()
    var command : Int
    do {
        println("Welcome to the Quoridor terminal version.")
        println("Please choose an action:")
        println("1. Read the rules")
        println("2. Set up a game")
        println("3. Start playing")
        println("4. Exit")
        println()
        println("Enter the number for your choice: ")

        var line = readln()
        try {
            command = line.toInt()
        } catch (e: NumberFormatException){
            command = 0
            println("Enter a number")
        }

        when(command){
            1 -> {
                game.ShowRules()
                println("Press enter to continue")
                readln()
            }
            2 -> {
                println("How many players? ")
                line = readln()
                try {
                    val players = line.toInt()
                    println(if (game.PrepareGame(players)) "Game is set up!" else "Something went wrong in setup, try again.")
                } catch (e: NumberFormatException){
                    println("Enter a number")
                }
                println("Press enter to return to the menu.")
                readln()
            }
            3 -> game.RunGame()
            4 -> println("Goodbye!")
            0-> println()
            else -> println("Invalid choice: '$command', try again.")
        }


    } while (command != 4)
}
