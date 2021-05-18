package me.flotion.model

import me.flotion.config.GREEN_LIM_KEY
import me.flotion.config.RedisSingleton
import me.flotion.config.YELLOW_LIM_KEY

data class UnderstandingLimits private constructor(
	val yellowLimit: Int,
	val greenLimit: Int,
) {
	companion object {
		fun loadFromDB(token: String): UnderstandingLimits {
			val yellowLim = RedisSingleton.db.hget(token, YELLOW_LIM_KEY)
			val greenLim = RedisSingleton.db.hget(token, GREEN_LIM_KEY)

			return UnderstandingLimits(yellowLim.toInt(), greenLim.toInt())
		}
	}

	fun isValid(): Boolean = yellowLimit < greenLimit
}
