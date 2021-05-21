package me.flotion.mutation

import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.model.ExcludedModules
import me.flotion.query.ExcludedModulesQuery
import org.springframework.stereotype.Component

@Component
class ExcludedModulesMutations : Mutation {
	fun setModules(modules: List<String>, context: NotionContext): ExcludedModulesQuery.ExcludedModulesResponse {
		if(context.user == null) return ExcludedModulesQuery.ExcludedModulesResponse(401, ResponseMessages.NOT_LOGGED_IN.message)

		val newModules = ExcludedModules(ArrayList<String>(modules))
		newModules.saveToDB(context.user.accessToken)

		return ExcludedModulesQuery.ExcludedModulesResponse(modules = newModules)
	}
}
