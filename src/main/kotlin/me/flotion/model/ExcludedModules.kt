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
}
