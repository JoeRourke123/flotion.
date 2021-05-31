package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.model.NotionClientDetails
import me.flotion.config.NotionSingleton
import me.flotion.config.RedisSingleton
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.model.NotionUser
import me.flotion.model.NotionUserFactory
import org.jraf.klibnotion.model.exceptions.NotionClientRequestException
import org.jraf.klibnotion.model.user.Person
import org.springframework.http.HttpCookie
import org.springframework.stereotype.Component
import org.springframework.web.context.support.HttpRequestHandlerServlet
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class AuthenticationMutation : Mutation {
	data class AuthorisationResponse(
		val response: Int,
		val user: NotionClientDetails? = null,
		val message: String = ResponseMessages.SUCCESS.message
	)

	@GraphQLDescription("mutation that accepts an oAuth code to get the access_token etc")
	suspend fun authoriseUser(context: NotionContext, code: String): AuthorisationResponse {
		val token: String

		try {
			token = NotionSingleton.client.oAuth.getAccessToken(
				oAuthCredentials = NotionSingleton.oAuthCredentials,
				code = code
			).accessToken
		} catch (e: NotionClientRequestException) {
			println(e.message)
			return AuthorisationResponse(400, message = ResponseMessages.EXPIRED_TOKEN.message)
		} catch (e: Exception) {
			println(e.message)
			return AuthorisationResponse(500, message = e.message ?: ResponseMessages.AUTH_ERROR.message)
		}

		val userClient = NotionSingleton.userClient(token)

		val workspaceUsers = userClient.users.getUserList().results.filter { it as? Person != null }

		return if (!NotionUserFactory.userExists(token) && workspaceUsers.size != 1) {
			AuthorisationResponse(400, message = ResponseMessages.PERSONAL_WORKSPACE_ONLY.message)
		} else {
			try {
				val user: NotionUser = NotionUserFactory.fromPerson(token, workspaceUsers[0] as Person)

				AuthorisationResponse(200, user = user.clientDetails)
			} catch (e: Exception) {
				print(e)
				AuthorisationResponse(500, message = e.message ?: ResponseMessages.AUTH_ERROR.message)
			}
		}
	}
}
