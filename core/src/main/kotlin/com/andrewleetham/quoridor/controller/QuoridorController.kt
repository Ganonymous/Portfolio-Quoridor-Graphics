package com.andrewleetham.quoridor.controller

import com.andrewleetham.quoridor.QuoridorPlayer
import com.andrewleetham.quoridorserver.model.RunningGameState

interface QuoridorController {
    fun requestMove(target: Pair<Int, Int>)
    fun requestWall(target: Pair<Int, Int>, horizontal: Boolean)
    fun getGameState(): RunningGameState
    fun getWinningPlayer(): QuoridorPlayer?
}
