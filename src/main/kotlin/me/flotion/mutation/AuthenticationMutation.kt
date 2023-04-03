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
import me.flotion.responses.ResponseObjects
import me.flotion.services.AuthenticationService
import org.jraf.klibnotion.model.exceptions.NotionClientRequestException
import org.jraf.klibnotion.model.user.Person
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpCookie
import org.springframework.stereotype.Component
import org.springframework.web.context.support.HttpRequestHandlerServlet
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class AuthenticationMutation @Autowired constructor(private val authService: AuthenticationService) : Mutation {

	@GraphQLDescription("mutation that accepts an oAuth code to get the access_token etc")
	suspend fun authoriseUser(context: NotionContext, code: String): ResponseObjects.AuthorisationResponse = try {
		ResponseObjects.AuthorisationResponse(
			user = authService.authoriseUser(code).clientDetails
		)
	} catch(e: Exception) {
		e.printStackTrace()
		ResponseObjects.AuthorisationResponse(
			response = 500,
			message = ResponseMessages.AUTH_ERROR.message
		)
	} catch (e: NotionClientRequestException) {
		ResponseObjects.AuthorisationResponse(
			response = 400,
			message = ResponseMessages.EXPIRED_TOKEN.message
		)
	} catch (e: PersonalWorkspaceOnlyException) {
		ResponseObjects.AuthorisationResponse(
			response = 400,
			message = ResponseMessages.PERSONAL_WORKSPACE_ONLY.message
		)
	}
}
