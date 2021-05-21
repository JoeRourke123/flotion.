package me.flotion.model

import me.flotion.config.RedisSingleton

class ExcludedModules(val modules: ArrayList<String>) : List<String> by modules {
	companion object {
		fun loadFromDB(token: String) : ExcludedModules {
			RedisSingleton.getJedisInstance().use { db ->
				val loadedModules: List<String> = db.lrange("$token#modules", 0, -1)
				return ExcludedModules(ArrayList<String>(loadedModules))
			}
		}
	}

	fun saveToDB(token: String) {
		RedisSingleton.getJedisInstance().use { db ->
			val valuesCount = db.llen("$token#modules")
			for (i in valuesCount downTo 1) {
				db.lpop("$token#modules")
			}

			for (m in modules) {
				db.lpush("$token#modules", m)
			}
		}
	}
}
