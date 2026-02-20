package com.andrewleetham.quoridor

import com.andrewleetham.quoridorserver.model.PlayerState
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable


class QuoridorPlayer(start: Pair<Int, Int>, boardSize: Int, var walls: Int, val playerName: String, val color: Color) {
    var position: Pair<Int, Int> = start
    val winCoord: Int
    val winOnColumn: Boolean

    init{
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

    fun checkWin(): Boolean{
        return if(winOnColumn) position.second == winCoord else position.first == winCoord
    }


    fun buildPlayerDisplay(skin: Skin): Table {
        val table = Table()
        table.defaults().pad(4f)
        val backgroundMap = Pixmap(1, 1, Pixmap.Format.RGB888)
        backgroundMap.setColor(color)
        backgroundMap.fill()
        val background = TextureRegionDrawable(TextureRegion(Texture(backgroundMap)))
        backgroundMap.dispose()
        table.background = background
        table.pad(10f)

        var label = Label(playerName, skin)
        table.add(label).colspan(11).center()
        table.row()

        label = Label("Walls:", skin)
        table.add(label).growX()

        for (i in 1..10){
            val text = if(walls >= i) "[#]" else "[ ]"
            label = Label(text, skin)
            table.add(label).width(20f).center().growX()
        }

        return table
    }

    companion object {
        fun fromPlayerState(state: PlayerState): QuoridorPlayer {
            return QuoridorPlayer(Pair(0,0), 9, state.walls, state.name, state.color.toGdxColor())
        }
    }

}
