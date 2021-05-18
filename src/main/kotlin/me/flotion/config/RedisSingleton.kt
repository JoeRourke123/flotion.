package me.flotion.config

import redis.clients.jedis.Jedis

object RedisSingleton {
	val db: Jedis = Jedis()
}
