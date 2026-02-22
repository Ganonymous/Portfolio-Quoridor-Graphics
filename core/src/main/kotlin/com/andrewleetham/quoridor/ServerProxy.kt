package com.andrewleetham.quoridor

import com.andrewleetham.quoridorserver.model.GameState
import com.andrewleetham.quoridorserver.model.MoveRequest
import com.andrewleetham.quoridorserver.model.WallRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


class ServerProxy {

    private val baseUrl = "https://portfolio-quoridor-server.onrender.com"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    fun dispose() {
        scope.cancel()
    }

    private suspend fun safeCall(block: suspend () -> HttpResponse): ServerResponse {
        return try {
            val response = block()
            if (response.status.isSuccess()) {
                val state = response.body<GameState>()
                ServerResponse(success = true, gameState = state)
            } else {
                ServerResponse(success = false, errorMessage = "Server request failed: ${response.status.description}")
            }

        } catch (e: Exception){
            ServerResponse (success = false, errorMessage = e.message)
        }
    }

     fun createGame (playerName: String, callback: (ServerResponse) -> Unit){
         scope.launch {
             val result =  safeCall {
                 client.post("$baseUrl/games") {
                     header("Player-Name", playerName)
                 }
             }
             withContext(Dispatchers.Main){
                 callback(result)
             }
         }

    }

    fun joinGame (playerName: String, gameId: String, callback: (ServerResponse) -> Unit){
        scope.launch {
            val result = safeCall {
                client.post("$baseUrl/games/$gameId/join"){
                    header("Player-Name", playerName)
                }
            }

            withContext(Dispatchers.Main){
                callback(result)
            }
        }

    }

    fun quitGame (playerName: String, gameId: String, callback: (ServerResponse) -> Unit){
        scope.launch {
            val result = safeCall {
                client.delete("$baseUrl/games/$gameId/quit"){
                    header("Player-Name", playerName)
                }
            }

            withContext(Dispatchers.Main){
                callback(result)
            }
        }
    }


    fun startGame(playerName: String, gameId: String, callback: (ServerResponse) -> Unit){
        scope.launch {
            val result = safeCall {
                client.put("$baseUrl/games/$gameId/start") {
                    header("Player-Name", playerName)
                }
            }

            withContext(Dispatchers.Main){
                callback(result)
            }
        }
    }

    fun getGameState (playerName: String, gameId: String, callback: (ServerResponse) -> Unit){
        scope.launch {
            val result = safeCall {
                client.get("$baseUrl/games/$gameId") {
                    header("Player-Name", playerName)
                }
            }
            withContext(Dispatchers.Main){
                callback(result)
            }
        }
    }

    fun sendMove (playerName: String, gameId: String, to: Pair<Int, Int>, callback: (ServerResponse) -> Unit){
        scope.launch {
            val result = safeCall {
                client.post("$baseUrl/games/$gameId/move") {
                    header("Player-Name", playerName)
                    contentType(ContentType.Application.Json)
                    setBody(MoveRequest(to))
                }
            }
            withContext(Dispatchers.Main){
                callback(result)
            }
        }
    }

    fun sendWall (playerName: String, gameId: String, target: Pair<Int, Int>, horizontal: Boolean, callback: (ServerResponse) -> Unit){
        scope.launch {
            val result = safeCall {
                client.post("$baseUrl/games/$gameId/wall") {
                    header("Player-Name", playerName)
                    contentType(ContentType.Application.Json)
                    setBody(WallRequest(target, horizontal))
                }
            }
            withContext(Dispatchers.Main){
                callback(result)
            }
        }
    }



    companion object {
        lateinit var instance: ServerProxy
        private set

        fun retrieveInstance(): ServerProxy {
            if(!::instance.isInitialized) instance = ServerProxy()
            return instance
        }
    }

}

data class ServerResponse(val success: Boolean, val gameState: GameState? = null, val errorMessage: String? = null)
