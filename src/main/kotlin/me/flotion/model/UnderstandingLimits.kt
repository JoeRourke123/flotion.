package me.flotion.model

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import me.flotion.config.*

data class UnderstandingLimits constructor(
	val yellowLimit: Int = YELLOW_LIMIT,
	val greenLimit: Int = GREEN_LIMIT,
) {
	companion object {
		fun loadFromDB(token: String): UnderstandingLimits {
			RedisSingleton.getJedisInstance().use { db ->
				val yellowLim = db.hget(token, YELLOW_LIM_KEY)
				val greenLim = db.hget(token, GREEN_LIM_KEY)

				return UnderstandingLimits(yellowLim.toInt(), greenLim.toInt())
			}
		}
	}

	fun isValid(): Boolean = yellowLimit < greenLimit

	@GraphQLIgnore
	fun saveToUser(token: String) {
		RedisSingleton.getJedisInstance().use { db ->
			db.hset(token, YELLOW_LIM_KEY, yellowLimit.toString())
			db.hset(token, GREEN_LIM_KEY, greenLimit.toString())
		}
	}

	fun getUnderstandingLevel(corrects: Int): Understanding = when {
		(corrects < yellowLimit) -> Understanding.RED
		(corrects < greenLimit) -> Understanding.YELLOW
		else -> Understanding.GREEN
	}
}
