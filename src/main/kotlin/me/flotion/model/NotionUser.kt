package me.flotion.model

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import me.flotion.config.FLOTION_SECRET
import java.security.Key

data class NotionUser constructor(
	@GraphQLIgnore val accessToken: String,
	val email: String,
	val firstName: String,

	val limits: UnderstandingLimits,
//	val excludedModules: ExcludedModules
) {

	val clientDetails get() = NotionClientDetails(accessToken, firstName, limits)
}

class NotionClientDetails(accessToken: String, val firstName: String, val limits: UnderstandingLimits) {
	val token: String

	init {
		val key: Key = Keys.hmacShaKeyFor(FLOTION_SECRET.toByteArray())
		token = Jwts.builder().setSubject(accessToken).signWith(key).compact()
	}
}
