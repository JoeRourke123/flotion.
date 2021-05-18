package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import kotlinx.coroutines.coroutineScope
import me.flotion.model.NotionClientDetails
import me.flotion.config.NotionSingleton
import me.flotion.model.NotionUser
import me.flotion.model.UnderstandingLimits
import org.jraf.klibnotion.model.exceptions.NotionClientRequestException
import org.jraf.klibnotion.model.user.Person
import org.springframework.stereotype.Component

@Component
class AuthenticationQuery : Mutation {
	data class AuthorisationResponse(val response: Int, val user: NotionClientDetails? = null, val message: String? = null)

	@GraphQLDescription("mutation that accepts an oAuth code to get the access_token etc")
	suspend fun authoriseUser(code: String): AuthorisationResponse {
		val token: String

		try {
			token = NotionSingleton.client.oAuth.getAccessToken(
				oAuthCredentials = NotionSingleton.oAuthCredentials,
				code = code
			).accessToken
		} catch(e: NotionClientRequestException) {
			return AuthorisationResponse(400, message = "Your token has expired, please try re-authorising")
		} catch(e: Exception) {
			return AuthorisationResponse(500, message = "Something went wrong when authorising you - try again")
		}

		val userClient = NotionSingleton.userClient(token)

		val workspaceUsers = userClient.users.getUserList().results.filter { it as? Person != null }

		return if(workspaceUsers.size == 1) {
			AuthorisationResponse(400, message = "You can only use Flotion on a personal workspace - try out on a different workspace")
		} else {
			val user = NotionUser.fromPerson(token, workspaceUsers[0] as Person)
			AuthorisationResponse(200, user = user.clientDetails)
		}
	}
}
