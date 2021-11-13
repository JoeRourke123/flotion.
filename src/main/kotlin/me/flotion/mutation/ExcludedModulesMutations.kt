package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.query.ModulesQuery
import me.flotion.responses.ResponseObjects
import me.flotion.services.ModulesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ExcludedModulesMutations @Autowired constructor(private val modulesService: ModulesService) : Mutation {
	@GraphQLDescription("sets the modules excluded for the user")
	fun setModules(modules: List<String>, context: NotionContext): ResponseObjects.ModulesResponse {
		if(context.user == null) return ResponseObjects.ModulesResponse(401, ResponseMessages.NOT_LOGGED_IN.message)

		return try {
			val newModules = modulesService.setExcludedModules(modules, context.user)

			ResponseObjects.ModulesResponse(modules = newModules)
		} catch(e: Exception) {
			ResponseObjects.ModulesResponse(500, ResponseMessages.SERVER_ERROR.message)
		}
	}
}
