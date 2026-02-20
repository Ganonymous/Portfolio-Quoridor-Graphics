package com.andrewleetham.quoridorserver.model

import kotlinx.serialization.Serializable


@Serializable
data class MoveRequest(val to: Pair<Int, Int>)

@Serializable
data class WallRequest(val target: Pair<Int, Int>, val horizontal: Boolean)
