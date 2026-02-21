package com.andrewleetham.quoridor.controller

import com.andrewleetham.quoridor.ServerProxy
import com.andrewleetham.quoridorserver.model.FinishedGameState
import com.andrewleetham.quoridorserver.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class OnlineController(
    private val gameId: String,
    private val playerName: String,
    private val onStateUpdated: (GameState) -> Unit,
    private val onEmitMessage: (String) -> Unit,
    private val proxy: ServerProxy = ServerProxy.retrieveInstance()
) : QuoridorController{
    private var lastGameState: GameState? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    fun startPolling() {
        pollingJob?.cancel()

        pollingJob = scope.launch {
            while (isActive) {
                refreshGameState()
                delay(1000)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun dispose() {
        scope.cancel()
    }

    override fun requestMove(target: Pair<Int, Int>) {
        proxy.sendMove(playerName, gameId, target) {response ->
            if(response.success){
                onStateUpdated(response.gameState!!)
            }
            else {
                onEmitMessage (response.errorMessage ?: "Something went wrong sending your move")
            }
        }
    }

    override fun requestWall(target: Pair<Int, Int>, horizontal: Boolean) {
        proxy.sendWall(playerName, gameId, target, horizontal) {response ->
            if(response.success){
                onStateUpdated(response.gameState!!)
            }
            else {
                onEmitMessage (response.errorMessage ?: "Something went wrong sending your wall")
            }
        }
    }

    override fun getGameState(): GameState {
        return lastGameState!!

    }

    override fun getWinner(): String? {
        val state = lastGameState
        return if (state is FinishedGameState) state.winner else null
    }


    fun startGame(){
        proxy.startGame(playerName, gameId) { response ->
            if(response.success){
                onStateUpdated(response.gameState!!)
            }
            else {
                onEmitMessage(response.errorMessage ?: "Something went wrong starting game")
            }
        }
    }

    fun refreshGameState() {
        proxy.getGameState(playerName, gameId) { response ->
            if(response.success){
                if (lastGameState != response.gameState){ lastGameState = response.gameState }
            }
        }
    }
}
