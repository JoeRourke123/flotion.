package me.flotion.model

import me.flotion.config.RedisSingleton

data class UnderstandingLimits private constructor(
	val yellowLimit: Int,
	val greenLimit: Int,
) {
	companion object {
		fun loadFromDB(token: String): UnderstandingLimits {
			val yellowLim = RedisSingleton.db.hget(token, "yellow_limit")
			val greenLim = RedisSingleton.db.hget(token, "green_limit")

			return UnderstandingLimits(yellowLim.toInt(), greenLim.toInt())
		}
	}

	fun isValid(): Boolean = yellowLimit < greenLimit
}
