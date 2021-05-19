package me.flotion.config

import redis.clients.jedis.Jedis

object RedisSingleton {
	fun getJedisInstance() = Jedis()
}
