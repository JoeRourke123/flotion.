package me.flotion.query

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import me.flotion.config.MODULE_SELECT_KEY
import me.flotion.config.NotionSingleton
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import org.jraf.klibnotion.model.property.spec.MultiSelectPropertySpec
import org.jraf.klibnotion.model.property.spec.SelectPropertySpec
import org.springframework.stereotype.Component

@Component
class ModulesQuery : Query {
	class ModulesResponse(
		val response: Int = 200, val message: String = ResponseMessages.SUCCESS.message,
		val modules: List<String>? = null,
		val colours: List<String>? = null,
	)

	@GraphQLDescription("retrieves the modules a user has chosen to not show in the application")
	fun getExcludedModules(context: NotionContext): ModulesResponse =
		if (context.user == null) ModulesResponse(
			401,
			ResponseMessages.NOT_LOGGED_IN.message
		) else ModulesResponse(modules = context.user.excludedModules)


	@GraphQLDescription("gets all the user's modules - except those which are excluded")
	suspend fun getModules(context: NotionContext): ModulesResponse {
		if (context.user == null) return ModulesResponse(401, ResponseMessages.NOT_LOGGED_IN.message)

		val client = NotionSingleton.userClient(context.user.accessToken)
		val dbID = context.user.databaseID

		val modulesProperty =
			client.databases.getDatabase(dbID).propertySpecs.find { it.name == MODULE_SELECT_KEY } as MultiSelectPropertySpec?
				?: return ModulesResponse(400, ResponseMessages.MALFORMED_CARD.message)

		val modules = ArrayList<String>()
		val colours = ArrayList<String>()

		val excludedSet = setOf<String>(*context.user.excludedModules.toTypedArray())

		for (module in modulesProperty.options) {
			if (module.name !in excludedSet) {
				modules.add(module.name)
				colours.add(module.color.name)
			}
		}

		return ModulesResponse(modules = modules, colours = colours)
	}
}
