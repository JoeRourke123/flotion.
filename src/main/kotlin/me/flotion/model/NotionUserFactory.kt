package me.flotion.model

import me.flotion.config.*
import me.flotion.exceptions.UnvalidatedUserException
import org.jraf.klibnotion.client.NotionClient
import org.jraf.klibnotion.model.user.Person
import redis.clients.jedis.Jedis

abstract class NotionUserFactory {
	companion object {
		suspend fun userExists(token: String): Boolean {
			RedisSingleton.getJedisInstance().use { db ->
				return db.exists(token)
			}
		}

		private suspend fun setDefaultValues(token: String) {
			RedisSingleton.getJedisInstance().use { db ->
				val cardsDatabaseID = NotionSingleton.getUserCardDB(token)
				db.hset(
					token,
					CARD_DB_KEY,
					cardsDatabaseID ?: throw UnvalidatedUserException(ResponseMessages.CARD_DB_NOT_FOUND.message)
				)

				db.hset(token, YELLOW_LIM_KEY, YELLOW_LIMIT.toString())
				db.hset(token, GREEN_LIM_KEY, GREEN_LIMIT.toString())
			}
		}

		suspend fun fromPerson(token: String, person: Person): NotionUser {
			if (!userExists(token)) {
				setDefaultValues(token)
			}

//			val modules = ExcludedModules.loadFromDB(token)
			val lims = UnderstandingLimits.loadFromDB(token)

			return NotionUser(
				token,
				person.email,
				person.name,
				lims,
//				modules
			)
		}

		suspend fun fromToken(token: String): NotionUser {
			if (!userExists(token)) {
//				throw UnvalidatedUserException("Attempted to create user from token before checking workspace users")
				setDefaultValues(token)
			}

			val userClient = NotionSingleton.userClient(token)

			val workspaceUsers = userClient.users.getUserList().results.filter { it as? Person != null }

			return fromPerson(token, workspaceUsers[0] as Person)
		}
	}
}
