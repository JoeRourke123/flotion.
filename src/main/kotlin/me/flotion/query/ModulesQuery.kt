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

	private suspend fun getModulesAndColours(modules: List<String>, context: NotionContext): ModulesResponse {
		val modulesProperty = context.user?.getModuleProperty() ?: return ModulesResponse(
			400,
			ResponseMessages.MALFORMED_CARD.message
		)

		val excludedSet = setOf(*modules.toTypedArray())

		val colours =
			modulesProperty.options.asSequence().filter { it.name in excludedSet }.map { it.color.name }.toList()

		return ModulesResponse(modules = modules, colours = colours)
	}

	@GraphQLDescription("retrieves the modules a user has chosen to not show in the application")
	suspend fun getExcludedModules(context: NotionContext): ModulesResponse =
		try {
			if (context.user == null) ModulesResponse(
				401,
				ResponseMessages.NOT_LOGGED_IN.message
			) else getModulesAndColours(context.user.getExcludedModules(), context)
		} catch(e: Exception) {
			ModulesResponse(500, ResponseMessages.SERVER_ERROR.message)
		}

	@GraphQLDescription("gets all the user's modules - except those which are excluded")
	suspend fun getModules(context: NotionContext): ModulesResponse =
		try {
			if (context.user == null) ModulesResponse(
				401,
				ResponseMessages.NOT_LOGGED_IN.message
			) else getModulesAndColours(context.user.getModules(), context)
		} catch(e: Exception) {
			ModulesResponse(500, ResponseMessages.SERVER_ERROR.message)
		}

	@GraphQLDescription("gets all the user's modules - even those which are excluded")
	suspend fun allModules(context: NotionContext): ModulesResponse =
		try {
			if (context.user == null) ModulesResponse(
				401,
				ResponseMessages.NOT_LOGGED_IN.message
			) else getModulesAndColours(context.user.getAllModules(), context)
		} catch(e: Exception) {
			 ModulesResponse(500, ResponseMessages.SERVER_ERROR.message)
		}
}
