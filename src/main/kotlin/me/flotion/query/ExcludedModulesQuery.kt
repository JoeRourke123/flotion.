package me.flotion.query

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.model.ExcludedModules
import org.springframework.stereotype.Component

@Component
class ExcludedModulesQuery : Query {
	class ExcludedModulesResponse(
		val response: Int = 200, val message: String = ResponseMessages.SUCCESS.message,
		val modules: List<String>? = null
	)

	@GraphQLDescription("retrieves the modules a user has chosen to not show on the application")
	fun getExcludedModules(context: NotionContext): ExcludedModulesResponse =
		if (context.user == null) ExcludedModulesResponse(
			401,
			ResponseMessages.NOT_LOGGED_IN.message
		) else ExcludedModulesResponse(modules = context.user.excludedModules)
}
