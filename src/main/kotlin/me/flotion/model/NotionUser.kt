package me.flotion.model

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import me.flotion.config.*
import me.flotion.exceptions.UnvalidatedUserException
import me.flotion.query.ModulesQuery
import org.jraf.klibnotion.model.property.spec.MultiSelectPropertySpec
import java.security.Key

data class NotionUser constructor(
	@GraphQLIgnore val accessToken: String,
	val email: String,
	val firstName: String,

	val limits: UnderstandingLimits,
	private val excludedModules: ExcludedModules
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

	/**
	 * Returns all the modules stored in the Notion database (even excluded ones).
	 */
	suspend fun getAllModules(): List<String> {
		val notion = NotionSingleton.userClient(accessToken)

		val modulesProperty = getModuleProperty()

		return modulesProperty?.options?.map { it.name } ?: emptyList()
	}

	/**
	 * Gets the modules excluded by the user.
	 */
	fun getExcludedModules(): List<String> = excludedModules.modules

	/**
	 * Gets all the user's modules that they haven't excluded.
	 */
	suspend fun getModules(): List<String> {
		val excludedModules = setOf(*getExcludedModules().toTypedArray())

		return getAllModules().filter { it !in excludedModules }
	}

	/**
	 * Gets the module property from the Notion database.
	 */
	suspend fun getModuleProperty(): MultiSelectPropertySpec? =
		NotionSingleton.userClient(accessToken).databases.getDatabase(databaseID).propertySpecs
			.find { it.name == MODULE_SELECT_KEY } as MultiSelectPropertySpec?
}

class NotionClientDetails(accessToken: String, val firstName: String, val limits: UnderstandingLimits) {
	val token: String

	init {
		val key: Key = Keys.hmacShaKeyFor(FLOTION_SECRET.toByteArray())
		token = Jwts.builder().setSubject(accessToken).signWith(key).compact()
	}
}
