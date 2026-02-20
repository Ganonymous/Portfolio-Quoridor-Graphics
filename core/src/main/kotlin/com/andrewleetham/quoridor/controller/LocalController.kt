package com.andrewleetham.quoridor.controller

import com.andrewleetham.quoridor.QuoridorCore
import com.andrewleetham.quoridor.QuoridorPlayer
import com.andrewleetham.quoridorserver.model.RunningGameState

class LocalController(
    private val core: QuoridorCore,
    private val gameId: String,
    private val onStateUpdated: (RunningGameState) -> Unit
) : QuoridorController{
    var gameWon = false
    override fun requestMove(target: Pair<Int, Int>) {
        if (core.applyMove(target)){
            gameWon = core.getCurrentPlayer().checkWin()
            if (!gameWon){core.nextTurn()}
            onStateUpdated(core.toRunningGameState(gameId))
        }
    }

    override fun requestWall(target: Pair<Int, Int>, horizontal: Boolean) {
        if (core.applyWall(target, horizontal)){
            onStateUpdated(core.toRunningGameState(gameId))
            }
    }

    override fun getGameState(): RunningGameState {
        return core.toRunningGameState(gameId)
    }

    override fun getWinningPlayer(): QuoridorPlayer? {
        if (!gameWon){ return null}
        return core.getCurrentPlayer()
    }

    fun prepareGame(players: Int): Boolean{
        return core.prepareGame(players)
    }

    fun startGame(){
        core.startGame()
    }
}
