package me.flotion.mutation

import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.model.ExcludedModules
import me.flotion.query.ModulesQuery
import org.springframework.stereotype.Component

@Component
class ExcludedModulesMutations : Mutation {
	fun setModules(modules: List<String>, context: NotionContext): ModulesQuery.ModulesResponse {
		if(context.user == null) return ModulesQuery.ModulesResponse(401, ResponseMessages.NOT_LOGGED_IN.message)

		val newModules = ExcludedModules(ArrayList<String>(modules))
		newModules.saveToDB(context.user.accessToken)

		return ModulesQuery.ModulesResponse(modules = newModules)
	}
}
