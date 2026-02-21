package com.andrewleetham.quoridor.controller

import com.andrewleetham.quoridor.QuoridorCore
import com.andrewleetham.quoridor.QuoridorPlayer
import com.andrewleetham.quoridor.ServerProxy
import com.andrewleetham.quoridorserver.model.FinishedGameState
import com.andrewleetham.quoridorserver.model.GameState
import com.andrewleetham.quoridorserver.model.RunningGameState

class OnlineController(
    private val gameId: String,
    private val onStateUpdated: (GameState) -> Unit,
    private val proxy: ServerProxy = ServerProxy.Instance()
) : QuoridorController{
    override fun requestMove(target: Pair<Int, Int>) {
        //Send move to proxy
    }

    override fun requestWall(target: Pair<Int, Int>, horizontal: Boolean) {
        //Send wall to proxy
    }

    override fun getGameState(): GameState {
        TODO()// Get the GameState from the proxy
    }

    override fun getWinner(): String? {
        //get winner from FinishedGameState
        return null
    }


    fun startGame(){
        //tell proxy to start
    }
}
