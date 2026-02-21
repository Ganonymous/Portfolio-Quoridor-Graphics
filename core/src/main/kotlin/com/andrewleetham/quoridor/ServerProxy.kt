package com.andrewleetham.quoridor

import com.andrewleetham.quoridorserver.model.GameState
import com.andrewleetham.quoridorserver.model.LobbyGameState


class ServerProxy {

    fun createGame (playerName: String): ServerResponse{
        return ServerResponse(success = false, errorMessage = "Server Not Ready")
    }

    fun joinGame (playerName: String, gameId: String) : ServerResponse{
        return ServerResponse(success = false, errorMessage = "Server Not Ready")
    }

    companion object {
        lateinit var instance: ServerProxy
        private set

        fun Instance(): ServerProxy {
            if(!::instance.isInitialized) instance = ServerProxy()
            return instance
        }
    }

}

data class ServerResponse(val success: Boolean, val gameState: GameState? = null, val errorMessage: String? = null)
