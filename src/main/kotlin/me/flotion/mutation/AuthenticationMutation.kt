package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.model.NotionClientDetails
import me.flotion.config.NotionSingleton
import me.flotion.config.ResponseMessages
import me.flotion.model.NotionUserFactory
import org.jraf.klibnotion.model.exceptions.NotionClientRequestException
import org.jraf.klibnotion.model.user.Person
import org.springframework.stereotype.Component

@Component
class AuthenticationMutation : Mutation {
	data class AuthorisationResponse(val response: Int, val user: NotionClientDetails? = null, val message: String = ResponseMessages.SUCCESS.message)

	@GraphQLDescription("mutation that accepts an oAuth code to get the access_token etc")
	suspend fun authoriseUser(code: String): AuthorisationResponse {
		val token: String

		try {
			token = NotionSingleton.client.oAuth.getAccessToken(
				oAuthCredentials = NotionSingleton.oAuthCredentials,
				code = code
			).accessToken
		} catch(e: NotionClientRequestException) {
			return AuthorisationResponse(400, message = ResponseMessages.EXPIRED_TOKEN.message)
		} catch(e: Exception) {
			return AuthorisationResponse(500, message = ResponseMessages.AUTH_ERROR.message)
		}

		val userClient = NotionSingleton.userClient(token)

		val workspaceUsers = userClient.users.getUserList().results.filter { it as? Person != null }

		return if(workspaceUsers.size == 1) {
			AuthorisationResponse(400, message = ResponseMessages.PERSONAL_WORKSPACE_ONLY.message)
		} else {
			val user = NotionUserFactory.fromPerson(token, workspaceUsers[0] as Person)
			AuthorisationResponse(200, user = user.clientDetails)
		}
	}
}
