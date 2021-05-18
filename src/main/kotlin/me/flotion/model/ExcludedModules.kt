package me.flotion.model

import me.flotion.config.RedisSingleton

class ExcludedModules(private val modules: List<String>) : List<String> by modules {
	companion object {
		fun loadFromDB(token: String) : ExcludedModules {
			val loadedModules: List<String> = RedisSingleton.db.mget(token)
			return ExcludedModules(loadedModules)
		}
	}
}
