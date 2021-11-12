package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.model.NotionClientDetails
import me.flotion.config.NotionSingleton
import me.flotion.config.RedisSingleton
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.exceptions.PersonalWorkspaceOnlyException
import me.flotion.model.NotionUser
import me.flotion.model.NotionUserFactory
import me.flotion.services.AuthenticationService
import org.jraf.klibnotion.model.exceptions.NotionClientRequestException
import org.jraf.klibnotion.model.user.Person
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpCookie
import org.springframework.stereotype.Component
import org.springframework.web.context.support.HttpRequestHandlerServlet
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class AuthenticationMutation @Autowired constructor(val authService: AuthenticationService) : Mutation {
	data class AuthorisationResponse(
		val response: Int = 200,
		val user: NotionClientDetails? = null,
		val message: String = ResponseMessages.SUCCESS.message
	)

	@GraphQLDescription("mutation that accepts an oAuth code to get the access_token etc")
	suspend fun authoriseUser(context: NotionContext, code: String): AuthorisationResponse = try {
		AuthorisationResponse(
			user = authService.authoriseUser(code).clientDetails
		)
	} catch(e: Exception) {
		AuthorisationResponse(
			response = 500,
			message = ResponseMessages.AUTH_ERROR.message
		)
	} catch (e: NotionClientRequestException) {
		AuthorisationResponse(
			response = 400,
			message = ResponseMessages.EXPIRED_TOKEN.message
		)
	} catch (e: PersonalWorkspaceOnlyException) {
		AuthorisationResponse(
			response = 400,
			message = ResponseMessages.PERSONAL_WORKSPACE_ONLY.message
		)
	}
}
