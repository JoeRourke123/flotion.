package me.flotion.query

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import me.flotion.config.NotionSingleton
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.model.NotionClientDetails
import me.flotion.model.NotionUser
import me.flotion.responses.ResponseObjects
import me.flotion.services.AuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class AuthenticationQuery @Autowired constructor(private val authService: AuthenticationService) : Query {

	@GraphQLDescription("generates URL for user to login to Notion, provide access, and get redirect URI")
	fun generateAuthURL(): String = authService.generateAuthenticationLink()

	@GraphQLDescription("returns details of the logged in user")
	fun userDetails(context: NotionContext): ResponseObjects.UserDetailsResponse {
		return try {
			if(context.user == null) {
				ResponseObjects.UserDetailsResponse(401, ResponseMessages.NOT_LOGGED_IN.message)
			} else {
				ResponseObjects.UserDetailsResponse(user = context.user.clientDetails)
			}
		} catch(e: Exception) {
			ResponseObjects.UserDetailsResponse(500, ResponseMessages.SERVER_ERROR.message)
		}
	}
}
