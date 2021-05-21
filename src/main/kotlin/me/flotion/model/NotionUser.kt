package me.flotion.model

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import me.flotion.config.*
import me.flotion.exceptions.UnvalidatedUserException
import java.security.Key

data class NotionUser constructor(
	@GraphQLIgnore val accessToken: String,
	val email: String,
	val firstName: String,

	val limits: UnderstandingLimits,
	val excludedModules: ExcludedModules
) {
	val databaseID: String
		get() {
			var dbID: String? = null

			RedisSingleton.getJedisInstance().use { db ->
				if (db.hexists(accessToken, CARD_DB_KEY)) {
					dbID = db.hget(accessToken, CARD_DB_KEY)
				}
			}

			return dbID ?: throw UnvalidatedUserException(ResponseMessages.AUTH_ERROR.message)
		}

	val clientDetails get() = NotionClientDetails(accessToken, firstName, limits)
}

class NotionClientDetails(accessToken: String, val firstName: String, val limits: UnderstandingLimits) {
	val token: String

	init {
		val key: Key = Keys.hmacShaKeyFor(FLOTION_SECRET.toByteArray())
		token = Jwts.builder().setSubject(accessToken).signWith(key).compact()
	}
}
