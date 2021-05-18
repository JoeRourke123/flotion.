package me.flotion.query

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import me.flotion.config.NotionSingleton
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class AuthenticationQuery : Query {
	@GraphQLDescription("generates URL for user to login to Notion, provide access, and get redirect URI")
	fun generateAuthURL(): String {
		val uniqueState = Random.nextLong().toString()
		return NotionSingleton.client.oAuth.getUserPromptUri(oAuthCredentials = NotionSingleton.oAuthCredentials, uniqueState = uniqueState)
	}
}
